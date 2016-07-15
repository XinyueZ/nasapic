package com.nasa.pic.app.fragments;


import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.os.ResultReceiver;
import android.support.v7.app.AppCompatDialogFragment;
import android.widget.DatePicker;

import com.nasa.pic.app.App;

import java.util.Calendar;

public final class DatePickerDialogFragment extends AppCompatDialogFragment {
	private static final String EXTRAS_LISTENER = DatePickerDialogFragment.class.getName() + ".EXTRAS.listener";
	public static final int RESULT_CODE = 0x09;
	public static final String EXTRAS_DAY_OF_MONTH = DatePickerDialogFragment.class.getName() + ".EXTRAS.dayOfMonth";
	public static final String EXTRAS_MONTH = DatePickerDialogFragment.class.getName() + ".EXTRAS.month";
	public static final String EXTRAS_YEAR = DatePickerDialogFragment.class.getName() + ".EXTRAS.year";

	public static DatePickerDialogFragment newInstance(ResultReceiver listener) {
		Bundle args = new Bundle(1);
		args.putParcelable(EXTRAS_LISTENER, listener);
		return (DatePickerDialogFragment) AppCompatDialogFragment.instantiate(App.Instance, DatePickerDialogFragment.class.getName(), args);
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		Calendar c = Calendar.getInstance();
		int dayOfMonth = c.get(Calendar.DAY_OF_MONTH);
		int month = c.get(Calendar.MONTH);
		int year = c.get(Calendar.YEAR);
		return new DatePickerDialog(getActivity(), new DatePickerDialog.OnDateSetListener() {
			@Override
			public void onDateSet(DatePicker datePicker, int year, int month, int dayOfMonth) {
				ResultReceiver resultReceiver = getArguments().getParcelable(EXTRAS_LISTENER);
				Bundle args = new Bundle(3);
				args.putInt(EXTRAS_YEAR, year);
				args.putInt(EXTRAS_MONTH, month);
				args.putInt(EXTRAS_DAY_OF_MONTH, dayOfMonth);
				if (resultReceiver != null) {
					resultReceiver.send(RESULT_CODE, args);
				}
			}
		}, year, month, dayOfMonth);
	}
}
