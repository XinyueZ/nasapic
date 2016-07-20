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
import android.support.v4.animation.AnimatorCompatHelper;
import android.support.v4.animation.AnimatorUpdateListenerCompat;
import android.support.v4.animation.ValueAnimatorCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.os.ResultReceiver;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AlertDialog.Builder;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Interpolator;

import com.chopping.application.LL;
import com.chopping.utils.DeviceUtils;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
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

	private SectionedGridRecyclerViewAdapter mSectionedAdapter;
	private ItemBinding mItemBinding;


	protected PhotoListAdapter mPhotoListAdapter;

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
		final PhotoListAdapter.ViewHolder viewHolder = e.getViewHolder();
		int pos = mSectionedAdapter.sectionedPositionToPosition(viewHolder.getAdapterPosition());
		openPhoto(viewHolder, pos);
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
		int currentMonth = calendar.get(Calendar.MONTH);
		int thisMonth = currentMonth + 1;
		int lastMonth = currentMonth;
		int beforeLastMonth = currentMonth - 1;
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
		if (isDataLoaded()) {
			if (mBinding.responsesRv.getAdapter() == null) {
				//Data
				mPhotoListAdapter = new PhotoListAdapter( );
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
		mBinding.toolbar.setLogo(R.drawable.ic_logo_toolbar);
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


	protected void openPhoto(final PhotoListAdapter.ViewHolder viewHolder, int pos) {
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
		boolean shouldLoadLocal = super.shouldLoadLocal(cxt) || !Realm.getDefaultInstance()
		                                                              .isEmpty();
		if (shouldLoadLocal) {
			onRestApiSuccess();
			LL.d("shouldLoadLocal: " + shouldLoadLocal);
		} else {
			LL.d("shouldLoadLocal: " + shouldLoadLocal);
		}
		return shouldLoadLocal;
	}


}
