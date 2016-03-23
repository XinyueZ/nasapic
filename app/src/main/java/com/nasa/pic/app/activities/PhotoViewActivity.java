package com.nasa.pic.app.activities;

import java.io.IOException;
import java.util.Date;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.customtabs.CustomTabsIntent;
import android.support.v4.content.ContextCompat;
import android.support.v4.os.AsyncTaskCompat;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import com.chopping.utils.DateTimeUtils;
import com.chopping.utils.Utils;
import com.nasa.pic.R;
import com.nasa.pic.app.App;
import com.nasa.pic.customtab.CustomTabActivityHelper;
import com.nasa.pic.customtab.CustomTabActivityHelper.ConnectionCallback;
import com.nasa.pic.customtab.WebViewFallback;
import com.nasa.pic.databinding.ActivityPhotoViewBinding;
import com.squareup.picasso.Picasso;

import uk.co.senab.photoview.PhotoViewAttacher.OnPhotoTapListener;


/**
 * Show big image as photo.
 *
 * @author Xinyue Zhao
 */
public final class PhotoViewActivity extends AppNormalActivity implements OnPhotoTapListener, ConnectionCallback {
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


	public static void showInstance(Context cxt, String title, String description, String urlToPhoto, Date datetime,
			String type) {
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

		mBinding = DataBindingUtil.setContentView(this, LAYOUT);
		setUpErrorHandling((ViewGroup) findViewById(R.id.error_content));
		initChromeCustomTabActivityHelper();

		setSupportActionBar(mBinding.toolbar);
		getSupportActionBar().setTitle(getPhotoTitle());
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		mBinding.bigImgIv.setOnPhotoTapListener(this);
		mBinding.bigImgIv.setZoomable(true);

		mBinding.descriptionTv.setText(getDescription());
		mBinding.descriptionTv.setTextColor(Color.WHITE);
		String datetime = DateTimeUtils.timeConvert2(App.Instance, getDatetime().getTime());
		mBinding.datetimeTv.setText(String.format(getString(R.string.lbl_photo_datetime_prefix), datetime));
		mBinding.datetimeTv.setTextColor(Color.WHITE);
		mBinding.setType(getType());

		initPull2Load(mBinding.contentSrl);
		mBinding.contentSrl.setOnRefreshListener(new OnRefreshListener() {
			@Override
			public void onRefresh() {
				loadImage();
			}
		});

		loadImage();

		if (!TextUtils.equals(getType(), "image")) {
			mBinding.playBtn.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					CustomTabsIntent customTabsIntent = new CustomTabsIntent.Builder().setToolbarColor(
							ContextCompat.getColor(PhotoViewActivity.this, R.color.colorPrimary)).setShowTitle(true)
							.setStartAnimations(PhotoViewActivity.this, android.R.anim.slide_in_left,
									android.R.anim.slide_out_right).setExitAnimations(PhotoViewActivity.this,
									android.R.anim.slide_in_left, android.R.anim.slide_out_right).build();
					mCustomTabActivityHelper.openCustomTab(PhotoViewActivity.this, customTabsIntent, getPhotoTitle(),
							getDescription(), getUrl2Photo(), getDatetime(), getType(), new WebViewFallback());
					//					new WebViewFallback().openUri(PhotoViewActivity.this, getPhotoTitle(),
					//														getDescription(), getUrl2Photo(), getDatetime(), getType());
				}
			});
		}
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
					return Picasso.with(App.Instance).load(Utils.uriStr2URI(getUrl2Photo()).toASCIIString()).get();
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
	protected int getMenuRes() {
		return MENU;
	}



	@Override
	protected void onStart() {
		super.onStart();
		mCustomTabActivityHelper.bindCustomTabsService(this);
	}

	@Override
	protected void onStop() {
		super.onStop();
		mCustomTabActivityHelper.unbindCustomTabsService(this);
		mBinding.playBtn.setEnabled(false);
	}


	private CustomTabActivityHelper mCustomTabActivityHelper;

	private void initChromeCustomTabActivityHelper() {
		mCustomTabActivityHelper = new CustomTabActivityHelper();
		mCustomTabActivityHelper.setConnectionCallback(this);
		mCustomTabActivityHelper.mayLaunchUrl(Uri.parse(getUrl2Photo()), null, null);
		mBinding.playBtn.setEnabled(false);
	}

	@Override
	public void onCustomTabsConnected() {
		mBinding.playBtn.setEnabled(true);
	}

	@Override
	public void onCustomTabsDisconnected() {
		mBinding.playBtn.setEnabled(false);
	}



}
