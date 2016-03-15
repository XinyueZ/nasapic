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

import com.chopping.net.TaskHelper;
import com.facebook.stetho.Stetho;
import com.nasa.pic.utils.Prefs;


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


	@Override
	public void onCreate() {
		super.onCreate();
		TaskHelper.init( getApplicationContext() );
		Prefs prefs = Prefs.createInstance( this );
		Stetho.initialize( Stetho.newInitializerBuilder( this ).enableDumpapp( Stetho.defaultDumperPluginsProvider( this ) )
				.enableWebKitInspector( Stetho.defaultInspectorModulesProvider( this ) ).build() );

	}

}
