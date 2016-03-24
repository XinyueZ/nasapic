package com.nasa.pic.app.activities;

import java.io.IOException;
import java.io.Serializable;
import java.util.Date;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.customtabs.CustomTabsIntent;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.os.AsyncTaskCompat;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import com.chopping.utils.DateTimeUtils;
import com.chopping.utils.Utils;
import com.nasa.pic.R;
import com.nasa.pic.app.App;
import com.nasa.pic.customtab.ActionBroadcastReceiver;
import com.nasa.pic.customtab.CustomTabActivityHelper;
import com.nasa.pic.customtab.CustomTabActivityHelper.ConnectionCallback;
import com.nasa.pic.customtab.WebViewFallback;
import com.nasa.pic.databinding.ActivityPhotoViewBinding;
import com.nasa.pic.events.OpenPhotoEvent;
import com.nasa.pic.transaction.Thumbnail;
import com.nasa.pic.transaction.Transaction;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorListenerAdapter;
import com.squareup.picasso.Picasso;

import uk.co.senab.photoview.PhotoViewAttacher.OnPhotoTapListener;


/**
 * Show big image as photo.
 *
 * @author Xinyue Zhao
 */
public final class PhotoViewActivity extends AppNormalActivity implements OnPhotoTapListener, ConnectionCallback {
	private static final String EXTRAS_THUMBNAIL = PhotoViewActivity.class.getName() + ".EXTRAS.thumbnail";
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

	private Transaction mTransaction;

	public static void showInstance(Context cxt, String title, String description, String urlToPhoto,
			String urlToPhotoFallback, Date datetime, String type, Thumbnail thumbnail) {
		Intent intent = new Intent(cxt, PhotoViewActivity.class);
		intent.putExtra(EXTRAS_TYPE, type);
		intent.putExtra(EXTRAS_TITLE, title);
		intent.putExtra(EXTRAS_DATE, datetime);
		intent.putExtra(EXTRAS_DESCRIPTION, description);
		intent.putExtra(EXTRAS_URL_TO_PHOTO, urlToPhoto);
		intent.putExtra(EXTRAS_URL_TO_PHOTO_FALLBACK, urlToPhotoFallback);
		intent.putExtra(EXTRAS_THUMBNAIL, (Serializable) thumbnail);
		intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
		cxt.startActivity(intent);
	}


	public static void showInstance(Context cxt, String title, String description, String urlToPhoto,String urlToPhotoFallback,  Date datetime,
			String type) {
		Intent intent = new Intent(cxt, PhotoViewActivity.class);
		intent.putExtra(EXTRAS_TYPE, type);
		intent.putExtra(EXTRAS_TITLE, title);
		intent.putExtra(EXTRAS_DATE, datetime);
		intent.putExtra(EXTRAS_DESCRIPTION, description);
		intent.putExtra(EXTRAS_URL_TO_PHOTO, urlToPhoto);
		intent.putExtra(EXTRAS_URL_TO_PHOTO_FALLBACK, urlToPhotoFallback);
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
		mBinding.thumbnailImgIv.setOnPhotoTapListener(this);
		mBinding.thumbnailImgIv.setZoomable(true);

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

		loadImageWithTransaction(savedInstanceState);

		if (!TextUtils.equals(getType(), "image")) {
			mBinding.playBtn.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					PendingIntent pendingIntentFb = CustomTabActivityHelper.createPendingIntent(PhotoViewActivity.this,
							ActionBroadcastReceiver.ACTION_ACTION_BUTTON_1, getIntent());
					PendingIntent pendingIntentShare = CustomTabActivityHelper.createPendingIntent(
							PhotoViewActivity.this, ActionBroadcastReceiver.ACTION_ACTION_BUTTON_2, getIntent());

					CustomTabsIntent customTabsIntent = new CustomTabsIntent.Builder().setToolbarColor(
							ContextCompat.getColor(PhotoViewActivity.this, R.color.colorPrimary)).setShowTitle(true)
							.setStartAnimations(PhotoViewActivity.this, android.R.anim.slide_in_left,
									android.R.anim.slide_out_right).setExitAnimations(PhotoViewActivity.this,
									android.R.anim.slide_in_left, android.R.anim.slide_out_right).addMenuItem(
									getString(R.string.action_share_fb), pendingIntentFb).addMenuItem(
									getString(R.string.action_share), pendingIntentShare).build();
					mCustomTabActivityHelper.openCustomTab(PhotoViewActivity.this, customTabsIntent, getPhotoTitle(),
							getDescription(), getUrl2Photo(), getDatetime(), getType(), new WebViewFallback());
					//					new WebViewFallback().openUri(PhotoViewActivity.this, getPhotoTitle(),
					//														getDescription(), getUrl2Photo(), getDatetime(), getType());
				}
			});
		}
	}

	/**
	 * Load image and play animation transaction for init-create of the {@link PhotoViewActivity}.
	 *
	 * @param savedInstanceState
	 * 		If {@code null} that means init-create the animation will be played. However it must be down only when the
	 * 		{@link Thumbnail} is available. See the usage of {@link OpenPhotoEvent}.
	 */
	private void loadImageWithTransaction(Bundle savedInstanceState) {
		Intent intent = getIntent();
		Object object = intent.getSerializableExtra(EXTRAS_THUMBNAIL);
		if (object != null) {
			// Only run the animation if we're coming from the parent activity, not if
			// we're recreated automatically by the window manager (e.g., device rotation)
			if (savedInstanceState == null) {
				ViewTreeObserver observer = mBinding.bigImgIv.getViewTreeObserver();
				observer.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
					@Override
					public boolean onPreDraw() {
						Intent intent = getIntent();
						Object object = intent.getSerializableExtra(EXTRAS_THUMBNAIL);

						mBinding.bigImgIv.getViewTreeObserver().removeOnPreDrawListener(this);
						mTransaction = new Transaction.Builder().setThumbnail((Thumbnail) object).setTarget(
								mBinding.thumbnailImgIv).build(PhotoViewActivity.this);
						mTransaction.enterAnimation(new AnimatorListenerAdapter() {
							@Override
							public void onAnimationEnd(Animator animation) {
								super.onAnimationEnd(animation);
								loadImage();
							}
						});

						return true;
					}
				});
			} else {
				loadImage();
			}
		} else {
			loadImage();
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
				if (mTransaction == null) {
					mBinding.bigImgIv.setImageResource(R.drawable.placeholder);
				}
			}

			@Override
			protected Bitmap doInBackground(Object... params) {
				try {
					return Picasso.with(App.Instance).load(Utils.uriStr2URI(getUrl2Photo()).toASCIIString()).get();
				} catch (OutOfMemoryError | IOException e) {
					try {
						return Picasso.with(App.Instance).load(Utils.uriStr2URI(getUrl2PhotoFallback()).toASCIIString())
								.get();
					} catch (IOException e1) {
						return null;
					}
				}
			}

			@Override
			protected void onPostExecute(Bitmap bitmap) {
				super.onPostExecute(bitmap);
				if (bitmap != null) {
					mBinding.bigImgIv.setImageBitmap(bitmap);
					mBinding.thumbnailImgIv.setVisibility(View.GONE);
				}
				mBinding.contentSrl.setRefreshing(false);
			}
		});
	}

	@Override
	public void onBackPressed() {
		if (mTransaction != null) {
			mBinding.bigImgIv.setVisibility(View.GONE);
			mBinding.thumbnailImgIv.setVisibility(View.VISIBLE);
			mTransaction.exitAnimation(new AnimatorListenerAdapter() {
				@Override
				public void onAnimationEnd(Animator animation) {
					super.onAnimationEnd(animation);
					ActivityCompat.finishAfterTransition(PhotoViewActivity.this);
				}
			});
		} else {
			super.onBackPressed();
		}
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
