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
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
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
	private String mTitle;
	private Date mDatetime;
	private String mUrl2Photo;
	private String mDescription;
	//[End]

	public static void showInstance(Context cxt,  String title, String description,String urlToPhoto, Date datetime) {
		Intent intent = new Intent(cxt, PhotoViewActivity.class);
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
			mTitle = savedInstanceState.getString(EXTRAS_TITLE);
			mUrl2Photo = savedInstanceState.getString(EXTRAS_URL_TO_PHOTO);
			mDescription = savedInstanceState.getString(EXTRAS_DESCRIPTION);
			mDatetime = (Date) savedInstanceState.getSerializable(EXTRAS_DATE);
		} else {
			Intent intent = getIntent();
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
		outState.putSerializable(EXTRAS_TITLE, mTitle);
		outState.putSerializable(EXTRAS_DATE, mDatetime);
		outState.putString(EXTRAS_DESCRIPTION, mDescription);
		outState.putString(EXTRAS_URL_TO_PHOTO, mUrl2Photo);
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
					return Picasso.with(App.Instance).load(Utils.uriStr2URI(mUrl2Photo).toASCIIString()).placeholder(
							R.drawable.placeholder).get();
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
		return true;
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
