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
import android.support.v17.leanback.widget.ListRowPresenter;
import android.support.v17.leanback.widget.OnItemViewClickedListener;
import android.support.v17.leanback.widget.Presenter;
import android.support.v17.leanback.widget.Row;
import android.support.v17.leanback.widget.RowPresenter;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.os.ResultReceiver;
import android.view.View;
import android.view.ViewGroup;

import com.chopping.utils.Utils;
import com.nasa.pic.R;
import com.nasa.pic.app.App;
import com.nasa.pic.app.noactivities.LoadImageManuallyService;
import com.nasa.pic.app.tvmode.TvModeDetailPresenter;
import com.nasa.pic.ds.PhotoDB;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.RealmResults;


/**
 * Displays a card with more details using a {@link DetailsFragment}.
 */
public class TvModeDetailFragment
		extends DetailsFragment
		implements OnItemViewClickedListener,
		           RealmChangeListener {

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
		mRealmData.addChangeListener(this);
	}

	private void setupUi() {
		FullWidthDetailsOverviewRowPresenter rowPresenter = new FullWidthDetailsOverviewRowPresenter(new TvModeDetailPresenter(getActivity())) {

			@Override
			protected RowPresenter.ViewHolder createRowViewHolder(ViewGroup parent) {
				// Customize Actionbar and Content by using custom colors.
				RowPresenter.ViewHolder viewHolder = super.createRowViewHolder(parent);

				View actionsView = viewHolder.view.findViewById(R.id.details_overview_actions_background);
				actionsView.setBackgroundColor(ResourcesCompat.getColor(getResources(),
				                                                        R.color.colorPrimary,
				                                                        null));

				View detailsView = viewHolder.view.findViewById(R.id.details_frame);
				detailsView.setBackgroundColor(ResourcesCompat.getColor(getResources(),
				                                                        R.color.common_black,
				                                                        null));
				return viewHolder;
			}
		};

		FullWidthDetailsOverviewSharedElementHelper mHelper = new FullWidthDetailsOverviewSharedElementHelper();
		mHelper.setSharedElementEnterTransition(getActivity(),
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
		mRowsAdapter = new ArrayObjectAdapter(rowPresenterSelector);

		// Setup action and detail row.
//		mDetailsOverview = new DetailsOverviewRow(photo);
//		ArrayObjectAdapter actionAdapter = new ArrayObjectAdapter();
//		actionAdapter.add(new Action(ACTION_ID_CAST,
//		                             getString(R.string.action_cast)));
//		mDetailsOverview.setActionsAdapter(actionAdapter);
//		mRowsAdapter.add(mDetailsOverview);

		// Setup related row.
//		ArrayObjectAdapter listRowAdapter = new ArrayObjectAdapter(
//				new CardPresenterSelector(getActivity()));
//		for (Card characterCard : data.getCharacters()) listRowAdapter.add(characterCard);
//		HeaderItem header = new HeaderItem(0, getString(R.string.header_related));
//		mRowsAdapter.add(new CardListRow(header, listRowAdapter, null));


		// Setup recommended row.
//        listRowAdapter = new ArrayObjectAdapter(new CardPresenterSelector(getActivity()));
//        for (Card card : data.getRecommended()) listRowAdapter.add(card);
//        header = new HeaderItem(1, getString(R.string.header_recommended));
//        mRowsAdapter.add(new ListRow(header, listRowAdapter));
//
		setAdapter(mRowsAdapter);
		new Handler().postDelayed(new Runnable() {
			                          @Override
			                          public void run() {
				                          startEntranceTransition();
			                          }
		                          },
		                          500);
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


	@Override
	public void onChange() {
		PhotoDB photo = (PhotoDB) mRealmData.get(0);
		setRowActionsAndDetail(photo);
	}

	private void setRowActionsAndDetail(PhotoDB photo) {
		ResultReceiver resultReceiver = new ResultReceiver(new Handler(Looper.getMainLooper())) {
			@Override
			protected void onReceiveResult(int resultCode,
			                               Bundle resultData) {
				super.onReceiveResult(resultCode,
				                      resultData);
				mDetailsOverview.setImageBitmap(getActivity(),
				                                (Bitmap) resultData.getParcelable(EXTRAS_IMAGE));
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

	private void setRowRecommended(RealmList<PhotoDB> photoList){
		//TODO Add recommended photos which are shot in the same month.
	}
}
