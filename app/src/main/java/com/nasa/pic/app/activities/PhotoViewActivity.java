package com.nasa.pic.app.activities;

import java.io.IOException;
import java.util.Date;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.os.AsyncTaskCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.chopping.activities.BaseActivity;
import com.chopping.application.BasicPrefs;
import com.chopping.utils.DateTimeUtils;
import com.chopping.utils.Utils;
import com.nasa.pic.R;
import com.nasa.pic.app.App;
import com.nasa.pic.databinding.ActivityPhotoViewBinding;
import com.nasa.pic.utils.Prefs;
import com.squareup.picasso.Picasso;
import com.tinyurl4j.Api;
import com.tinyurl4j.Api.TinyUrl;
import com.tinyurl4j.data.Response;

import retrofit2.Call;
import retrofit2.Callback;
import uk.co.senab.photoview.PhotoViewAttacher.OnPhotoTapListener;

import static android.R.id.home;

/**
 * Show big image as photo.
 *
 * @author Xinyue Zhao
 */
public final class PhotoViewActivity extends BaseActivity implements OnPhotoTapListener {
	private static final String EXTRAS_TITLE = "com.nasa.pic.app.activities.PhotoViewActivity.title";
	private static final String EXTRAS_DESCRIPTION = "com.nasa.pic.app.activities.PhotoViewActivity.description";
	private static final String EXTRAS_URL_TO_PHOTO = "com.nasa.pic.app.activities.PhotoViewActivity.url2photo";
	private static final String EXTRAS_DATE = "com.nasa.pic.app.activities.PhotoViewActivity.datetime";
	private static final String EXTRAS_TYPE = "com.nasa.pic.app.activities.PhotoViewActivity.type";
	/**
	 * Data-binding.
	 */
	private ActivityPhotoViewBinding mBinding;
	/**
	 * Main layout for this component.
	 */
	private static final int LAYOUT = R.layout.activity_photo_view;

	/**
	 * The menu to this view.
	 */
	private static final int MENU = R.menu.menu_photo_view;

	//[Begin for photo's info]
	private String mType;
	private String mTitle;
	private Date mDatetime;
	private String mUrl2Photo;
	private String mDescription;
	//[End]

	/**
	 * A tinyurl to the {@link com.nasa.pic.ds.PhotoDB}.
	 */
	private String mSharedUrl;


	public static void showInstance(Context cxt, String title, String description, String urlToPhoto, Date datetime, String type) {
		Intent intent = new Intent(cxt, PhotoViewActivity.class);
		intent.putExtra(EXTRAS_TYPE, type);
		intent.putExtra(EXTRAS_TITLE, title);
		intent.putExtra(EXTRAS_DATE, datetime);
		intent.putExtra(EXTRAS_DESCRIPTION, description);
		intent.putExtra(EXTRAS_URL_TO_PHOTO, urlToPhoto);
		intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
		cxt.startActivity(intent);
	}


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(LAYOUT);

		if (savedInstanceState != null) {
			mType =  savedInstanceState.getString(EXTRAS_TYPE);
			mTitle = savedInstanceState.getString(EXTRAS_TITLE);
			mUrl2Photo = savedInstanceState.getString(EXTRAS_URL_TO_PHOTO);
			mDescription = savedInstanceState.getString(EXTRAS_DESCRIPTION);
			mDatetime = (Date) savedInstanceState.getSerializable(EXTRAS_DATE);
		} else {
			Intent intent = getIntent();
			mType =  intent.getStringExtra(EXTRAS_TYPE);
			mTitle = intent.getStringExtra(EXTRAS_TITLE);
			mUrl2Photo = intent.getStringExtra(EXTRAS_URL_TO_PHOTO);
			mDescription = intent.getStringExtra(EXTRAS_DESCRIPTION);
			mDatetime = (Date) intent.getSerializableExtra(EXTRAS_DATE);
		}

		mBinding = DataBindingUtil.setContentView(this, LAYOUT);
		setUpErrorHandling((ViewGroup) findViewById(R.id.error_content));

		setSupportActionBar(mBinding.toolbar);
		getSupportActionBar().setTitle("");
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		mBinding.bigImgIv.setOnPhotoTapListener(this);
		mBinding.bigImgIv.setZoomable(true);

		mBinding.descriptionTv.setText(mDescription);
		mBinding.descriptionTv.setTextColor(Color.WHITE);
		String datetime = DateTimeUtils.timeConvert2(App.Instance, mDatetime.getTime());
		mBinding.datetimeTv.setText(String.format(getString(R.string.lbl_photo_datetime_prefix), datetime));
		mBinding.datetimeTv.setTextColor(Color.WHITE);
		mBinding.setType(mType);
		initPull2Load(mBinding.contentSrl);
		loadImage();
	}


	protected void initPull2Load(SwipeRefreshLayout swipeRefreshLayout) {
		int actionbarHeight = Utils.getActionBarHeight(App.Instance);
		swipeRefreshLayout.setOnRefreshListener(new OnRefreshListener() {
			@Override
			public void onRefresh() {
				loadImage();
			}
		});
		swipeRefreshLayout.setColorSchemeResources(R.color.c_refresh_1, R.color.c_refresh_2, R.color.c_refresh_3,
				R.color.c_refresh_4);
		swipeRefreshLayout.setProgressViewEndTarget(true, actionbarHeight * 2);
		swipeRefreshLayout.setProgressViewOffset(false, 0, actionbarHeight * 2);
		swipeRefreshLayout.setRefreshing(true);
	}


	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putSerializable(EXTRAS_TYPE, mType);
		outState.putSerializable(EXTRAS_TITLE, mTitle);
		outState.putSerializable(EXTRAS_DATE, mDatetime);
		outState.putString(EXTRAS_URL_TO_PHOTO, mUrl2Photo);
		outState.putString(EXTRAS_DESCRIPTION, mDescription);
	}


	/**
	 * Show remote image.
	 */
	private void loadImage() {
		AsyncTaskCompat.executeParallel(new AsyncTask<Object, Object, Bitmap>() {
			@Override
			protected void onPreExecute() {
				super.onPreExecute();
				mBinding.bigImgIv.setImageResource(R.drawable.placeholder);
			}

			@Override
			protected Bitmap doInBackground(Object... params) {
				try {
					return Picasso.with(App.Instance).load(Utils.uriStr2URI(mUrl2Photo).toASCIIString()).get();
				} catch (IOException e) {
					return null;
				}
			}

			@Override
			protected void onPostExecute(Bitmap bitmap) {
				super.onPostExecute(bitmap);
				if (bitmap != null) {
					mBinding.bigImgIv.setImageBitmap(bitmap);
				}
				mBinding.contentSrl.setRefreshing(false);
			}
		});
	}


	@Override
	public void onPhotoTap(View view, float v, float v1) {
	}


	@Override
	protected BasicPrefs getPrefs() {
		return Prefs.getInstance();
	}


	@Override
	public boolean onCreateOptionsMenu(final Menu menu) {
		getMenuInflater().inflate(MENU, menu);
		Call<Response> tinyUrlCall = Api.Retrofit.create(TinyUrl.class).getTinyUrl(mUrl2Photo);
		tinyUrlCall.enqueue(new Callback<Response>() {
			@Override
			public void onResponse(Call<Response> call, retrofit2.Response<Response> res) {
				if (res.isSuccess()) {
					Response response = res.body();
					mSharedUrl = !TextUtils.isEmpty(response.getResult()) ? response.getResult() : mUrl2Photo;
					buildFbShareMenu(menu);
				} else {
					onFailure(null, null);
				}
			}

			@Override
			public void onFailure(Call<Response> call, Throwable t) {
				mSharedUrl = mUrl2Photo;
				buildFbShareMenu(menu);
			}
		});
		return true;
	}

	private void buildFbShareMenu(Menu menu) {
		MenuItem menuShare = menu.findItem(R.id.action_share_photo);
		menuShare.setVisible(true);
		android.support.v7.widget.ShareActionProvider provider =
				(android.support.v7.widget.ShareActionProvider) MenuItemCompat.getActionProvider(menuShare);
		String text = App.Instance.getString(R.string.lbl_share_item_content, mDescription, mSharedUrl,
				Prefs.getInstance().getAppDownloadInfo());
		provider.setShareIntent(Utils.getDefaultShareIntent(provider, mTitle, text));
	}


	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case home:
			ActivityCompat.finishAfterTransition(this);
			break;
		case R.id.action_fb_share_photo:
			com.nasa.pic.utils.Utils.facebookShare(this, mTitle, mDescription, mUrl2Photo);
			break;
		}
		return super.onOptionsItemSelected(item);
	}
}
