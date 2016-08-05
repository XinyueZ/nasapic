package com.nasa.pic.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.Toolbar.OnMenuItemClickListener;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;

import com.chopping.activities.BaseActivity;
import com.chopping.application.BasicPrefs;
import com.chopping.utils.Utils;
import com.nasa.pic.R;
import com.nasa.pic.app.App;
import com.nasa.pic.utils.Prefs;
import com.tinyurl4j.Api;
import com.tinyurl4j.Api.TinyUrl;
import com.tinyurl4j.data.Response;

import java.util.Date;

import retrofit2.Call;
import retrofit2.Callback;

import static android.R.id.home;


public abstract class AppNormalActivity
		extends BaseActivity
		implements OnMenuItemClickListener {
	public static final String EXTRAS_TITLE                 = "com.nasa.pic.app.activities.PhotoViewActivity.title";
	public static final String EXTRAS_DESCRIPTION           = "com.nasa.pic.app.activities.PhotoViewActivity.description";
	public static final String EXTRAS_URL_TO_PHOTO          = "com.nasa.pic.app.activities.PhotoViewActivity.url2photo";
	public static final String EXTRAS_URL_TO_PHOTO_FALLBACK = "com.nasa.pic.app.activities.PhotoViewActivity.url2photo.fallback";
	public static final String EXTRAS_DATE                  = "com.nasa.pic.app.activities.PhotoViewActivity.datetime";
	public static final String EXTRAS_TYPE                  = "com.nasa.pic.app.activities.PhotoViewActivity.type";


	//[Begin for photo's info]
	private String mType;
	private String mTitle;
	private Date   mDatetime;
	private String mUrl2Photo;
	private String mUrl2PhotoFallback;
	private String mDescription;
	//[End]

	/**
	 * A tinyurl to the {@link com.nasa.pic.ds.PhotoDB}.
	 */
	private String mSharedUrl;


	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putSerializable(EXTRAS_TYPE,
		                         mType);
		outState.putSerializable(EXTRAS_TITLE,
		                         mTitle);
		outState.putSerializable(EXTRAS_DATE,
		                         mDatetime);
		outState.putString(EXTRAS_URL_TO_PHOTO,
		                   mUrl2Photo);
		outState.putString(EXTRAS_URL_TO_PHOTO_FALLBACK,
		                   mUrl2PhotoFallback);
		outState.putString(EXTRAS_DESCRIPTION,
		                   mDescription);
	}


	protected void initPull2Load(SwipeRefreshLayout swipeRefreshLayout) {
		int actionbarHeight = Utils.getActionBarHeight(App.Instance);
		swipeRefreshLayout.setColorSchemeResources(R.color.c_refresh_1,
		                                           R.color.c_refresh_2,
		                                           R.color.c_refresh_3,
		                                           R.color.c_refresh_4);
		swipeRefreshLayout.setProgressViewEndTarget(true,
		                                            actionbarHeight * 2);
		swipeRefreshLayout.setProgressViewOffset(false,
		                                         0,
		                                         actionbarHeight * 2);
	}


	protected void initMenu(final Toolbar toolbar) {
		toolbar.inflateMenu(getMenuRes());
		Call<Response> tinyUrlCall = Api.Retrofit.create(TinyUrl.class)
		                                         .getTinyUrl(mUrl2Photo);
		tinyUrlCall.enqueue(new Callback<Response>() {
			@Override
			public void onResponse(Call<Response> call,
			                       retrofit2.Response<Response> res) {
				if (res.isSuccessful()) {
					Response response = res.body();
					mSharedUrl = !TextUtils.isEmpty(response.getResult()) ?
					             response.getResult() :
					             mUrl2Photo;
					buildNormalShareMenu(toolbar.getMenu());
				} else {
					onFailure(null,
					          null);
				}
			}

			@Override
			public void onFailure(Call<Response> call,
			                      Throwable t) {
				mSharedUrl = mUrl2Photo;
				buildNormalShareMenu(toolbar.getMenu());
			}
		});
		toolbar.setOnMenuItemClickListener(this);
		toolbar.setNavigationIcon(R.drawable.ic_back_home);
		toolbar.setNavigationOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				ActivityCompat.finishAfterTransition(AppNormalActivity.this);
			}
		});
	}


	@Override
	public boolean onMenuItemClick(MenuItem item) {
		switch (item.getItemId()) {
			case home:
				ActivityCompat.finishAfterTransition(this);
				break;
			case R.id.action_fb_share_photo:
				com.nasa.pic.utils.Utils.facebookShare(this,
				                                       mTitle,
				                                       mDescription,
				                                       mUrl2Photo);
				break;
		}
		return true;
	}


	@Override
	protected BasicPrefs getPrefs() {
		return Prefs.getInstance();
	}


	protected abstract int getMenuRes();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		com.nasa.pic.utils.Utils.fullScreen(this);
		super.onCreate(savedInstanceState);
		if (savedInstanceState != null) {
			setType(savedInstanceState.getString(EXTRAS_TYPE));
			setPhotoTitle(savedInstanceState.getString(EXTRAS_TITLE));
			setUrl2Photo(savedInstanceState.getString(EXTRAS_URL_TO_PHOTO));
			setUrl2PhotoFallback(savedInstanceState.getString(EXTRAS_URL_TO_PHOTO_FALLBACK));
			setDescription(savedInstanceState.getString(EXTRAS_DESCRIPTION));
			setDatetime((Date) savedInstanceState.getSerializable(EXTRAS_DATE));
		} else {
			Intent intent = getIntent();
			setType(intent.getStringExtra(EXTRAS_TYPE));
			setPhotoTitle(intent.getStringExtra(EXTRAS_TITLE));
			setUrl2Photo(intent.getStringExtra(EXTRAS_URL_TO_PHOTO));
			setUrl2PhotoFallback(intent.getStringExtra(EXTRAS_URL_TO_PHOTO_FALLBACK));
			setDescription(intent.getStringExtra(EXTRAS_DESCRIPTION));
			setDatetime((Date) intent.getSerializableExtra(EXTRAS_DATE));
		}
	}


	private void buildNormalShareMenu(Menu menu) {
		MenuItem menuShare = menu.findItem(R.id.action_share_photo);
		menuShare.setVisible(true);
		android.support.v7.widget.ShareActionProvider provider = (android.support.v7.widget.ShareActionProvider) MenuItemCompat.getActionProvider(menuShare);
		String text = App.Instance.getString(R.string.lbl_share_item_content,
		                                     mDescription,
		                                     mSharedUrl,
		                                     Prefs.getInstance()
		                                          .getAppDownloadInfo());
		provider.setShareIntent(Utils.getDefaultShareIntent(provider,
		                                                    mTitle,
		                                                    text));
	}

	protected String getType() {
		return mType;
	}

	private void setType(String type) {
		mType = type;
	}

	protected String getPhotoTitle() {
		return mTitle;
	}

	private void setPhotoTitle(String title) {
		mTitle = title;
	}

	protected Date getDatetime() {
		return mDatetime;
	}

	private void setDatetime(Date datetime) {
		mDatetime = datetime;
	}

	protected String getUrl2Photo() {
		return mUrl2Photo;
	}

	private void setUrl2Photo(String url2Photo) {
		mUrl2Photo = url2Photo;
	}

	public String getUrl2PhotoFallback() {
		return mUrl2PhotoFallback;
	}

	private void setUrl2PhotoFallback(String url2PhotoFallback) {
		mUrl2PhotoFallback = url2PhotoFallback;
	}

	protected String getDescription() {
		return mDescription;
	}

	private void setDescription(String description) {
		mDescription = description;
	}


}
