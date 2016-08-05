package com.nasa.pic.app.fragments;

import android.app.Dialog;
import android.content.DialogInterface;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.BottomSheetDialogFragment;
import android.view.View;
import android.widget.RadioGroup;

import com.nasa.pic.R;
import com.nasa.pic.app.App;
import com.nasa.pic.app.noactivities.wallpaper.CreateWallpaperDaily;
import com.nasa.pic.databinding.TimePlanBinding;
import com.nasa.pic.events.WallpaperDailyChangedEvent;
import com.nasa.pic.utils.Prefs;

import de.greenrobot.event.EventBus;


public final class DailyTimePlanBottomDialogFragment extends BottomSheetDialogFragment implements RadioGroup.OnCheckedChangeListener {

	private static final int LAYOUT = R.layout.fragment_bottom_dialog_daily_time_plan;
	private BottomSheetBehavior mBehavior;
	private TimePlanBinding mBinding;

	public static DailyTimePlanBottomDialogFragment newInstance() {
		return (DailyTimePlanBottomDialogFragment) DailyTimePlanBottomDialogFragment.instantiate(App.Instance, DailyTimePlanBottomDialogFragment.class.getName(), null);
	}

	@NonNull
	@Override
	public final Dialog onCreateDialog(Bundle savedInstanceState) {
		BottomSheetDialog dialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);
		View view = View.inflate(getContext(), LAYOUT, null);
		mBinding = DataBindingUtil.bind(view);
		mBinding.timePlanRp.check(getCurrentSelectedPlanCheckbox());
		mBinding.timePlanRp.setOnCheckedChangeListener(this);
		dialog.setContentView(view);
		mBehavior = BottomSheetBehavior.from((View) view.getParent());
		return dialog;
	}

	@Override
	public final void onStart() {
		super.onStart();
		mBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
	}


	private static
	@IdRes
	int getCurrentSelectedPlanCheckbox() {
		switch ((int) Prefs.getInstance()
		                   .getWallpaperDailyTimePlan()) {
			case 1:
				return R.id.time_plan_1;
			case 2:
				return R.id.time_plan_2;
			case 3:
				return R.id.time_plan_3;
			case 4:
				return R.id.time_plan_4;
			case 5:
				return R.id.time_plan_5;
			case 6:
				return R.id.time_plan_6;
			default:
				return R.id.time_plan_3;
		}
	}

	private static
	@StringRes
	int getCurrentSelectedPlanString() {
		switch ((int) Prefs.getInstance()
		                   .getWallpaperDailyTimePlan()) {
			case 1:
				return R.string.wallpaper_daily_time_plan_1_hour;
			case 2:
				return R.string.wallpaper_daily_time_plan_2_hour;
			case 3:
				return R.string.wallpaper_daily_time_plan_3_hour;
			case 4:
				return R.string.wallpaper_daily_time_plan_4_hour;
			case 5:
				return R.string.wallpaper_daily_time_plan_5_hour;
			case 6:
				return R.string.wallpaper_daily_time_plan_6_hour;
			default:
				return R.string.wallpaper_daily_time_plan_3_hour;
		}
	}

	private int getSelectedPlanIndex() {
		switch (mBinding.timePlanRp.getCheckedRadioButtonId()) {
			case R.id.time_plan_1:
				return 1;
			case R.id.time_plan_2:
				return 2;
			case R.id.time_plan_3:
				return 3;
			case R.id.time_plan_4:
				return 4;
			case R.id.time_plan_5:
				return 5;
			case R.id.time_plan_6:
				return 6;
			default:
				return 3;
		}
	}


	@Override
	public final void onDismiss(DialogInterface dialog) {
		super.onDismiss(dialog);
		CreateWallpaperDaily.setDailyUpdate(App.Instance, getSelectedPlanIndex() * Prefs.WALLPAPER_TIME_BASE);
		EventBus.getDefault()
		        .post(new WallpaperDailyChangedEvent(String.format(App.Instance.getString(R.string.wallpaper_update_frequency),
		                                                           App.Instance.getString(getCurrentSelectedPlanString()))));
	}

	@Override
	public final void onCheckedChanged(RadioGroup radioGroup, int i) {
		mBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
		Prefs.getInstance()
		     .setWallpaperDailyTimePlan(getSelectedPlanIndex());
	}
}
