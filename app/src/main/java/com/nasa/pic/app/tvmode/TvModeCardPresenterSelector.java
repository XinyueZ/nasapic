package com.nasa.pic.app.tvmode;

import android.content.Context;
import android.support.v17.leanback.widget.Presenter;
import android.support.v17.leanback.widget.PresenterSelector;

import com.nasa.pic.R;
import com.nasa.pic.ds.PhotoDB;

/**
 * This PresenterSelector will decide what Presenter to use depending on a given card's type.
 */
public class TvModeCardPresenterSelector
		extends PresenterSelector {

	private final Context   mContext;
	private       Presenter mPresenter;

	public TvModeCardPresenterSelector(Context context) {
		mContext = context;
	}

	@Override
	public Presenter getPresenter(Object item) {
		if (!(item instanceof PhotoDB)) {
			throw new RuntimeException(String.format("The PresenterSelector only supports data items of type '%s'",
			                                         PhotoDB.class.getName()));
		}
		if (mPresenter == null) {
			int style = R.style.GridCardStyle;
			mPresenter = new TvModeGridCardPresenter(mContext,
			                                         style);
		}
		return mPresenter;
	}

}
