

package com.nasa.pic.app.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.support.v17.leanback.app.VerticalGridFragment;
import android.support.v17.leanback.widget.ArrayObjectAdapter;
import android.support.v17.leanback.widget.FocusHighlight;
import android.support.v17.leanback.widget.PresenterSelector;
import android.support.v17.leanback.widget.VerticalGridPresenter;

import com.nasa.pic.R;
import com.nasa.pic.app.tvmode.TvModeCardPresenterSelector;
import com.nasa.pic.ds.PhotoDB;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmObject;
import io.realm.RealmResults;

/**
 * An example how to use leanback's {@link VerticalGridFragment}.
 */
public class TvModeFragment
		extends VerticalGridFragment {

	private static final int COLUMNS     = 4;
	private static final int ZOOM_FACTOR = FocusHighlight.ZOOM_FACTOR_MEDIUM;

	private ArrayObjectAdapter mAdapter;

	private Realm                               mRealm;
	private RealmResults<? extends RealmObject> mRealmData;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mRealm = Realm.getDefaultInstance();
		setTitle(getString(R.string.action_tv_mode));
		setupRowAdapter();
	}

	private void setupRowAdapter() {
		VerticalGridPresenter gridPresenter = new VerticalGridPresenter(ZOOM_FACTOR);
		gridPresenter.setNumberOfColumns(COLUMNS);
		setGridPresenter(gridPresenter);

		PresenterSelector cardPresenterSelector = new TvModeCardPresenterSelector(getActivity());
		mAdapter = new ArrayObjectAdapter(cardPresenterSelector);
		setAdapter(mAdapter);

		prepareEntranceTransition();
		new Handler().postDelayed(new Runnable() {
			                          @Override
			                          public void run() {
				                          createRows();
				                          startEntranceTransition();
			                          }
		                          },
		                          1000);
	}

	@Override
	public void onDestroy() {
		if (mRealm != null) {
			mRealm.close();
		}
		super.onDestroy();
	}

	private void createRows() {
		final RealmResults<PhotoDB> async = mRealm.where(PhotoDB.class)
		                                          .findAllAsync();
		async.addChangeListener(new RealmChangeListener() {
			@Override
			public void onChange() {
				async.removeChangeListeners();
				mAdapter.addAll(0,
				                async);
			}
		});
	}
}
