package com.nasa.pic.app.activities;

import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
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
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.view.ViewPropertyAnimatorListenerAdapter;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDialogFragment;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.Interpolator;
import android.widget.CheckedTextView;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.RadioGroup;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.chopping.utils.DateTimeUtils;
import com.chopping.utils.Utils;
import com.google.common.eventbus.Subscribe;
import com.nasa.pic.R;
import com.nasa.pic.app.App;
import com.nasa.pic.customtab.ActionBroadcastReceiver;
import com.nasa.pic.customtab.CustomTabActivityHelper;
import com.nasa.pic.customtab.CustomTabActivityHelper.ConnectionCallback;
import com.nasa.pic.customtab.WebViewFallback;
import com.nasa.pic.databinding.ActivityPhotoViewBinding;
import com.nasa.pic.events.CloseDialogEvent;
import com.nasa.pic.events.OpenPhotoEvent;
import com.nasa.pic.transition.BakedBezierInterpolator;
import com.nasa.pic.transition.Thumbnail;
import com.nasa.pic.transition.TransitCompat;
import com.nasa.pic.utils.Prefs;

import java.util.Date;

import de.greenrobot.event.EventBus;
import uk.co.senab.photoview.PhotoViewAttacher.OnPhotoTapListener;

import static android.support.design.widget.BottomSheetBehavior.STATE_COLLAPSED;


/**
 * Show big image as photo.
 *
 * @author Xinyue Zhao
 */
public final class PhotoViewActivity extends AppNormalActivity implements OnPhotoTapListener,
                                                                          ConnectionCallback,
                                                                          RadioGroup.OnCheckedChangeListener {
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

	private TransitCompat mTransit;
	private CompoundButton mQuSwitch;

	//------------------------------------------------
	//Subscribes, event-handlers
	//------------------------------------------------

	/**
	 * Handler for {@link  CloseDialogEvent}.
	 *
	 * @param e Event {@link CloseDialogEvent}.
	 */
	@Subscribe
	public void onEvent(CloseDialogEvent e) {
		mBinding.wallpaperSettingLayout.wallpaperChangeDailyCtv.setChecked(Prefs.getInstance()
		                                                                        .doesWallpaperChangeDaily());
	}
	//------------------------------------------------

	public static void showInstance(Context cxt, String title, String description, String urlToPhoto, String urlToPhotoFallback, Date datetime, String type, Thumbnail thumbnail) {
		Intent intent = new Intent(cxt, PhotoViewActivity.class);
		intent.putExtra(EXTRAS_TYPE, type);
		intent.putExtra(EXTRAS_TITLE, title);
		intent.putExtra(EXTRAS_DATE, datetime);
		intent.putExtra(EXTRAS_DESCRIPTION, description);
		intent.putExtra(EXTRAS_URL_TO_PHOTO, urlToPhoto);
		intent.putExtra(EXTRAS_URL_TO_PHOTO_FALLBACK, urlToPhotoFallback);
		intent.putExtra(EXTRAS_THUMBNAIL, thumbnail);
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
		boolean large = getResources().getBoolean(R.bool.large);
		int orientation = getResources().getConfiguration().orientation;

		mBinding = DataBindingUtil.setContentView(this, LAYOUT);
		mBinding.loadingFab.hide();
		setUpErrorHandling((ViewGroup) findViewById(R.id.error_content));
		initChromeCustomTabActivityHelper();


		StringBuilder description;
		if (large || orientation == Configuration.ORIENTATION_LANDSCAPE) {
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
		mBinding.hdSizeMultiplierLayout.sizeMultiplierRp.setOnCheckedChangeListener(this);

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
					CustomTabActivityHelper.openCustomTab(PhotoViewActivity.this, customTabsIntent, getPhotoTitle(), getDescription(), getUrl2Photo(), getDatetime(), getType(), new WebViewFallback
							());
				}
			});
			mBinding.errorContent.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.common_black, null));
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
							private final float old = 0;
							private final float end = 255;
							private final Interpolator interpolator2 = new BakedBezierInterpolator();

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
							private final float old = 255;
							private final float end = 0;
							private final Interpolator interpolator2 = new BakedBezierInterpolator();

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
			     .skipMemoryCache(false)
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
		if (mQuSwitch.isChecked()) {
			mBinding.hdSizeMultiplierLayout.getRoot()
			                               .setVisibility(mBinding.hdSizeMultiplierLayout.getRoot()
			                                                                             .getVisibility() != View.VISIBLE ?
			                                              View.VISIBLE :
			                                              View.GONE);
		}
		mBinding.wallpaperSettingLayout.getRoot()
		                               .setVisibility(mBinding.wallpaperSettingLayout.getRoot()
		                                                                             .getVisibility() != View.VISIBLE ?
		                                              View.VISIBLE :
		                                              View.GONE);
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
		mQuSwitch = (CompoundButton) findViewById(R.id.qu_switch);
		if (TextUtils.equals(getType(), "image")) {
			mQuSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					switchHd(isChecked);
					mBinding.hdSizeMultiplierLayout.getRoot()
					                               .setVisibility(isChecked ?
					                                              View.VISIBLE :
					                                              View.GONE);
					mBinding.wallpaperSettingLayout.getRoot()
					                               .setVisibility(isChecked ?
					                                              View.VISIBLE :
					                                              View.GONE);
				}
			});
		} else {
			mBinding.toolbar.getMenu()
			                .findItem(R.id.action_hd_switch)
			                .setVisible(false);
		}
	}

	private final RequestListener<String, GlideDrawable> mHdSwitchListener = new RequestListener<String, GlideDrawable>() {
		@Override
		public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
			stopLoadingIndicator();
			switchHd(false);
			Snackbar.make(mBinding.errorContent, R.string.error_switch_hd, Snackbar.LENGTH_SHORT)
			        .show();
			return false;
		}

		@Override
		public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
			stopLoadingIndicator();
			return false;
		}
	};

	/**
	 * Change quality of photo when check switch on top-bar.
	 *
	 * @param isChecked Is cheched or not.
	 */
	private void switchHd(boolean isChecked) {
		if (isChecked) {
			startLoadingIndicator();
			Glide.with(App.Instance)
			     .load(Utils.uriStr2URI(getUrl2Photo())
			                .toASCIIString())
			     .diskCacheStrategy(DiskCacheStrategy.ALL)
			     .sizeMultiplier(getHdFactor())
			     .skipMemoryCache(true)
			     .listener(mHdSwitchListener)
			     .into(mBinding.bigImgIv);

		} else {
			Glide.with(App.Instance)
			     .load(Utils.uriStr2URI(getUrl2PhotoFallback())
			                .toASCIIString())
			     .diskCacheStrategy(DiskCacheStrategy.ALL)
			     .skipMemoryCache(false)
			     .listener(mHdSwitchListener)
			     .into(mBinding.bigImgIv);

		}
	}


	private void startLoadingIndicator() {
		Drawable drawable = mBinding.loadingFab.getDrawable();
		if (drawable instanceof Animatable) {
			((Animatable) drawable).start();
		}
		mBinding.loadingFab.show();
	}

	private void stopLoadingIndicator() {
		Drawable drawable = mBinding.loadingFab.getDrawable();
		if (drawable instanceof Animatable) {
			((Animatable) drawable).stop();
		}
		mBinding.loadingFab.hide();
	}

	@Override
	public void onCheckedChanged(RadioGroup radioGroup, int id) {
		Glide.with(App.Instance)
		     .load(Utils.uriStr2URI(getUrl2Photo())
		                .toASCIIString())
		     .diskCacheStrategy(DiskCacheStrategy.ALL)
		     .sizeMultiplier(getHdFactor())
		     .skipMemoryCache(true)
		     .listener(mHdSwitchListener)
		     .into(mBinding.bigImgIv);
		mBinding.hdSizeMultiplierLayout.getRoot()
		                               .setVisibility(View.GONE);
		mBinding.wallpaperSettingLayout.getRoot()
		                               .setVisibility(View.GONE);
	}

	private float getHdFactor() {
		int index = 1;
		switch (mBinding.hdSizeMultiplierLayout.sizeMultiplierRp.getCheckedRadioButtonId()) {
			case R.id.size_multiplier_0:
				index = 0;
				break;
			case R.id.size_multiplier_1:
				index = 1;
				break;
			case R.id.size_multiplier_2:
				index = 2;
				break;
			case R.id.size_multiplier_3:
				index = 3;
				break;
		}

		int[] multipliers = getResources().getIntArray(R.array.size_multipliers);
		return multipliers[index] / 100f;
	}

	public void wallpaperChangeDaily(View view) {
		CheckedTextView checkedTextView = (CheckedTextView) view;
		updateWallpaperSetting(checkedTextView);
	}

	private static void updateWallpaperSetting(CheckedTextView checkedTextView) {
		Prefs prefs = Prefs.getInstance();
		checkedTextView.setChecked(!prefs.doesWallpaperChangeDaily());
		prefs.setWallpaperChangeDaily(checkedTextView.isChecked());
	}

	public void setAsWallpaper(View view) {
		Prefs prefs = Prefs.getInstance();
		if (prefs.doesWallpaperChangeDaily()) {
			com.nasa.pic.utils.Utils.showDialogFragment(getSupportFragmentManager(), AsWallpaperDialogFragment.newInstance(App.Instance), null);
		}
	}


	public static class AsWallpaperDialogFragment extends AppCompatDialogFragment {
		public static AsWallpaperDialogFragment newInstance(Context cxt) {
			return (AsWallpaperDialogFragment) AsWallpaperDialogFragment.instantiate(cxt, AsWallpaperDialogFragment.class.getName());
		}

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			return new AlertDialog.Builder(getActivity()).setTitle(R.string.wallpaper_as)
			                                             .setMessage(R.string.wallpaper_change_daily_cancel)
			                                             .setPositiveButton(R.string.btn_yes, new DialogInterface.OnClickListener() {
				                                             public void onClick(DialogInterface dialog, int whichButton) {

				                                             }
			                                             })
			                                             .setNegativeButton(R.string.btn_no, new DialogInterface.OnClickListener() {
				                                             public void onClick(DialogInterface dialog, int whichButton) {
					                                             Prefs.getInstance()
					                                                  .setWallpaperChangeDaily(false);
				                                             }
			                                             })
			                                             .setCancelable(false)
			                                             .create();
		}

		@Override
		public void onDismiss(DialogInterface dialog) {
			super.onDismiss(dialog);
			EventBus.getDefault()
			        .post(new CloseDialogEvent());
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		mBinding.wallpaperSettingLayout.wallpaperChangeDailyCtv.setChecked(Prefs.getInstance()
		                                                                        .doesWallpaperChangeDaily());
	}
}
