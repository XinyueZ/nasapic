/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.nasa.pic.app.fragments;

import android.app.Activity;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v17.leanback.app.DetailsFragment;
import android.support.v17.leanback.widget.Action;
import android.support.v17.leanback.widget.ArrayObjectAdapter;
import android.support.v17.leanback.widget.ClassPresenterSelector;
import android.support.v17.leanback.widget.DetailsOverviewRow;
import android.support.v17.leanback.widget.FullWidthDetailsOverviewRowPresenter;
import android.support.v17.leanback.widget.FullWidthDetailsOverviewSharedElementHelper;
import android.support.v17.leanback.widget.HeaderItem;
import android.support.v17.leanback.widget.ListRow;
import android.support.v17.leanback.widget.ListRowPresenter;
import android.support.v17.leanback.widget.OnItemViewClickedListener;
import android.support.v17.leanback.widget.Presenter;
import android.support.v17.leanback.widget.Row;
import android.support.v17.leanback.widget.RowPresenter;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.os.ResultReceiver;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import com.bumptech.glide.load.resource.bitmap.GlideBitmapDrawable;
import com.chopping.utils.Utils;
import com.nasa.pic.R;
import com.nasa.pic.app.App;
import com.nasa.pic.app.noactivities.LoadImageManuallyService;
import com.nasa.pic.app.tvmode.TvModeDetailPresenter;
import com.nasa.pic.app.tvmode.TvModeGridPresenterSelector;
import com.nasa.pic.ds.PhotoDB;

import java.util.Calendar;
import java.util.Date;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmObject;
import io.realm.RealmQuery;
import io.realm.RealmResults;


/**
 * Displays a card with more details using a {@link DetailsFragment}.
 */
public class TvModeDetailFragment
		extends DetailsFragment
		implements OnItemViewClickedListener {

	public static final String EXTRAS_IMAGE    = "com.nasa.pic.app.fragments.extra.BITMAP";
	public static final String TRANSITION_NAME = "t_for_transition";
	public static final String EXTRAS_PHOTO    = "photo";
	public static final int    ACTION_ID_CAST  = 1;

	private ArrayObjectAdapter mRowsAdapter;

	private Realm                               mRealm;
	private RealmResults<? extends RealmObject> mRealmData;
	private DetailsOverviewRow                  mDetailsOverview;


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setupUi();
		setupEventListeners();
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

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		Bundle extras = getActivity().getIntent()
		                             .getExtras();
		String photoReqId = extras.getString(EXTRAS_PHOTO,
		                                     null);
		mRealm = Realm.getDefaultInstance();
		mRealmData = mRealm.where(PhotoDB.class)
		                   .equalTo("reqId",
		                            photoReqId)
		                   .findAllAsync();
		mRealmData.addChangeListener(new RealmChangeListener() {
			@Override
			public void onChange() {
				mRealmData.removeChangeListener(this);
				PhotoDB photo = (PhotoDB) mRealmData.get(0);
				//Show details, photo-snapshot, description etc.
				setRowActionsAndDetail(photo);
				//Async task to get photo-list in the month of "this photo".
				fetchRecommendedItems(photo.getDate());
				mRealmData.addChangeListener(new RealmChangeListener() {
					@Override
					public void onChange() {
						mRealmData.removeChangeListener(this);
						setRowRecommended();
					}
				});
			}
		});
	}

	private void setupUi() {
		Activity activity = getActivity();
		if (activity != null) {
			FullWidthDetailsOverviewRowPresenter rowPresenter = new FullWidthDetailsOverviewRowPresenter(new TvModeDetailPresenter(activity)) {
				@Override
				protected RowPresenter.ViewHolder createRowViewHolder(ViewGroup parent) {
					// Customize Actionbar and Content by using custom colors.
					RowPresenter.ViewHolder viewHolder = super.createRowViewHolder(parent);

					View actionsView = viewHolder.view.findViewById(R.id.details_overview_actions_background);
					actionsView.setBackgroundColor(ResourcesCompat.getColor(getResources(),
					                                                        R.color.colorAccent,
					                                                        null));

					View detailsView = viewHolder.view.findViewById(R.id.details_frame);
					detailsView.setBackgroundColor(ResourcesCompat.getColor(getResources(),
					                                                        R.color.colorPrimary,
					                                                        null));
					return viewHolder;
				}
			};

			FullWidthDetailsOverviewSharedElementHelper mHelper = new FullWidthDetailsOverviewSharedElementHelper();
			mHelper.setSharedElementEnterTransition(activity,
			                                        TRANSITION_NAME);
			rowPresenter.setListener(mHelper);
			rowPresenter.setParticipatingEntranceTransition(false);
			prepareEntranceTransition();

			ListRowPresenter shadowDisabledRowPresenter = new ListRowPresenter();
			shadowDisabledRowPresenter.setShadowEnabled(false);

			// Setup PresenterSelector to distinguish between the different rows.
			ClassPresenterSelector rowPresenterSelector = new ClassPresenterSelector();
			rowPresenterSelector.addClassPresenter(DetailsOverviewRow.class,
			                                       rowPresenter);
			rowPresenterSelector.addClassPresenter(ListRow.class,
			                                       new ListRowPresenter());
			mRowsAdapter = new ArrayObjectAdapter(rowPresenterSelector);

			setAdapter(mRowsAdapter);
			new Handler().postDelayed(new Runnable() {
				                          @Override
				                          public void run() {
					                          startEntranceTransition();
				                          }
			                          },
			                          500);
		}
	}

	private void setupEventListeners() {
		setOnItemViewClickedListener(this);
	}

	@Override
	public void onItemClicked(Presenter.ViewHolder itemViewHolder,
	                          Object item,
	                          RowPresenter.ViewHolder rowViewHolder,
	                          Row row) {
		if (!(item instanceof Action)) {
			return;
		}
		Action action = (Action) item;
		switch ((int) action.getId()) {
			case ACTION_ID_CAST:
				Utils.showShortToast(App.Instance,
				                     "ACTION_ID_CAST");
				break;
		}
	}


	private void setRowActionsAndDetail(PhotoDB photo) {
		ResultReceiver resultReceiver = new ResultReceiver(new Handler(Looper.getMainLooper())) {
			@Override
			protected void onReceiveResult(int resultCode,
			                               Bundle resultData) {
				super.onReceiveResult(resultCode,
				                      resultData);
				Activity activity = getActivity();
				if (activity != null) {
					Bitmap bitmap = resultData.getParcelable(EXTRAS_IMAGE);
					mDetailsOverview.setImageBitmap(activity,
					                                bitmap);
					updateBackground(bitmap);
				}
			}
		};
		LoadImageManuallyService.startService(App.Instance,
		                                      photo.getUrls()
		                                           .getNormal(),
		                                      resultReceiver);
		mDetailsOverview = new DetailsOverviewRow(photo);
		ArrayObjectAdapter actionAdapter = new ArrayObjectAdapter();
		actionAdapter.add(new Action(ACTION_ID_CAST,
		                             getString(R.string.action_cast)));
		mDetailsOverview.setActionsAdapter(actionAdapter);
		mRowsAdapter.add(mDetailsOverview);
	}

	private void setRowRecommended() {
		Activity activity = getActivity();
		if (activity != null) {
			ArrayObjectAdapter listRowAdapter = new ArrayObjectAdapter(new TvModeGridPresenterSelector(activity));
			listRowAdapter.addAll(0,
			                      mRealmData);
			HeaderItem header = new HeaderItem(1,
			                                   getString(R.string.lbl_more_photos));
			mRowsAdapter.add(new ListRow(header,
			                             listRowAdapter));
		}
	}

	private void fetchRecommendedItems(Date date) {
		RealmQuery<PhotoDB> q        = mRealm.where(PhotoDB.class);
		Calendar            calendar = Calendar.getInstance();
		calendar.set(Calendar.MINUTE,
		             0);
		calendar.set(Calendar.SECOND,
		             0);
		calendar.set(Calendar.MILLISECOND,
		             0);
		calendar.setTime(date);
		int lastDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
		calendar.set(Calendar.DAY_OF_MONTH,
		             lastDay);
		Date timeMax = calendar.getTime();
		q.lessThanOrEqualTo("date",
		                    timeMax);
		calendar.set(Calendar.DAY_OF_MONTH,
		             0);
		Date timeMin = calendar.getTime();
		q.greaterThanOrEqualTo("date",
		                       timeMin);
		mRealmData = q.findAllAsync();
	}

	private void updateBackground(Bitmap bmp) {
		Activity activity = getActivity();
		if (activity != null) {
			Window window = activity.getWindow();
			if (window != null) {
				Resources resources = getResources();
				if (resources != null) {
					window.setBackgroundDrawable(new GlideBitmapDrawable(resources,
					                                                     bmp));
				}
			}
		}
	}
}
