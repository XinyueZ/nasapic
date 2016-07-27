package com.nasa.pic.app.activities;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetBehavior.BottomSheetCallback;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.animation.AnimatorCompatHelper;
import android.support.v4.animation.AnimatorUpdateListenerCompat;
import android.support.v4.animation.ValueAnimatorCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.os.ResultReceiver;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog.Builder;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Interpolator;

import com.chopping.application.LL;
import com.chopping.utils.DeviceUtils;
import com.chopping.utils.Utils;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.appinvite.AppInvite;
import com.google.android.gms.appinvite.AppInviteInvitation;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.nasa.pic.R;
import com.nasa.pic.api.Api;
import com.nasa.pic.app.App;
import com.nasa.pic.app.adapters.PhotoListAdapter;
import com.nasa.pic.app.adapters.SectionedGridRecyclerViewAdapter;
import com.nasa.pic.app.fragments.AboutDialogFragment.EulaConfirmationDialog;
import com.nasa.pic.app.fragments.AppListImpFragment;
import com.nasa.pic.app.fragments.DatePickerDialogFragment;
import com.nasa.pic.app.noactivities.AppGuardService;
import com.nasa.pic.databinding.ActivityAbstractMainBinding;
import com.nasa.pic.databinding.ItemBinding;
import com.nasa.pic.ds.PhotoDB;
import com.nasa.pic.ds.RequestPhotoDayList;
import com.nasa.pic.ds.RequestPhotoLastThreeList;
import com.nasa.pic.ds.RequestPhotoList;
import com.nasa.pic.events.ClickPhotoItemEvent;
import com.nasa.pic.events.EULAConfirmedEvent;
import com.nasa.pic.events.EULARejectEvent;
import com.nasa.pic.events.OpenPhotoEvent;
import com.nasa.pic.transition.BakedBezierInterpolator;
import com.nasa.pic.transition.Thumbnail;
import com.nasa.pic.transition.TransitCompat;
import com.nasa.pic.utils.Prefs;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import de.greenrobot.event.EventBus;
import io.realm.Realm;
import io.realm.RealmObject;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import io.realm.Sort;

import static com.nasa.pic.app.fragments.DatePickerDialogFragment.RESULT_CODE;
import static com.nasa.pic.utils.Utils.extractSections;
import static com.nasa.pic.utils.Utils.validateDateTimeSelection;
import static java.util.concurrent.TimeUnit.DAYS;

/**
 * An abstract {@link android.app.Activity} that contains {@link android.support.v4.widget.DrawerLayout}.
 *
 * @author Xinyue Zhao
 */
public abstract class AbstractMainActivity extends AppRestfulActivity implements GoogleApiClient.OnConnectionFailedListener {

	/**
	 * The menu to this view.
	 */
	private static final int MENU = R.menu.menu_main;

	/**
	 * Main layout for this component.
	 */
	private static final int LAYOUT = R.layout.activity_abstract_main;
	private static final int DURATION_FOUR_DAYS = 4;
	private static final int DURATION_THREE_DAYS = 3;
	private static final int DURATION_ONE_DAY = 1;
	/**
	 * Data-binding.
	 */
	private ActivityAbstractMainBinding mBinding;

	private SectionedGridRecyclerViewAdapter mSectionedAdapter;
	private ItemBinding mItemBinding;


	protected PhotoListAdapter mPhotoListAdapter;

	private static final int REQUEST_INVITE = 0x97;

	/**
	 * The interstitial ad.
	 */
	private InterstitialAd mInterstitialAd;

	//------------------------------------------------
	//Subscribes, event-handlers
	//------------------------------------------------

	/**
	 * Handler for {@link com.nasa.pic.events.OpenPhotoEvent}.
	 *
	 * @param e Event {@link com.nasa.pic.events.OpenPhotoEvent}.
	 */
	public void onEvent(OpenPhotoEvent e) {
		PhotoDB photoDB = (PhotoDB) e.getObject();
		if (e.getThumbnail() != null) {
			e.getThumbnail()
			 .setSource(photoDB.getUrls()
			                   .getNormal());
			PhotoViewActivity.showInstance(this,
			                               TextUtils.isEmpty(photoDB.getTitle()) ?
			                               "" :
			                               photoDB.getTitle(),
			                               photoDB.getDescription(),
			                               TextUtils.isEmpty(photoDB.getUrls()
			                                                        .getHd()) ?
			                               photoDB.getUrls()
			                                      .getNormal() :
			                               photoDB.getUrls()
			                                      .getHd(),
			                               photoDB.getUrls()
			                                      .getNormal(),
			                               photoDB.getDate(),
			                               photoDB.getType(),
			                               e.getThumbnail());
			mItemBinding = e.getItemBinding();
		} else {
			PhotoViewActivity.showInstance(this,
			                               TextUtils.isEmpty(photoDB.getTitle()) ?
			                               "" :
			                               photoDB.getTitle(),
			                               photoDB.getDescription(),
			                               TextUtils.isEmpty(photoDB.getUrls()
			                                                        .getHd()) ?
			                               photoDB.getUrls()
			                                      .getNormal() :
			                               photoDB.getUrls()
			                                      .getHd(),
			                               photoDB.getUrls()
			                                      .getNormal(),
			                               photoDB.getDate(),
			                               photoDB.getType());
		}
	}

	/**
	 * Handler for {@link  EULARejectEvent}.
	 *
	 * @param e Event {@link  EULARejectEvent}.
	 */
	public void onEvent(@SuppressWarnings("UnusedParameters") EULARejectEvent e) {
		finish();
	}

	/**
	 * Handler for {@link EULAConfirmedEvent}
	 *
	 * @param e Event {@link  EULAConfirmedEvent}.
	 */
	public void onEvent(@SuppressWarnings("UnusedParameters") EULAConfirmedEvent e) {


	}

	/**
	 * Handler for {@link ClickPhotoItemEvent}.
	 *
	 * @param e Event {@link ClickPhotoItemEvent}.
	 */
	public void onEvent(ClickPhotoItemEvent e) {
		final RecyclerView.ViewHolder viewHolder = e.getViewHolder();
		int pos = mSectionedAdapter.sectionedPositionToPosition(viewHolder.getAdapterPosition());
		if (viewHolder instanceof PhotoListAdapter.ItemViewHolder) {
			openPhoto((PhotoListAdapter.ItemViewHolder) viewHolder, pos);
		} else {
			EventBus.getDefault()
			        .post(new OpenPhotoEvent(getData().get(pos), null, null));
			mItemBinding = null;
		}
	}
	//------------------------------------------------


	/**
	 * To confirm whether the validation of the Play-service of Google Inc.
	 */
	private void checkPlayService() {
		GoogleApiAvailability googleAPI = GoogleApiAvailability.getInstance();
		int result = googleAPI.isGooglePlayServicesAvailable(this);
		if (result == ConnectionResult.SUCCESS) {//Ignore update.
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
			                 .setCancelable(result == ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED)
			                 .create()
			                 .show();
		}
	}


	@Override
	protected void loadList() {
		Calendar calendar = Calendar.getInstance();
		int year = calendar.get(Calendar.YEAR);
		int lastMonth = calendar.get(Calendar.MONTH);
		int thisMonth = lastMonth + 1;
		int beforeLastMonth = lastMonth - 1;
		String timeZone = calendar.getTimeZone()
		                          .getID();


		loadPhotoList(year, thisMonth, DatePickerDialogFragment.IGNORED_DAY, timeZone);
		loadPhotoList(year, lastMonth, DatePickerDialogFragment.IGNORED_DAY, timeZone);
		loadPhotoList(year, beforeLastMonth, DatePickerDialogFragment.IGNORED_DAY, timeZone);
	}


	@Override
	protected void queryLocalData() {
		buildListView(this, mBinding.responsesRv);
		super.queryLocalData();
	}


	@Override
	protected RealmResults<? extends RealmObject> createQuery(RealmQuery<? extends RealmObject> q) {
		return q.findAllSortedAsync("date", Sort.DESCENDING);
	}


	@Override
	protected void buildViews() {
		setHasShownDataOnUI(!getData().isEmpty());
		if (isDataLoaded()) {
			if (mBinding.responsesRv.getAdapter() == null) {
				//Data
				mPhotoListAdapter = new PhotoListAdapter();
				mPhotoListAdapter.setData(getData());
				//Sections
				SectionedGridRecyclerViewAdapter.Section[] sectionsArray = extractSections(getData());
				mSectionedAdapter = new SectionedGridRecyclerViewAdapter(this, R.layout.item_section, R.id.section_title_tv, mBinding.responsesRv, mPhotoListAdapter);
				mSectionedAdapter.setSections(sectionsArray);
				//Set
				mBinding.responsesRv.setAdapter(mSectionedAdapter);
			} else {
				//Data
				mPhotoListAdapter.setData(getData());
				//Sections
				SectionedGridRecyclerViewAdapter.Section[] sectionsArray = extractSections(getData());
				mSectionedAdapter.setSections(sectionsArray);
				//Update
				mSectionedAdapter.notifyDataSetChanged();
				mPhotoListAdapter.notifyDataSetChanged();
			}
			getBinding().noResultsTv.setVisibility(getData().isEmpty() ?
			                                       View.VISIBLE :
			                                       View.GONE);


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


	//Load photos, yeae or month < 0 means load last three days
	//otherwise try to load photo(s) with yyyy-month-day or yyyy-month .
	protected void loadPhotoList(int year, int month, int day, String timeZone) {
		String reqId = UUID.randomUUID()
		                   .toString();
		if (year < 0 || month < 0) {
			RequestPhotoLastThreeList requestPhotoLastThreeList = new RequestPhotoLastThreeList();
			requestPhotoLastThreeList.setReqId(reqId);
			requestPhotoLastThreeList.setTimeZone(timeZone);
			App.Instance.getApiManager()
			            .execAsync(AppGuardService.Retrofit.create(Api.class)
			                                               .getPhotoListLast3Days(requestPhotoLastThreeList), requestPhotoLastThreeList);
		} else {
			if (day == DatePickerDialogFragment.IGNORED_DAY) {
				RequestPhotoList requestPhotoList = new RequestPhotoList();
				requestPhotoList.setReqId(reqId);
				requestPhotoList.setYear(year);
				requestPhotoList.setMonth(month);
				requestPhotoList.setTimeZone(timeZone);
				App.Instance.getApiManager()
				            .execAsync(AppGuardService.Retrofit.create(Api.class)
				                                               .getPhotoMonthList(requestPhotoList), requestPhotoList);
			} else {
				List<String> keywords = new ArrayList<>(1);
				keywords.add(String.format(Locale.getDefault(), "%d-%d-%d", year, month, day));
				RequestPhotoDayList dayListRequest = new RequestPhotoDayList();
				dayListRequest.setReqId(reqId);
				dayListRequest.setDateTimes(keywords);
				dayListRequest.setTimeZone(Calendar.getInstance()
				                                   .getTimeZone()
				                                   .getID());
				App.Instance.getApiManager()
				            .execAsync(AppGuardService.Retrofit.create(Api.class)
				                                               .getPhotoList(dayListRequest), dayListRequest);
			}
		}
	}


	@Override
	protected void initDataBinding() {
		mBinding = DataBindingUtil.setContentView(this, LAYOUT);
		setUpErrorHandling((ViewGroup) findViewById(R.id.error_content));
		View stickyV = mBinding.getRoot()
		                       .findViewById(com.chopping.R.id.err_sticky_container);
		((CoordinatorLayout.LayoutParams) mBinding.getRoot()
		                                          .findViewById(com.chopping.R.id.err_sticky_container)
		                                          .getLayoutParams()).gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
		ViewCompat.setElevation(stickyV, getResources().getDimension(R.dimen.ab_elevation));

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


	private void sendAppInvitation() {
		String invitationTitle = getString(R.string.lbl_share_app_title);
		String invitationMessage = getString(R.string.invitation_message);
		String invitationDeepLink = getString(R.string.lbl_store_url, getPackageName());
		String invitationCustomImage = getString(R.string.invitation_custom_image);
		String invitationCta = getString(R.string.invitation_cta);


		Intent intent = new AppInviteInvitation.IntentBuilder(invitationTitle).setMessage(invitationMessage)
		                                                                      .setDeepLink(Uri.parse(invitationDeepLink))
		                                                                      .setCustomImage(Uri.parse(invitationCustomImage))
		                                                                      .setCallToActionText(invitationCta)
		                                                                      .build();
		startActivityForResult(intent, REQUEST_INVITE);
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
			case R.id.action_invite:
				sendAppInvitation();
				break;
		}
		return super.onMenuItemClick(item);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		new GoogleApiClient.Builder(this).addApi(AppInvite.API)
		                                 .enableAutoManage(this, this)
		                                 .build();
		initAdmob();
		mBinding.toolbar.setLogo(R.drawable.ic_logo_toolbar);
		mBinding.loadNewPhotosBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				loadPhotoList(-1, -1, -1, null);
				view.setVisibility(View.GONE);
				mBinding.contentSrl.setRefreshing(true);
			}
		});
		mBinding.searchFab.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Fragment fragment = DatePickerDialogFragment.newInstance(new ResultReceiver(new Handler(getMainLooper())) {
					@Override
					protected void onReceiveResult(int resultCode, Bundle resultData) {
						super.onReceiveResult(resultCode, resultData);
						getSupportFragmentManager().popBackStack();
						switch (resultCode) {
							case RESULT_CODE:

								int year = resultData.getInt(DatePickerDialogFragment.EXTRAS_YEAR);
								int month = resultData.getInt(DatePickerDialogFragment.EXTRAS_MONTH);
								int day = resultData.getInt(DatePickerDialogFragment.EXTRAS_DAY_OF_MONTH);

								if (!validateDateTimeSelection(year, month, day, mBinding.errorContent)) {
									return;
								}
								if (day == DatePickerDialogFragment.IGNORED_DAY) {
									SearchResultActivity.showInstance(AbstractMainActivity.this, String.format(Locale.getDefault(), "%d-%d", year, month));
								} else {
									SearchResultActivity.showInstance(AbstractMainActivity.this, String.format(Locale.getDefault(), "%d-%d-%d", year, month, day));
								}
								break;
						}
					}
				});
				getSupportFragmentManager().beginTransaction()
				                           .add(R.id.dialog_fl, fragment)
				                           .addToBackStack(null)
				                           .commit();
				mBinding.dialogFl.show();
				mBinding.searchFab.hide();
			}
		});

		getSupportFragmentManager().addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
			@Override
			public void onBackStackChanged() {
				if (getSupportFragmentManager().findFragmentById(R.id.dialog_fl) == null) {
					mBinding.dialogFl.hide();
					mBinding.searchFab.show();
				}
			}
		});


		mBinding.responsesRv.setHasFixedSize(true);
		mBinding.responsesRv.addOnScrollListener(new RecyclerView.OnScrollListener() {
			@Override
			public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
				//Scrolling up and down can hidden and show the FAB.
				float y = ViewCompat.getY(recyclerView);
				if (y < dy) {
					if (mBinding.searchFab.isShown()) {
						mBinding.searchFab.hide();
					}
				} else {
					boolean shouldShow = getSupportFragmentManager().findFragmentById(R.id.dialog_fl) == null;
					if (!mBinding.searchFab.isShown() && shouldShow) {
						mBinding.searchFab.show();
					}
				}

			}
		});
	}


	protected void openPhoto(final PhotoListAdapter.ItemViewHolder viewHolder, int pos) {
		if (pos != RecyclerView.NO_POSITION) {
			try {
				ValueAnimatorCompat animator = AnimatorCompatHelper.emptyValueAnimator();
				animator.setDuration(TransitCompat.ANIM_DURATION * 3);
				animator.setTarget(viewHolder.mBinding.thumbnailIv);
				animator.addUpdateListener(new AnimatorUpdateListenerCompat() {
					private final float oldAlpha = 1;
					private final float endAlpha = 0;
					private final Interpolator interpolator2 = new BakedBezierInterpolator();

					@Override
					public void onAnimationUpdate(ValueAnimatorCompat animation) {
						float fraction = interpolator2.getInterpolation(animation.getAnimatedFraction());

						//Set background alpha
						float alpha = oldAlpha + (fraction * (endAlpha - oldAlpha));
						ViewCompat.setAlpha(viewHolder.mBinding.thumbnailIv, alpha);
					}
				});
				animator.start();

				int[] screenLocation = new int[2];
				viewHolder.mBinding.thumbnailIv.getLocationOnScreen(screenLocation);
				Thumbnail thumbnail = new Thumbnail(screenLocation[1], screenLocation[0], viewHolder.mBinding.thumbnailIv.getWidth(), viewHolder.mBinding.thumbnailIv.getHeight());
				EventBus.getDefault()
				        .post(new OpenPhotoEvent(getData().get(pos), thumbnail, viewHolder.mBinding));
			} catch (NullPointerException ex) {
				EventBus.getDefault()
				        .post(new OpenPhotoEvent(getData().get(pos), null, null));
			}
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		revertLastSelectedListItemView();
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				if (mBinding.dialogFl.isContentShown()) {
					mBinding.dialogFl.show();
				}
			}
		}, 500);
	}

	private void buildListView(Context cxt, RecyclerView recyclerView) {
		DeviceUtils.ScreenSize screenSize = DeviceUtils.getScreenSize(App.Instance);
		int width = screenSize.Width;
		LL.d("Screen width: " + width);
		float basic = cxt.getResources()
		                 .getDimension(R.dimen.basic_card_width);
		LL.d("Basic: " + basic);
		float div = width / basic;
		LL.d("Div: " + div);
		BigDecimal cardCount = new BigDecimal(div).setScale(0, BigDecimal.ROUND_HALF_UP);
		LL.d("CardCount: " + cardCount);
		recyclerView.setLayoutManager(new GridLayoutManager(cxt, cardCount.intValue()));
	}


	private void revertLastSelectedListItemView() {
		if (mItemBinding == null) {
			return;
		}
		ValueAnimatorCompat animator = AnimatorCompatHelper.emptyValueAnimator();
		animator.setDuration(TransitCompat.ANIM_DURATION * 4);
		animator.addUpdateListener(new AnimatorUpdateListenerCompat() {
			private final float oldAlpha = 0;
			private final float endAlpha = 1;
			private final Interpolator interpolator2 = new BakedBezierInterpolator();

			@Override
			public void onAnimationUpdate(ValueAnimatorCompat animation) {
				if(mItemBinding == null || mItemBinding.thumbnailIv == null ) {
					return;
				}
				float fraction = interpolator2.getInterpolation(animation.getAnimatedFraction());
				//Set background alpha
				float alpha = oldAlpha + (fraction * (endAlpha - oldAlpha));
				ViewCompat.setAlpha(mItemBinding.thumbnailIv, alpha);
			}
		});
		animator.start();
	}

	@Override
	protected boolean shouldLoadLocal(Context cxt) {
		boolean notEmptyLocal = !Realm.getDefaultInstance()
		                              .isEmpty();
		boolean defaultShouldLoadLocal = super.shouldLoadLocal(cxt);
		boolean shouldLoadLocal = defaultShouldLoadLocal || notEmptyLocal;
		if (shouldLoadLocal) {
			onRestApiSuccess();
			if (notEmptyLocal) {
				Date maxDate = Realm.getDefaultInstance()
				                    .where(getDataClazz())
				                    .maximumDate("date");
				Calendar maxCalendar = Calendar.getInstance();
				if (maxDate != null) {
					maxCalendar.setTime(maxDate);
				}
				maxCalendar.set(Calendar.HOUR_OF_DAY, 0);
				maxCalendar.set(Calendar.MINUTE, 0);
				maxCalendar.set(Calendar.SECOND, 0);
				maxCalendar.set(Calendar.MILLISECOND, 0);
				Calendar nowCalendar = Calendar.getInstance();
				nowCalendar.set(Calendar.HOUR_OF_DAY, 0);
				nowCalendar.set(Calendar.MINUTE, 0);
				nowCalendar.set(Calendar.SECOND, 0);
				nowCalendar.set(Calendar.MILLISECOND, 0);
				long diff = nowCalendar.getTimeInMillis() - maxCalendar.getTimeInMillis();
				boolean lessThanThreeDays = diff <= DAYS.toMillis(DURATION_THREE_DAYS) && diff >= DAYS.toMillis(DURATION_ONE_DAY);
				mBinding.loadNewPhotosBtn.setVisibility(lessThanThreeDays ?
				                                        View.VISIBLE :
				                                        View.GONE);
				if (diff >= DAYS.toMillis(DURATION_FOUR_DAYS) && !defaultShouldLoadLocal) {
					shouldLoadLocal = false;
				}
			}
		}
		LL.d("shouldLoadLocal:" + shouldLoadLocal);
		return shouldLoadLocal;
	}


	@Override
	public void onConnectionFailed(ConnectionResult connectionResult) {
		Snackbar.make(mBinding.errorContent, R.string.google_play_services_error, Snackbar.LENGTH_LONG)
		        .show();
	}


	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == REQUEST_INVITE) {
			if (resultCode == RESULT_OK) {
				// Get the invitation IDs of all sent messages
//				String[] ids = AppInviteInvitation.getInvitationIds(resultCode, data);
			} else {
				Snackbar.make(mBinding.errorContent, R.string.send_failed, Snackbar.LENGTH_LONG)
				        .show();
			}
		}
	}

	/**
	 * Ads for the app.
	 */
	private void initAdmob() {
		Prefs prefs = Prefs.getInstance();
		int curTime = prefs.getAdsCount();
		int adsTimes = 5;
		if (curTime % adsTimes == 0) {
			// Create an ad.
			mInterstitialAd = new InterstitialAd(this);
			mInterstitialAd.setAdUnitId(getString(R.string.interstitial_ad_unit_id));
			// Create ad request.
			AdRequest adRequest = new AdRequest.Builder().build();
			// Begin loading your interstitial.
			mInterstitialAd.setAdListener(new AdListener() {
				@Override
				public void onAdLoaded() {
					super.onAdLoaded();
					displayInterstitial();
				}
			});
			mInterstitialAd.loadAd(adRequest);
		}
		curTime++;
		prefs.setAdsCount(curTime);
	}

	/**
	 * Invoke displayInterstitial() when you are ready to display an interstitial.
	 */
	public void displayInterstitial() {
		if (mInterstitialAd.isLoaded()) {
			mInterstitialAd.show();
		}
	}

	private void initPull2Load(SwipeRefreshLayout swipeRefreshLayout) {
		int actionbarHeight = Utils.getActionBarHeight(App.Instance);
		swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
			@Override
			public void onRefresh() {
				loadList();
			}
		});
		swipeRefreshLayout.setColorSchemeResources(R.color.c_refresh_1, R.color.c_refresh_2, R.color.c_refresh_3, R.color.c_refresh_4);
		swipeRefreshLayout.setProgressViewEndTarget(true, actionbarHeight * 2);
		swipeRefreshLayout.setProgressViewOffset(false, 0, actionbarHeight * 2);
		swipeRefreshLayout.setRefreshing(true);
	}

}
