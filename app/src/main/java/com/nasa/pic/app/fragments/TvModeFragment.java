

package com.nasa.pic.app.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v17.leanback.app.VerticalGridFragment;
import android.support.v17.leanback.widget.ArrayObjectAdapter;
import android.support.v17.leanback.widget.FocusHighlight;
import android.support.v17.leanback.widget.ImageCardView;
import android.support.v17.leanback.widget.OnItemViewClickedListener;
import android.support.v17.leanback.widget.OnItemViewSelectedListener;
import android.support.v17.leanback.widget.Presenter;
import android.support.v17.leanback.widget.PresenterSelector;
import android.support.v17.leanback.widget.Row;
import android.support.v17.leanback.widget.RowPresenter;
import android.support.v17.leanback.widget.VerticalGridPresenter;
import android.support.v4.app.ActivityOptionsCompat;
import android.widget.ImageView;

import com.nasa.pic.app.activities.TvModeDetailActivity;
import com.nasa.pic.app.tvmode.TvModeGridPresenterSelector;
import com.nasa.pic.ds.PhotoDB;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmObject;
import io.realm.RealmResults;

/**
 * An example how to use leanback's {@link VerticalGridFragment}.
 */
public class TvModeFragment
		extends VerticalGridFragment
		implements RealmChangeListener {

	private static final int COLUMNS     = 4;
	private static final int ZOOM_FACTOR = FocusHighlight.ZOOM_FACTOR_MEDIUM;

	private ArrayObjectAdapter mAdapter;

	private Realm                               mRealm;
	private RealmResults<? extends RealmObject> mRealmData;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mRealm = Realm.getDefaultInstance();
		setupRowAdapter();
	}

	private void setupRowAdapter() {
		VerticalGridPresenter gridPresenter = new VerticalGridPresenter(ZOOM_FACTOR);
		gridPresenter.setNumberOfColumns(COLUMNS);
		setGridPresenter(gridPresenter);

		PresenterSelector cardPresenterSelector = new TvModeGridPresenterSelector(getActivity());
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
		setOnItemViewSelectedListener(new OnItemViewSelectedListener() {
			@Override
			public void onItemSelected(Presenter.ViewHolder itemViewHolder,
			                           Object item,
			                           RowPresenter.ViewHolder rowViewHolder,
			                           Row row) {
			}
		});
		setOnItemViewClickedListener(new OnItemViewClickedListener() {
			@Override
			public void onItemClicked(Presenter.ViewHolder itemViewHolder,
			                          Object item,
			                          RowPresenter.ViewHolder rowViewHolder,
			                          Row row) {
				if (!(item instanceof PhotoDB) || !(itemViewHolder.view instanceof ImageCardView)) {
					return;
				}

				Activity activity = getActivity();
				if (activity != null) {
					ImageView imageView = ((ImageCardView) itemViewHolder.view).getMainImageView();
					Bundle bundle = ActivityOptionsCompat.makeSceneTransitionAnimation(activity,
					                                                                   imageView,
					                                                                   TvModeDetailFragment.TRANSITION_NAME)
					                                     .toBundle();
					Intent intent = new Intent(activity.getBaseContext(),
					                           TvModeDetailActivity.class);
					PhotoDB photo = (PhotoDB) item;
					intent.putExtra(TvModeDetailFragment.EXTRAS_PHOTO,
					                photo.getReqId());
					startActivity(intent,
					              bundle);
				}

			}
		});
	}

	@Override
	public void onDestroy() {
		if (mRealmData != null) {
			mRealmData.removeChangeListeners();
		}
		if (mRealm != null) {
			mRealm.close();
		}
		super.onDestroy();
	}

	private void createRows() {
		mRealmData = mRealm.where(PhotoDB.class)
		                   .findAllAsync();
		mRealmData.addChangeListener(this);
	}

	@Override
	public void onChange() {
		mAdapter.addAll(0,
		                mRealmData);
	}
}
