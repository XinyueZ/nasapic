package com.nasa.pic.app.activities;

import java.io.IOException;
import java.util.Date;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetBehavior.BottomSheetCallback;
import android.support.v4.app.ActivityCompat;
import android.support.v4.os.AsyncTaskCompat;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.chopping.activities.BaseActivity;
import com.chopping.application.BasicPrefs;
import com.chopping.utils.Utils;
import com.nasa.pic.R;
import com.nasa.pic.app.App;
import com.nasa.pic.databinding.ActivityPhotoViewBinding;
import com.nasa.pic.utils.Prefs;
import com.squareup.picasso.Picasso;

import uk.co.senab.photoview.PhotoViewAttacher.OnPhotoTapListener;

/**
 * Show big image as photo.
 *
 * @author Xinyue Zhao
 */
public final class PhotoViewActivity extends BaseActivity implements OnPhotoTapListener {
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


	//[Begin for photo's info]
	private Date mDatetime;
	private String mUrl2Photo;
	private String mDescription;
	//[End]

	public static void showInstance(Context cxt, String urlToPhoto, String description, Date datetime) {
		Intent intent = new Intent(cxt, PhotoViewActivity.class);
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
			mDatetime = (Date) savedInstanceState.getSerializable(EXTRAS_DATE);
			mUrl2Photo = savedInstanceState.getString(EXTRAS_URL_TO_PHOTO);
			mDescription = savedInstanceState.getString(EXTRAS_DESCRIPTION);
		} else {
			Intent intent = getIntent();
			mDatetime = (Date) intent.getSerializableExtra(EXTRAS_DATE);
			mUrl2Photo = intent.getStringExtra(EXTRAS_URL_TO_PHOTO);
			mDescription = intent.getStringExtra(EXTRAS_DESCRIPTION);
		}

		mBinding = DataBindingUtil.setContentView(this, LAYOUT);
		setUpErrorHandling((ViewGroup) findViewById(R.id.error_content));

		setSupportActionBar(mBinding.toolbar);
		getSupportActionBar().setTitle("");
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		mBinding.bigImgIv.setOnPhotoTapListener(this);
		mBinding.bigImgIv.setZoomable(true);

		mBinding.descriptionTv.setText(mDescription);


		BottomSheetBehavior behavior = BottomSheetBehavior.from(mBinding.bottomSheet);
		behavior.setBottomSheetCallback(new BottomSheetCallback() {
			@Override
			public void onStateChanged(@NonNull View bottomSheet, int newState) {
				switch (newState) {
				case BottomSheetBehavior.STATE_EXPANDED:
					if(getSupportActionBar().isShowing()) {
						getSupportActionBar().hide();
					}
					break;
				case BottomSheetBehavior.STATE_COLLAPSED:
					if(!getSupportActionBar().isShowing()) {
						getSupportActionBar().show();
					}
				}
			}

			@Override
			public void onSlide(@NonNull View bottomSheet, float slideOffset) {

			}
		});
		showImage();
	}


	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putSerializable(EXTRAS_DATE, mDatetime);
		outState.putString(EXTRAS_DESCRIPTION, mDescription);
		outState.putString(EXTRAS_URL_TO_PHOTO, mUrl2Photo);
	}


	/**
	 * Show remote image.
	 */
	private void showImage() {
		AsyncTaskCompat.executeParallel(new AsyncTask<Object, Object, Bitmap>() {
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
			}
		});
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		// Respond to the action bar's Up/Home button
		case android.R.id.home:
			ActivityCompat.finishAfterTransition(this);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}


	@Override
	public void onPhotoTap(View view, float v, float v1) {
		handleToolbar();
	}

	private void handleToolbar() {
		if(getSupportActionBar().isShowing()) {
			getSupportActionBar().hide();
		} else {
			getSupportActionBar().show();
		}
	}


	@Override
	protected BasicPrefs getPrefs() {
		return Prefs.getInstance();
	}
}
