package com.nasa.pic.app.fragments;


import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.os.ResultReceiver;
import android.support.v7.app.AppCompatDialogFragment;
import android.support.v7.view.ContextThemeWrapper;
import android.view.View;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.NumberPicker;

import com.nasa.pic.app.App;

import java.util.Calendar;

public final class MonthPickerDialogFragment extends AppCompatDialogFragment {
	private static final String EXTRAS_LISTENER = MonthPickerDialogFragment.class.getName() + ".EXTRAS.listener";
	public static final int RESULT_CODE = 0x09;
	public static final String EXTRAS_MONTH = MonthPickerDialogFragment.class.getName() + ".EXTRAS.month";
	public static final String EXTRAS_YEAR = MonthPickerDialogFragment.class.getName() + ".EXTRAS.year";

	public static MonthPickerDialogFragment newInstance(ResultReceiver listener) {
		Bundle args = new Bundle(1);
		args.putParcelable(EXTRAS_LISTENER, listener);
		return (MonthPickerDialogFragment) AppCompatDialogFragment.instantiate(App.Instance, MonthPickerDialogFragment.class.getName(), args);
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		Calendar c = Calendar.getInstance();
		int dayOfMonth = c.get(Calendar.DAY_OF_MONTH);
		int month = c.get(Calendar.MONTH);
		int year = c.get(Calendar.YEAR);
		DatePickerDialog dlg = new DatePickerDialog(new ContextThemeWrapper(getActivity(), android.R.style.Theme_Holo_Dialog_NoActionBar), new DatePickerDialog.OnDateSetListener() {
			@Override
			public void onDateSet(DatePicker datePicker, int year, int month, int dayOfMonth) {
				ResultReceiver resultReceiver = getArguments().getParcelable(EXTRAS_LISTENER);
				Bundle args = new Bundle(2);
				args.putInt(EXTRAS_YEAR, year);
				args.putInt(EXTRAS_MONTH, month + 1);
				if (resultReceiver != null) {
					resultReceiver.send(RESULT_CODE, args);
				}
			}
		}, year, month, dayOfMonth) {
			@Override
			protected void onCreate(Bundle savedInstanceState) {
				super.onCreate(savedInstanceState);
				LinearLayout mSpinners = (LinearLayout) findViewById(getContext().getResources()
				                                                                 .getIdentifier("android:id/pickers", null, null));
				if (mSpinners != null) {
					NumberPicker mMonthSpinner = (NumberPicker) findViewById(getContext().getResources()
					                                                                     .getIdentifier("android:id/month", null, null));
					NumberPicker mYearSpinner = (NumberPicker) findViewById(getContext().getResources()
					                                                                    .getIdentifier("android:id/year", null, null));
					mSpinners.removeAllViews();
					if (mMonthSpinner != null) {
						mSpinners.addView(mMonthSpinner);
					}
					if (mYearSpinner != null) {
						mSpinners.addView(mYearSpinner);
					}
				}
				View dayPickerView = findViewById(getContext().getResources()
				                                              .getIdentifier("android:id/day", null, null));
				if (dayPickerView != null) {
					dayPickerView.setVisibility(View.GONE);
				}
			}
		};
		return dlg;
	}
}
