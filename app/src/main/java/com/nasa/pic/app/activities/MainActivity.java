package com.nasa.pic.app.activities;

import java.util.Calendar;
import java.util.UUID;

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
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog.Builder;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.nasa.pic.R;
import com.nasa.pic.api.Api;
import com.nasa.pic.app.App;
import com.nasa.pic.app.adapters.PhotoListAdapter;
import com.nasa.pic.app.fragments.AboutDialogFragment;
import com.nasa.pic.app.fragments.AboutDialogFragment.EulaConfirmationDialog;
import com.nasa.pic.app.fragments.AppListImpFragment;
import com.nasa.pic.app.noactivities.AppGuardService;
import com.nasa.pic.databinding.ActivityMainBinding;
import com.nasa.pic.ds.RequestPhotoList;
import com.nasa.pic.events.EULAConfirmedEvent;
import com.nasa.pic.events.EULARejectEvent;
import com.nasa.pic.utils.Prefs;

import io.realm.RealmObject;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import io.realm.Sort;

public class MainActivity extends AppBasicActivity implements NavigationView.OnNavigationItemSelectedListener {
	/**
	 * The menu to this view.
	 */
	private static final int MENU = R.menu.menu_main;

	/**
	 * Main layout for this component.
	 */
	private static final int LAYOUT = R.layout.activity_main;
	/**
	 * Data-binding.
	 */
	private ActivityMainBinding mBinding;
	//------------------------------------------------
	//Subscribes, event-handlers
	//------------------------------------------------


	/**
	 * Handler for {@link  EULARejectEvent}.
	 *
	 * @param e
	 * 		Event {@link  EULARejectEvent}.
	 */
	public void onEvent(EULARejectEvent e) {
		finish();
	}

	/**
	 * Handler for {@link EULAConfirmedEvent}
	 *
	 * @param e
	 * 		Event {@link  EULAConfirmedEvent}.
	 */
	public void onEvent(EULAConfirmedEvent e) {


	}
	//------------------------------------------------

	/**
	 * Show single instance of {@link MainActivity}
	 *
	 * @param cxt
	 * 		{@link Activity}.
	 */
	public static void showInstance(Activity cxt) {
		Intent intent = new Intent(cxt, MainActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
		ActivityCompat.startActivity(cxt, intent, null);
	}


	@Override
	public void onBackPressed() {
		DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
		if (drawer.isDrawerOpen(GravityCompat.START)) {
			drawer.closeDrawer(GravityCompat.START);
		} else {
			super.onBackPressed();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(MENU, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();

		//noinspection SimplifiableIfStatement
		switch (id) {
		case R.id.action_about:
			showDialogFragment(AboutDialogFragment.newInstance(this), null);
			break;
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		MenuItem menuShare = menu.findItem(R.id.action_share);
		android.support.v7.widget.ShareActionProvider provider =
				(android.support.v7.widget.ShareActionProvider) MenuItemCompat.getActionProvider(menuShare);

		String subject = getString(R.string.lbl_share_app_title);
		String text = getString(R.string.lbl_share_app_content, getString(R.string.application_name),
				Prefs.getInstance().getAppDownloadInfo());

		provider.setShareIntent(com.chopping.utils.Utils.getDefaultShareIntent(provider, subject, text));

		return super.onPrepareOptionsMenu(menu);
	}

	@SuppressWarnings("StatementWithEmptyBody")
	@Override
	public boolean onNavigationItemSelected(MenuItem item) {
		// Handle navigation view item clicks here.
		int id = item.getItemId();

		switch (id) {
		case R.id.action_more_photos:
			MorePhotosActivity.showInstance(this);
			break;
		case R.id.action_app_list:
			BottomSheetBehavior behavior = BottomSheetBehavior.from(mBinding.bottomSheet);
			behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
			break;
		}

		DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
		drawer.closeDrawer(GravityCompat.START);
		return true;
	}


	/**
	 * To confirm whether the validation of the Play-service of Google Inc.
	 */
	private void checkPlayService() {
		final int isFound = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
		if (isFound == ConnectionResult.SUCCESS) {//Ignore update.
			//The "End User License Agreement" must be confirmed before you use this application.
			if (!Prefs.getInstance().isEULAOnceConfirmed()) {
				showDialogFragment(new EulaConfirmationDialog(), null);
			}
		} else {
			new Builder(this).setTitle(R.string.application_name).setMessage(R.string.play_service).setCancelable(false)
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
					}).setCancelable(isFound == ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED).create().show();
		}
	}


	@Override
	protected void loadList() {
		Calendar calendar = Calendar.getInstance();
		int year = calendar.get(Calendar.YEAR);
		int month = calendar.get(Calendar.MONTH) + 1;
		String timeZone = calendar.getTimeZone().getID();

		loadPhotoList(year, month, timeZone);
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
				mBinding.setAdapter(new PhotoListAdapter());
			}
			if (mBinding.getAdapter().getData() == null) {
				mBinding.getAdapter().setData(getData());
			}
			mBinding.getAdapter().notifyDataSetChanged();
		}
	}


	@Override
	protected void sendPending() {

	}

	@Override
	protected void onRestApiSuccess() {
		mBinding.contentSrl.setRefreshing(false);
	}

	private void loadPhotoList(int year, int month, String timeZone) {
		RequestPhotoList requestPhotoList = new RequestPhotoList();
		requestPhotoList.setReqId(UUID.randomUUID().toString());
		requestPhotoList.setYear(year);
		requestPhotoList.setMonth(month);
		requestPhotoList.setTimeZone(timeZone);
		App.Instance.getApiManager().execAsync(
				AppGuardService.Retrofit.create(Api.class).getPhotoMonthList(requestPhotoList), requestPhotoList);
	}


	@Override
	protected void initDataBinding() {
		mBinding = DataBindingUtil.setContentView(this, LAYOUT);
		setUpErrorHandling((ViewGroup) findViewById(R.id.error_content));
		setSupportActionBar(mBinding.toolbar);

		//Pull-2-load indicator
		initPull2Load(mBinding.contentSrl);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		//FAB
		FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
		fab.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG).setAction("Action", null)
						.show();
			}
		});

		//Navi-drawer
		DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
		ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, mBinding.toolbar,
				R.string.navigation_drawer_open, R.string.navigation_drawer_close);
		drawer.addDrawerListener(toggle);
		toggle.syncState();
		NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
		navigationView.setNavigationItemSelectedListener(this);
	}



	/**
	 * Show all external applications links.
	 */
	private void showAppList() {
		getSupportFragmentManager().beginTransaction().replace(R.id.app_list_fl, AppListImpFragment.newInstance(this))
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
}
