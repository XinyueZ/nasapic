/*
                   _ooOoo_
                  o8888888o
                  88" . "88
                  (| -_- |)
                  O\  =  /O
               ____/`---'\____
             .'  \\|     |//  `.
            /  \\|||  :  |||//  \
           /  _||||| -:- |||||-  \
           |   | \\\  -  /// |   |
           | \_|  ''\---/''  |   |
           \  .-\__  `-`  ___/-. /
         ___`. .'  /--.--\  `. . __
      ."" '<  `.___\_<|>_/___.'  >'"".
     | | :  `- \`.;`\ _ /`;.`/ - ` : | |
     \  \ `-.   \_ __\ /__ _/   .-` /  /
======`-.____`-.___\_____/___.-`____.-'======
                   `=---='
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
         佛祖保佑       永无BUG
*/
//          佛曰:
//                  写字楼里写字间，写字间里程序员；
//                  程序人员写程序，又拿程序换酒钱。
//                  酒醒只在网上坐，酒醉还来网下眠；
//                  酒醉酒醒日复日，网上网下年复年。
//                  但愿老死电脑间，不愿鞠躬老板前；
//                  奔驰宝马贵者趣，公交自行程序员。
//                  别人笑我忒疯癫，我笑自己命太贱。

package com.nasa.pic.app;

import android.support.multidex.MultiDexApplication;
import android.text.TextUtils;

import com.chopping.net.TaskHelper;
import com.chopping.rest.RestApiManager;
import com.chopping.rest.RestFireManager;
import com.chopping.utils.RestUtils;
import com.crashlytics.android.Crashlytics;
import com.facebook.stetho.Stetho;
import com.nasa.pic.R;
import com.nasa.pic.utils.Prefs;
import com.tinyurl4j.Api;
import com.tinyurl4j.Api.TinyUrl;
import com.tinyurl4j.data.Response;

import io.fabric.sdk.android.Fabric;
import retrofit2.Call;
import retrofit2.Callback;


/**
 * The app-object of the project.
 *
 * @author Xinyue Zhao
 */
public final class App extends MultiDexApplication {
	/**
	 * Application's instance.
	 */
	public static App Instance;

	{
		Instance = this;
	}

	private RestApiManager mApiManager;
	private RestFireManager mFireManager;

	@Override
	public void onCreate() {
		super.onCreate();
		String[] fireInfo = RestUtils.initRest(
				this,
				true
		);
		if( fireInfo != null ) {
			mFireManager = new RestFireManager(
					fireInfo[ 0 ],
					fireInfo[ 1 ],
					Integer.valueOf( fireInfo[ 2 ] ),
					"reqTime"
			);
		}
		mFireManager.onCreate( this );
		mApiManager = new RestApiManager();
		mApiManager.onCreate();

		Fabric.with(this, new Crashlytics());
		TaskHelper.init(getApplicationContext());
		Prefs prefs = Prefs.createInstance(this);
		Stetho.initialize(Stetho.newInitializerBuilder(this).enableDumpapp(Stetho.defaultDumperPluginsProvider(this))
				.enableWebKitInspector(Stetho.defaultInspectorModulesProvider(this)).build());


		String url = Prefs.getInstance().getAppDownloadInfo();
		if (TextUtils.isEmpty(url) || !url.contains("tinyurl")) {
			Call<Response> tinyUrlCall = Api.Retrofit.create(TinyUrl.class).getTinyUrl(
					getString(R.string.lbl_store_url, getPackageName()));
			tinyUrlCall.enqueue(new Callback<Response>() {
				@Override
				public void onResponse(Call<Response> call,retrofit2.Response<Response> response) {
					if (response.isSuccess()) {
						Prefs.getInstance().setAppDownloadInfo(
								getString(R.string.lbl_share_download_app, getString(R.string.application_name),
										response.body().getResult()));
					} else {
						onFailure(null,null);
					}
				}

				@Override
				public void onFailure(Call<Response> call, Throwable t) {
					Prefs.getInstance().setAppDownloadInfo(
							getString(R.string.lbl_share_download_app, getString(R.string.application_name),
									getString(R.string.lbl_store_url, getPackageName())));
				}
			});
		}
	}


	public RestApiManager getApiManager() {
		return mApiManager;
	}


	public RestFireManager getFireManager() {
		return mFireManager;
	}
}
