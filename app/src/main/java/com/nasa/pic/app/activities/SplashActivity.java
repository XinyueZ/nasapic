package com.nasa.pic.app.activities;

import android.Manifest.permission;
import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import com.chopping.activities.BaseActivity;
import com.chopping.application.BasicPrefs;
import com.google.android.gms.gcm.GcmNetworkManager;
import com.google.android.gms.gcm.PeriodicTask;
import com.nasa.pic.R;
import com.nasa.pic.app.noactivities.AppGuardService;
import com.nasa.pic.databinding.ActivitySplashBinding;
import com.nasa.pic.utils.Prefs;
import com.nasa.pic.utils.Utils;

import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnPermissionDenied;
import permissions.dispatcher.RuntimePermissions;

/**
 * First screen of application.
 *
 * @author Xinyue Zhao
 */
@RuntimePermissions
public final class SplashActivity extends BaseActivity {
	/**
	 * Main layout for this component.
	 */
	private static final int LAYOUT = R.layout.activity_splash;
	/**
	 * Data-binding.
	 */
	private ActivitySplashBinding mBinding;

	private boolean mAlive;

	private void showSplash() {
		mBinding.splashFl.post(new Runnable() {
			@Override
			public void run() {
				mBinding.splashFl.show(2000);
			}
		});
	}

	private void permissionTest() {
		mBinding.splashFl.postDelayed(new Runnable() {
			@Override
			public void run() {
				SplashActivityPermissionsDispatcher.getPermissionsWithCheck(SplashActivity.this);
			}
		}, 2500);
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
			@NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		// delegate the permission handling to generated method
		SplashActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
	}


	@NeedsPermission({ permission.READ_PHONE_STATE, permission.WRITE_EXTERNAL_STORAGE, permission.READ_EXTERNAL_STORAGE  })
	void getPermissions() {
		if (mAlive) {
			MainActivity.showInstance(this);
		}
		finish();
	}


	@OnPermissionDenied({ permission.READ_PHONE_STATE, permission.WRITE_EXTERNAL_STORAGE, permission.READ_EXTERNAL_STORAGE })
	void noPermissions() {
		Snackbar.make(findViewById(R.id.error_content), R.string.msg_permission_prompt, Snackbar.LENGTH_INDEFINITE)
				.setAction(R.string.btn_app_close, new OnClickListener() {
					@Override
					public void onClick(View v) {
						ActivityCompat.finishAffinity(SplashActivity.this);
					}
				}).show();

	}


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		mAlive  = true;
		Utils.fullScreen(this);
		super.onCreate(savedInstanceState);
		mBinding = DataBindingUtil.setContentView(this, LAYOUT);
		setUpErrorHandling((ViewGroup) findViewById(R.id.error_content));
		mBinding.splashFl.hide();
	}

	@Override
	protected void onAppConfigLoaded() {
		super.onAppConfigLoaded();
		startAppGuardService(this);
		showSplash();
		permissionTest();
	}


	@Override
	protected void onAppConfigIgnored() {
		super.onAppConfigIgnored();
		startAppGuardService(this);
		showSplash();
		permissionTest();
	}

	@Override
	protected BasicPrefs getPrefs() {
		return Prefs.getInstance();
	}


	public static void startAppGuardService(Context cxt) {
		AppGuardService.buildRetrofit();
		long scheduleSec = Prefs.API_SERVER_SWITCH_SCHEDULE;
		long flexSecs = 60L;
		String tag = System.currentTimeMillis() + "";
		PeriodicTask scheduleTask = new PeriodicTask.Builder().setService(AppGuardService.class).setPeriod(scheduleSec)
				.setFlex(flexSecs).setTag(tag).setPersisted(true).setRequiredNetwork(
						com.google.android.gms.gcm.Task.NETWORK_STATE_ANY).setRequiresCharging(false).build();
		GcmNetworkManager.getInstance(cxt).schedule(scheduleTask);
	}

	@Override
	protected void onDestroy() {
		mAlive = false;
		super.onDestroy();
	}

}
