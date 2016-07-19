package com.nasa.pic.app.fragments;


import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.os.ResultReceiver;
import android.support.v7.app.AppCompatDialogFragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.nasa.pic.R;
import com.nasa.pic.app.App;
import com.nasa.pic.databinding.DatePickerBinding;
import com.nasa.pic.utils.Prefs;

import java.util.Calendar;

public final class DatePickerDialogFragment extends AppCompatDialogFragment {
	private static final String EXTRAS_LISTENER = DatePickerDialogFragment.class.getName() + ".EXTRAS.listener";
	public static final int RESULT_CODE = 0x09;
	public static final String EXTRAS_DAY_OF_MONTH = DatePickerDialogFragment.class.getName() + ".EXTRAS.dayOfMonth";
	public static final String EXTRAS_MONTH = DatePickerDialogFragment.class.getName() + ".EXTRAS.month";
	public static final String EXTRAS_YEAR = DatePickerDialogFragment.class.getName() + ".EXTRAS.year";
	public static final int IGNORED_DAY = -1;
	private DatePickerBinding mBinding;

	public static DatePickerDialogFragment newInstance(ResultReceiver listener) {
		Bundle args = new Bundle(1);
		args.putParcelable(EXTRAS_LISTENER, listener);
		return (DatePickerDialogFragment) AppCompatDialogFragment.instantiate(App.Instance, DatePickerDialogFragment.class.getName(), args);
	}

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_date_picker, container, false);
		return mBinding.getRoot();
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
				if (TextUtils.isEmpty(mBinding.yearEt.getText().toString()) || TextUtils.isEmpty(mBinding.monthEt.getText().toString()) ||
						(Integer.valueOf(mBinding.yearEt.getText().toString()) <= 0 || Integer.valueOf(mBinding.yearEt.getText().toString()) > thisYear) ||
						(Integer.valueOf(mBinding.monthEt.getText().toString()) <= 0 || Integer.valueOf(mBinding.monthEt.getText().toString()) > 12)) {
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
	}

}
