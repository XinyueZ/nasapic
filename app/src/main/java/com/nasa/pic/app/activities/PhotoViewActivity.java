package com.nasa.pic.app.activities;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.customtabs.CustomTabsIntent;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.Snackbar;
import android.support.v4.animation.AnimatorCompatHelper;
import android.support.v4.animation.AnimatorUpdateListenerCompat;
import android.support.v4.animation.ValueAnimatorCompat;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPropertyAnimatorListenerAdapter;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.Interpolator;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
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
import com.nasa.pic.transition.BakedBezierInterpolator;
import com.nasa.pic.transition.Thumbnail;
import com.nasa.pic.transition.TransitCompat;

import java.io.Serializable;
import java.util.Date;

import uk.co.senab.photoview.PhotoViewAttacher.OnPhotoTapListener;

import static android.support.design.widget.BottomSheetBehavior.STATE_COLLAPSED;


/**
 * Show big image as photo.
 *
 * @author Xinyue Zhao
 */
public final class PhotoViewActivity extends AppNormalActivity implements OnPhotoTapListener,
                                                                          ConnectionCallback {
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

	private boolean mLarge;

	private TransitCompat mTransit;

	public static void showInstance(Context cxt, String title, String description, String urlToPhoto, String urlToPhotoFallback, Date datetime, String type, Thumbnail thumbnail) {
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


	public static void showInstance(Context cxt, String title, String description, String urlToPhoto, String urlToPhotoFallback, Date datetime, String type) {
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
		mLarge = getResources().getBoolean(R.bool.large);
		int orientation = getResources().getConfiguration().orientation;

		mBinding = DataBindingUtil.setContentView(this, LAYOUT);
		setUpErrorHandling((ViewGroup) findViewById(R.id.error_content));
		initChromeCustomTabActivityHelper();

		StringBuilder description;
		if (mLarge || orientation == Configuration.ORIENTATION_LANDSCAPE) {
			mBinding.toolbar.setTitle(getPhotoTitle());
			description = new StringBuilder(1);
			description.append(getDescription());
		} else {
			description = new StringBuilder(3);
			description.append(getPhotoTitle());
			description.append("\n....\n");
			description.append(getDescription());
		}
		initMenu(mBinding.toolbar);

		mBinding.bigImgIv.setOnPhotoTapListener(this);
		mBinding.bigImgIv.setZoomable(true);

		mBinding.descriptionTv.setText(description.toString());
		mBinding.descriptionTv.setTextColor(Color.WHITE);
		String datetime = DateTimeUtils.timeConvert2(App.Instance, getDatetime().getTime());
		mBinding.datetimeTv.setText(String.format(getString(R.string.lbl_photo_datetime_prefix), datetime));
		mBinding.datetimeTv.setTextColor(Color.WHITE);
		mBinding.setType(getType());


		loadImageWithTransaction(savedInstanceState);

		if (!TextUtils.equals(getType(), "image")) {
			mBinding.playBtn.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					PendingIntent pendingIntentFb = CustomTabActivityHelper.createPendingIntent(PhotoViewActivity.this, ActionBroadcastReceiver.ACTION_ACTION_BUTTON_1, getIntent());
					PendingIntent pendingIntentShare = CustomTabActivityHelper.createPendingIntent(PhotoViewActivity.this, ActionBroadcastReceiver.ACTION_ACTION_BUTTON_2, getIntent());

					CustomTabsIntent customTabsIntent = new CustomTabsIntent.Builder().setToolbarColor(ContextCompat.getColor(PhotoViewActivity.this, R.color.common_black))
					                                                                  .setShowTitle(true)
					                                                                  .setStartAnimations(PhotoViewActivity.this, android.R.anim.slide_in_left, android.R.anim.slide_out_right)
					                                                                  .setExitAnimations(PhotoViewActivity.this, android.R.anim.slide_in_left, android.R.anim.slide_out_right)
					                                                                  .addMenuItem(getString(R.string.action_share_fb), pendingIntentFb)
					                                                                  .addMenuItem(getString(R.string.action_share), pendingIntentShare)
					                                                                  .build();
					mCustomTabActivityHelper.openCustomTab(PhotoViewActivity.this,
					                                       customTabsIntent,
					                                       getPhotoTitle(),
					                                       getDescription(),
					                                       getUrl2Photo(),
					                                       getDatetime(),
					                                       getType(),
					                                       new WebViewFallback());
					//										new WebViewFallback().openUri(PhotoViewActivity.this, getPhotoTitle(),
					//																			getDescription(), getUrl2Photo(), getDatetime(), getType());
				}
			});
			mBinding.errorContent.setBackgroundResource(R.color.common_black);
		}
	}

	/**
	 * Load image and play animation transaction for init-create of the {@link PhotoViewActivity}.
	 *
	 * @param savedInstanceState If {@code null} that means init-create the animation will be played. However it must be down only when the
	 *                           {@link Thumbnail} is available. See the usage of {@link OpenPhotoEvent}.
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

						mBinding.bigImgIv.getViewTreeObserver()
						                 .removeOnPreDrawListener(this);

						ValueAnimatorCompat enterTogether = AnimatorCompatHelper.emptyValueAnimator();
						enterTogether.setDuration(TransitCompat.ANIM_DURATION * 2);
						enterTogether.addUpdateListener(new AnimatorUpdateListenerCompat() {
							private float old = 0;
							private float end = 255;
							private Interpolator interpolator2 = new BakedBezierInterpolator();

							@Override
							public void onAnimationUpdate(ValueAnimatorCompat animation) {
								float fraction = interpolator2.getInterpolation(animation.getAnimatedFraction());

								//Set background alpha
								float alpha = old + (fraction * (end - old));
								mBinding.errorContent.getBackground()
								                     .setAlpha((int) alpha);
							}
						});

						ValueAnimatorCompat exitTogether = AnimatorCompatHelper.emptyValueAnimator();
						exitTogether.setDuration(TransitCompat.ANIM_DURATION * 4);
						exitTogether.addUpdateListener(new AnimatorUpdateListenerCompat() {
							private float old = 255;
							private float end = 0;
							private Interpolator interpolator2 = new BakedBezierInterpolator();

							@Override
							public void onAnimationUpdate(ValueAnimatorCompat animation) {
								float fraction = interpolator2.getInterpolation(animation.getAnimatedFraction());

								//Set background alpha
								float alpha = old + (fraction * (end - old));
								mBinding.errorContent.getBackground()
								                     .setAlpha((int) alpha);
							}
						});


						mTransit = new TransitCompat.Builder().setThumbnail((Thumbnail) object)
						                                      .setTarget(mBinding.bigImgIv)
						                                      .setPlayTogetherAfterEnterTransition(enterTogether)
						                                      .setPlayTogetherBeforeExitTransition(exitTogether)
						                                      .build();
						mTransit.enter(new ViewPropertyAnimatorListenerAdapter() {
							@Override
							public void onAnimationStart(View view) {
								super.onAnimationStart(view);
								loadImage(getUrl2PhotoFallback());
							}
						});

						return true;
					}
				});
			} else {
				loadImage(getUrl2PhotoFallback());
			}
		} else {
			loadImage(getUrl2PhotoFallback());
		}
	}


	/**
	 * Show remote image.
	 */
	private void loadImage(final String path) {
		if (TextUtils.equals("image", getType())) {
			if (mTransit == null) {
				mBinding.bigImgIv.setImageResource(R.drawable.placeholder);
			}
			Glide.with(App.Instance)
			     .load(Utils.uriStr2URI(path)
			                .toASCIIString())
			     .diskCacheStrategy(DiskCacheStrategy.ALL)
			     .listener(new RequestListener<String, GlideDrawable>() {
				     @Override
				     public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
					     return false;
				     }

				     @Override
				     public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
					     //Important to set background from transparent to black.
					     mBinding.errorContent.setBackgroundResource(R.color.common_black);
					     return false;
				     }
			     })
			     .into(mBinding.bigImgIv);
		}
	}

	@Override
	public void onBackPressed() {
		BottomSheetBehavior behavior = BottomSheetBehavior.from(mBinding.bottomSheet);
		if (behavior.getState() != STATE_COLLAPSED) {
			behavior.setState(STATE_COLLAPSED);
			return;
		}

		if (mTransit != null) {
			mTransit.exit(new ViewPropertyAnimatorListenerAdapter() {
				@Override
				public void onAnimationEnd(View view) {
					super.onAnimationEnd(view);
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

	@Override
	protected void initMenu(Toolbar toolbar) {
		super.initMenu(toolbar);
		mBinding.toolbar.setNavigationOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				onBackPressed();
			}
		});
		SwitchCompat quSwitch = (SwitchCompat) findViewById(R.id.qu_switch);
		quSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				switchHd(isChecked);
			}
		});
	}

	private RequestListener<String, GlideDrawable> mHdSwitchListener = new RequestListener<String, GlideDrawable>() {
		@Override
		public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
			switchHd(false);
			Snackbar.make(mBinding.errorContent, R.string.error_switch_hd, Snackbar.LENGTH_SHORT)
			        .show();
			return false;
		}

		@Override
		public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
			mBinding.bigImgPb.setVisibility(View.GONE);
			return false;
		}
	};

	/**
	 * Change quality of photo when check switch on top-bar.
	 *
	 * @param isChecked Is cheched or not.
	 */
	private void switchHd(boolean isChecked) {
		mBinding.bigImgPb.setVisibility(View.VISIBLE);
		if (isChecked) {
			Glide.with(App.Instance)
			     .load(Utils.uriStr2URI(getUrl2Photo())
			                .toASCIIString())
			     .diskCacheStrategy(DiskCacheStrategy.ALL)
			     .listener(mHdSwitchListener)
			     .into(mBinding.bigImgIv);

			Snackbar.make(mBinding.errorContent, R.string.action_switch_hd, Snackbar.LENGTH_SHORT)
			        .show();
		} else {
			Glide.with(App.Instance)
			     .load(Utils.uriStr2URI(getUrl2PhotoFallback())
			                .toASCIIString())
			     .diskCacheStrategy(DiskCacheStrategy.ALL)
			     .listener(mHdSwitchListener)
			     .into(mBinding.bigImgIv);

			Snackbar.make(mBinding.errorContent, R.string.action_switch_normal, Snackbar.LENGTH_SHORT)
			        .show();
		}
	}

}
