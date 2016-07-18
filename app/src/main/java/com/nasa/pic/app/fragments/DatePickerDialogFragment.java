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

		mBinding.searchBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				int year = Integer.valueOf(mBinding.yearEt.getText()
				                                          .toString());
				int month = Integer.valueOf(mBinding.monthEt.getText()
				                                            .toString());

				if (year <= 0 || month <= 0) {
					mBinding.warningTv.setVisibility(View.VISIBLE);
					mBinding.warningTv.setText(R.string.lbl_year_month_day_wrong);
				} else {
					ResultReceiver resultReceiver = getArguments().getParcelable(EXTRAS_LISTENER);
					Bundle args = new Bundle(3);
					args.putInt(EXTRAS_YEAR, year);
					args.putInt(EXTRAS_MONTH, month);
					args.putInt(EXTRAS_DAY_OF_MONTH,
					            TextUtils.isEmpty(mBinding.dayEt.getText()) || Integer.valueOf(mBinding.dayEt.getText()
					                                                                                         .toString()) <= 0 ?
					            IGNORED_DAY :
					            Integer.valueOf(mBinding.dayEt.getText()
					                                          .toString()));
					if (resultReceiver != null) {
						resultReceiver.send(RESULT_CODE, args);
					}
					dismiss();
				}
			}
		});
	}

}
