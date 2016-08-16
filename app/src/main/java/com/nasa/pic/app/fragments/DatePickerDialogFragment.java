package com.nasa.pic.app.fragments;


import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.animation.AnimatorCompatHelper;
import android.support.v4.animation.AnimatorUpdateListenerCompat;
import android.support.v4.animation.ValueAnimatorCompat;
import android.support.v4.os.ResultReceiver;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPropertyAnimatorListenerAdapter;
import android.support.v7.app.AppCompatDialogFragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.Interpolator;

import com.google.common.eventbus.Subscribe;
import com.nasa.pic.R;
import com.nasa.pic.app.App;
import com.nasa.pic.databinding.DatePickerBinding;
import com.nasa.pic.events.CloseDatePickerDialogEvent;
import com.nasa.pic.events.PopupDatePickerDialogEvent;
import com.nasa.pic.transition.BakedBezierInterpolator;
import com.nasa.pic.transition.Thumbnail;
import com.nasa.pic.transition.TransitCompat;
import com.nasa.pic.utils.Prefs;

import java.util.Calendar;

import de.greenrobot.event.EventBus;

public final class DatePickerDialogFragment extends AppCompatDialogFragment {
	private static final String EXTRAS_LISTENER = DatePickerDialogFragment.class.getName() + ".EXTRAS.listener";
	public static final int RESULT_CODE = 0x09;
	public static final String EXTRAS_DAY_OF_MONTH = DatePickerDialogFragment.class.getName() + ".EXTRAS.dayOfMonth";
	public static final String EXTRAS_MONTH = DatePickerDialogFragment.class.getName() + ".EXTRAS.month";
	public static final String EXTRAS_YEAR = DatePickerDialogFragment.class.getName() + ".EXTRAS.year";
	private static final String EXTRAS_THUMBNAIL = DatePickerDialogFragment.class.getName() + ".EXTRAS.thumbnail";
	public static final int IGNORED_DAY = -1;
	private static final int LAYOUT = R.layout.fragment_date_picker;
	private DatePickerBinding mBinding;
	private TransitCompat mTransition;

	//------------------------------------------------
	//Subscribes, event-handlers
	//------------------------------------------------

	/**
	 * Handler for {@link CloseDatePickerDialogEvent}.
	 *
	 * @param e Event {@link CloseDatePickerDialogEvent}.
	 */
	@Subscribe
	public void onEvent(@SuppressWarnings("UnusedParameters") CloseDatePickerDialogEvent e) {
		closeThisFragment();
	}


	//------------------------------------------------

	public static DatePickerDialogFragment newInstance(ResultReceiver listener, Thumbnail thumbnail) {
		Bundle args = new Bundle(1);
		args.putParcelable(EXTRAS_LISTENER, listener);
		args.putSerializable(EXTRAS_THUMBNAIL, thumbnail);
		return (DatePickerDialogFragment) AppCompatDialogFragment.instantiate(App.Instance, DatePickerDialogFragment.class.getName(), args);
	}

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		mBinding = DataBindingUtil.inflate(inflater, LAYOUT, container, false);
		return mBinding.getRoot();
	}

	@Override
	public void onResume() {
		super.onResume();
		EventBus.getDefault()
		        .register(this);
	}

	@Override
	public void onPause() {
		EventBus.getDefault()
		        .unregister(this);
		super.onPause();
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		Prefs prefs = Prefs.getInstance();
		mBinding.yearEt.setText(prefs.getSearchYear());
		mBinding.monthEt.setText(prefs.getSearchMonth());
		mBinding.dayEt.setText(Integer.valueOf(prefs.getSearchDay()) == IGNORED_DAY ?
		                       "" :
		                       prefs.getSearchDay());
		mBinding.searchBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {

				int thisYear = Calendar.getInstance()
				                       .get(Calendar.YEAR);
				if (TextUtils.isEmpty(mBinding.yearEt.getText()
				                                     .toString()) || TextUtils.isEmpty(mBinding.monthEt.getText()
				                                                                                       .toString()) ||
						(Integer.valueOf(mBinding.yearEt.getText()
						                                .toString()) <= 0 || Integer.valueOf(mBinding.yearEt.getText()
						                                                                                    .toString()) > thisYear) ||
						(Integer.valueOf(mBinding.monthEt.getText()
						                                 .toString()) <= 0 || Integer.valueOf(mBinding.monthEt.getText()
						                                                                                      .toString()) > 12)) {
					mBinding.warningTv.setVisibility(View.VISIBLE);
					mBinding.warningTv.setText(R.string.lbl_year_month_day_wrong);
				} else {
					int year = Integer.valueOf(mBinding.yearEt.getText()
					                                          .toString());
					int month = Integer.valueOf(mBinding.monthEt.getText()
					                                            .toString());
					ResultReceiver resultReceiver = getArguments().getParcelable(EXTRAS_LISTENER);
					Bundle args = new Bundle(3);
					int day = TextUtils.isEmpty(mBinding.dayEt.getText()) || Integer.valueOf(mBinding.dayEt.getText()
					                                                                                       .toString()) <= 0 ?
					          IGNORED_DAY :
					          Integer.valueOf(mBinding.dayEt.getText()
					                                        .toString());
					args.putInt(EXTRAS_YEAR, year);
					args.putInt(EXTRAS_MONTH, month);
					args.putInt(EXTRAS_DAY_OF_MONTH, day);
					if (resultReceiver != null) {
						resultReceiver.send(RESULT_CODE, args);
					}

					Prefs prefs = Prefs.getInstance();
					prefs.setSearchYear(String.valueOf(year));
					prefs.setSearchMonth(String.valueOf(month));
					prefs.setSearchDay(String.valueOf(day));
					dismiss();
				}
			}
		});

		transitCompat();
	}


	private void transitCompat() {
		Object thumbnail = getArguments().getSerializable(EXTRAS_THUMBNAIL);
		if (thumbnail != null) {
			// Only run the animation if we're coming from the parent activity, not if
			// we're recreated automatically by the window manager (e.g., device rotation)
			ViewTreeObserver observer = mBinding.getRoot()
			                                    .getViewTreeObserver();
			observer.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
				@Override
				public boolean onPreDraw() {
					Thumbnail thumbnail = (Thumbnail)getArguments().getSerializable(EXTRAS_THUMBNAIL);
					mBinding.getRoot()
					        .getViewTreeObserver()
					        .removeOnPreDrawListener(this);

					ValueAnimatorCompat beforeExitAnimator = AnimatorCompatHelper.emptyValueAnimator();
					beforeExitAnimator.setDuration(TransitCompat.ANIM_DURATION);
					final Interpolator interpolator2 = new BakedBezierInterpolator();
					beforeExitAnimator.addUpdateListener(new AnimatorUpdateListenerCompat() {
						private final float old = 1;
						private final float end = 0;
						private Thumbnail thumbnail = (Thumbnail)getArguments().getSerializable(EXTRAS_THUMBNAIL);

						@Override
						public void onAnimationUpdate(ValueAnimatorCompat animation) {
							float fraction = interpolator2.getInterpolation(animation.getAnimatedFraction());

							//Set height
							float cur = old + (fraction * (end - old));
							ViewCompat.setPivotX(mBinding.getRoot(),  mBinding.getRoot().getRight() - thumbnail.getWidth() );
							ViewCompat.setPivotY(mBinding.getRoot(), mBinding.getRoot().getBottom() - thumbnail.getHeight() );
							ViewCompat.setScaleX(mBinding.getRoot(), cur);
							ViewCompat.setScaleY(mBinding.getRoot(), cur);
						}
					});

					mTransition = new TransitCompat.Builder().setThumbnail( thumbnail)
					                                         .setTarget(mBinding.getRoot())
					                                         .setPlayTogetherBeforeExitTransition(beforeExitAnimator)
					                                         .build();

					mTransition.enter(new ViewPropertyAnimatorListenerAdapter());
					return true;
				}
			});
		}
	}

	private void closeThisFragment() {
		if (mTransition != null) {
			mTransition.exit(new ViewPropertyAnimatorListenerAdapter() {
				@Override
				public void onAnimationEnd(View v) {
					super.onAnimationEnd(v);
					EventBus.getDefault()
					        .post(new PopupDatePickerDialogEvent());
				}
			});
		} else {
			EventBus.getDefault()
			        .post(new PopupDatePickerDialogEvent());
		}
	}
}
