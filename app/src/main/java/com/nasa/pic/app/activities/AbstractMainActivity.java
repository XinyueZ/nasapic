package com.nasa.pic.app.activities;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetBehavior.BottomSheetCallback;
import android.support.design.widget.Snackbar;
import android.support.v4.os.ResultReceiver;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AlertDialog.Builder;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
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
import com.nasa.pic.app.fragments.DatePickerDialogFragment;
import com.nasa.pic.app.fragments.MonthPickerDialogFragment;
import com.nasa.pic.app.noactivities.AppGuardService;
import com.nasa.pic.databinding.ActivityAbstractMainBinding;
import com.nasa.pic.ds.PhotoDB;
import com.nasa.pic.ds.RequestPhotoList;
import com.nasa.pic.events.EULAConfirmedEvent;
import com.nasa.pic.events.EULARejectEvent;
import com.nasa.pic.utils.Prefs;

import java.util.Calendar;
import java.util.Locale;
import java.util.UUID;

import io.realm.RealmObject;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import io.realm.Sort;

import static com.nasa.pic.app.fragments.DatePickerDialogFragment.RESULT_CODE;

/**
 * An abstract {@link android.app.Activity} that contains {@link android.support.v4.widget.DrawerLayout}.
 *
 * @author Xinyue Zhao
 */
public abstract class AbstractMainActivity extends AppRestfulActivity {

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
		Calendar calendar = Calendar.getInstance();
		int year = calendar.get(Calendar.YEAR);
		int currentMonth = calendar.get(Calendar.MONTH);
		int currentDay = calendar.get(Calendar.DAY_OF_MONTH);
		int shownMonth = currentMonth + 1;
		String timeZone = calendar.getTimeZone()
		                          .getID();


		loadPhotoList(year, shownMonth, timeZone);
		if (currentDay < 15) {
			loadPhotoList(year, currentMonth, timeZone);
		}
	}

	private void loadList(Calendar calendar) {
		int year = calendar.get(Calendar.YEAR);
		int currentMonth = calendar.get(Calendar.MONTH);
		int shownMonth = currentMonth + 1;
		String timeZone = calendar.getTimeZone()
		                          .getID();
		loadPhotoList(year, shownMonth, timeZone);
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
		stopLoadingMoreIndicator();
	}


	@Override
	protected void onRestApiFail() {
		mBinding.contentSrl.setRefreshing(false);
		stopLoadingMoreIndicator();
	}


	protected void loadPhotoList(int year, int month, String timeZone) {
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
		mBinding.toolbar.setTitle(R.string.application_name);
		mBinding.loadingFab.hide();
		mBinding.loadMoreFab.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				PhotoDB photoDB = (PhotoDB) getData().last();
				Log.d("last searched",
				      "searched date: " + photoDB.getDate()
				                                 .toString());
				Calendar calendar = Calendar.getInstance();
				calendar.setTime(photoDB.getDate());
				calendar.add(Calendar.MONTH, -1);
				loadList(calendar);
				Snackbar.make(mBinding.errorContent, R.string.lbl_more_photos_to_load, Snackbar.LENGTH_LONG)
				        .show();
				startLoadingMoreIndicator();
			}
		});
		mBinding.showMonthFab.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				showDialogFragment(MonthPickerDialogFragment.newInstance(new ResultReceiver(new Handler(getMainLooper())) {
					@Override
					protected void onReceiveResult(int resultCode, Bundle resultData) {
						super.onReceiveResult(resultCode, resultData);
						switch (resultCode) {
							case RESULT_CODE:
								int year = resultData.getInt(MonthPickerDialogFragment.EXTRAS_YEAR);
								int month = resultData.getInt(MonthPickerDialogFragment.EXTRAS_MONTH);
								String keyword = String.format(Locale.getDefault(), "%d-%d", year, month);
								Log.d("keyword", "keyword: " + keyword);
								SearchResultActivity.showInstance(AbstractMainActivity.this, keyword);
								break;
						}
					}
				}), "picker");
			}
		});
		mBinding.showDayFab.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				showDialogFragment(DatePickerDialogFragment.newInstance(new ResultReceiver(new Handler(getMainLooper())) {
					@Override
					protected void onReceiveResult(int resultCode, Bundle resultData) {
						super.onReceiveResult(resultCode, resultData);
						switch (resultCode) {
							case RESULT_CODE:
								int year = resultData.getInt(DatePickerDialogFragment.EXTRAS_YEAR);
								int month = resultData.getInt(DatePickerDialogFragment.EXTRAS_MONTH);
								int dayOfMonth = resultData.getInt(DatePickerDialogFragment.EXTRAS_DAY_OF_MONTH);
								Calendar calendar = Calendar.getInstance();
								String timeZone = calendar.getTimeZone()
								                          .getID();
								String keyword = String.format(Locale.getDefault(), "%d-%d-%d", year, month, dayOfMonth);
								Log.d("keyword", "keyword: " + keyword);
//								List<String> keywords = new ArrayList<>(1);
//								keywords.add(keyword);
//								RequestPhotoDayList dayListRequest = new RequestPhotoDayList();
//								dayListRequest.setReqId(UUID.randomUUID()
//								                            .toString());
//								dayListRequest.setDateTimes(keywords);
//								dayListRequest.setTimeZone(timeZone);
//								App.Instance.getApiManager()
//								            .execAsync(AppGuardService.Retrofit.create(Api.class)
//								                                               .getPhotoList(dayListRequest), dayListRequest);
//								mBinding.contentSrl.setRefreshing(true);
								break;
						}
					}
				}), "picker");
			}
		});
		mBinding.responsesRv.addOnScrollListener(new RecyclerView.OnScrollListener() {
			@Override
			public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
				//Scrolling up and down can hidden and show the FAB.
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
			}
		});
	}

	private void startLoadingMoreIndicator() {
		Drawable drawable = mBinding.loadingFab.getDrawable();
		if (drawable instanceof Animatable) {
			((Animatable) drawable).start();
			mBinding.loadingFab.show();
		}
	}

	private void stopLoadingMoreIndicator() {
		Drawable drawable = mBinding.loadingFab.getDrawable();
		if (drawable instanceof Animatable) {
			((Animatable) drawable).stop();
			mBinding.loadingFab.hide();
		}
	}
}
