package com.nasa.pic.app.activities;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetBehavior.BottomSheetCallback;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AlertDialog.Builder;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.nasa.pic.R;
import com.nasa.pic.api.Api;
import com.nasa.pic.app.App;
import com.nasa.pic.app.adapters.PhotoListAdapter;
import com.nasa.pic.app.fragments.AboutDialogFragment.EulaConfirmationDialog;
import com.nasa.pic.app.fragments.AppListImpFragment;
import com.nasa.pic.app.noactivities.AppGuardService;
import com.nasa.pic.databinding.ActivityAbstractMainBinding;
import com.nasa.pic.ds.RequestPhotoList;
import com.nasa.pic.events.EULAConfirmedEvent;
import com.nasa.pic.events.EULARejectEvent;
import com.nasa.pic.utils.Prefs;

import java.util.Calendar;
import java.util.UUID;

import io.realm.RealmObject;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import io.realm.Sort;

/**
 * An abstract {@link android.app.Activity} that contains {@link android.support.v4.widget.DrawerLayout}.
 *
 * @author Xinyue Zhao
 */
public abstract class AbstractMainActivity extends AppRestfulActivity   {

	/**
	 * The menu to this view.
	 */
	private static final int MENU = R.menu.menu_main;

	/**
	 * Main layout for this component.
	 */
	private static final int LAYOUT = R.layout.activity_abstract_main;
	/**
	 * Data-binding.
	 */
	private ActivityAbstractMainBinding mBinding;

	//[Begin for detecting scrolling onto bottom]
	private int mVisibleItemCount;
	private int mPastVisibleItems;
	private int mTotalItemCount;
	//[End]
	private Calendar mCalendar = Calendar.getInstance();

	//------------------------------------------------
	//Subscribes, event-handlers
	//------------------------------------------------


	/**
	 * Handler for {@link  EULARejectEvent}.
	 *
	 * @param e Event {@link  EULARejectEvent}.
	 */
	public void onEvent(EULARejectEvent e) {
		finish();
	}

	/**
	 * Handler for {@link EULAConfirmedEvent}
	 *
	 * @param e Event {@link  EULAConfirmedEvent}.
	 */
	public void onEvent(EULAConfirmedEvent e) {


	}
	//------------------------------------------------



	/**
	 * To confirm whether the validation of the Play-service of Google Inc.
	 */
	private void checkPlayService() {
		final int isFound = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
		if (isFound == ConnectionResult.SUCCESS) {//Ignore update.
			//The "End User License Agreement" must be confirmed before you use this application.
			if (!Prefs.getInstance()
			          .isEULAOnceConfirmed()) {
				showDialogFragment(new EulaConfirmationDialog(), null);
			}
		} else {
			new Builder(this).setTitle(R.string.application_name)
			                 .setMessage(R.string.play_service)
			                 .setCancelable(false)
			                 .setPositiveButton(R.string.btn_yes, new DialogInterface.OnClickListener() {
				                 public void onClick(DialogInterface dialog, int whichButton) {
					                 dialog.dismiss();
					                 Intent intent = new Intent(Intent.ACTION_VIEW);
					                 intent.setData(Uri.parse(getString(R.string.play_service_url)));
					                 try {
						                 startActivity(intent);
					                 } catch (ActivityNotFoundException e0) {
						                 intent.setData(Uri.parse(getString(R.string.play_service_web)));
						                 try {
							                 startActivity(intent);
						                 } catch (Exception e1) {
							                 //Ignore now.
						                 }
					                 } finally {
						                 finish();
					                 }
				                 }
			                 })
			                 .setCancelable(isFound == ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED)
			                 .create()
			                 .show();
		}
	}


	@Override
	protected void loadList() {
		if(mCalendar == null) {
			return;
		}

		int year = mCalendar.get(Calendar.YEAR);
		int currentMonth = mCalendar.get(Calendar.MONTH);
		int currentDay = mCalendar.get(Calendar.DAY_OF_MONTH);
		int shownMonth = currentMonth + 1;
		String timeZone = mCalendar.getTimeZone()
		                          .getID();


		loadPhotoList(year, shownMonth, -1, timeZone);
		if (currentDay < 15) {
			loadPhotoList(year, currentMonth, -1, timeZone);
			mCalendar.add(Calendar.MONTH, -1);
		}
	}

	@Override
	protected void queryLocalData() {
		buildListView(this, mBinding.responsesRv);
		super.queryLocalData();
	}


	@Override
	protected RealmResults<? extends RealmObject> createQuery(RealmQuery<? extends RealmObject> q) {
		RealmResults<? extends RealmObject> results = q.findAllSortedAsync("date", Sort.DESCENDING);
		return results;
	}


	@Override
	protected void buildViews() {
		if (isDataLoaded()) {
			if (mBinding.getAdapter() == null) {
				mBinding.setAdapter(new PhotoListAdapter(getCellSize()));
			}
			if (mBinding.getAdapter()
			            .getData() == null) {
				mBinding.getAdapter()
				        .setData(getData());
			}
			mBinding.getAdapter()
			        .notifyDataSetChanged();
		}
	}


	@Override
	protected void sendPending() {

	}

	@Override
	protected void onRestApiSuccess() {
		mBinding.contentSrl.setRefreshing(false);
	}


	@Override
	protected void onRestApiFail() {
		mBinding.contentSrl.setRefreshing(false);
	}

	protected void loadPhotoList(int year, int month, int day, String timeZone) {
		RequestPhotoList requestPhotoList = new RequestPhotoList();
		requestPhotoList.setReqId(UUID.randomUUID()
		                              .toString());
		requestPhotoList.setYear(year);
		requestPhotoList.setMonth(month);
		requestPhotoList.setTimeZone(timeZone);
		App.Instance.getApiManager()
		            .execAsync(AppGuardService.Retrofit.create(Api.class)
		                                               .getPhotoMonthList(requestPhotoList), requestPhotoList);
	}


	@Override
	protected void initDataBinding() {
		mBinding = DataBindingUtil.setContentView(this, LAYOUT);
		setUpErrorHandling((ViewGroup) findViewById(R.id.error_content));

		//Pull-2-load indicator
		initPull2Load(mBinding.contentSrl);
	}


	protected void initMenu() {
		mBinding.toolbar.inflateMenu(getMenuRes());
		mBinding.toolbar.setOnMenuItemClickListener(this);

		MenuItem menuShare = mBinding.toolbar.getMenu()
		                                     .findItem(R.id.action_share);
		android.support.v7.widget.ShareActionProvider provider = (android.support.v7.widget.ShareActionProvider) MenuItemCompat.getActionProvider(menuShare);

		String subject = getString(R.string.lbl_share_app_title);
		String text = getString(R.string.lbl_share_app_content,
		                        getString(R.string.application_name),
		                        Prefs.getInstance()
		                             .getAppDownloadInfo());

		provider.setShareIntent(com.chopping.utils.Utils.getDefaultShareIntent(provider, subject, text));

	}


	/**
	 * Show all external applications links.
	 */
	private void showAppList() {
		getSupportFragmentManager().beginTransaction()
		                           .replace(R.id.app_list_fl, AppListImpFragment.newInstance(this))
		                           .commit();

		BottomSheetBehavior behavior = BottomSheetBehavior.from(mBinding.bottomSheet);
		behavior.setBottomSheetCallback(new BottomSheetCallback() {
			@Override
			public void onStateChanged(@NonNull View bottomSheet, int newState) {
				switch (newState) {
					case BottomSheetBehavior.STATE_EXPANDED:
						mBinding.bottomSheetHeadIv.setVisibility(View.GONE);
						break;
					case BottomSheetBehavior.STATE_COLLAPSED:
						mBinding.bottomSheetHeadIv.setVisibility(View.VISIBLE);
				}
			}

			@Override
			public void onSlide(@NonNull View bottomSheet, float slideOffset) {

			}
		});
	}


	@Override
	protected void onAppConfigIgnored() {
		super.onAppConfigIgnored();
		onConfigFinished();
	}


	@Override
	protected void onAppConfigLoaded() {
		super.onAppConfigLoaded();
		onConfigFinished();
	}

	private void onConfigFinished() {
		checkPlayService();
		showAppList();
	}

	@Override
	protected int getMenuRes() {
		return MENU;
	}

	/**
	 * @return {@link android.databinding.ViewDataBinding} object of this {@link Activity}.
	 */
	protected ActivityAbstractMainBinding getBinding() {
		return mBinding;
	}

	@Override
	public boolean onMenuItemClick(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.action_more_apps:
				BottomSheetBehavior behavior = BottomSheetBehavior.from(mBinding.bottomSheet);
				behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
				break;
		}
		return super.onMenuItemClick(item);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getBinding().toolbar.setTitle(R.string.application_name);
		mBinding.loadMoreFab.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				mCalendar.add(Calendar.MONTH, -1);
				loadList();
			}
		});
		mBinding.responsesRv.addOnScrollListener(new RecyclerView.OnScrollListener() {
			@Override
			public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
				//Scrolling up and down can hiden and show the FAB.
				float y = ViewCompat.getY(recyclerView);
				if (y < dy) {
					if (mBinding.showDayFab.isShown()) {
						mBinding.showDayFab.hide();
					}
					if (mBinding.showMonthFab.isShown()) {
						mBinding.showMonthFab.hide();
					}
				} else {
					if (!mBinding.showDayFab.isShown()) {
						mBinding.showDayFab.show();
					}
					if (!mBinding.showMonthFab.isShown()) {
						mBinding.showMonthFab.show();
					}
				}

				//Calc whether the list has been scrolled on bottom,
				//this lets app to getting next page.
				LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
				mVisibleItemCount = linearLayoutManager.getChildCount();
				mTotalItemCount = linearLayoutManager.getItemCount();
				mPastVisibleItems = linearLayoutManager.findFirstVisibleItemPosition();
				if (ViewCompat.getY(recyclerView) < dy) {
					if ((mVisibleItemCount + mPastVisibleItems) == mTotalItemCount) {
						//Load more
						if (!mBinding.loadMoreFab.isShown()) {
							mBinding.loadMoreFab.show();
						}
					}
				} else {
					if (mBinding.loadMoreFab.isShown()) {
						mBinding.loadMoreFab.hide();
					}
				}
			}});
	}
}
