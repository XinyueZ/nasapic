package com.nasa.pic.app.activities;

import android.Manifest.permission;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;

import com.chopping.activities.BaseActivity;
import com.chopping.application.BasicPrefs;
import com.nasa.pic.R;
import com.nasa.pic.utils.Prefs;

import permissions.dispatcher.DeniedPermissions;
import permissions.dispatcher.NeedsPermissions;
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

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
			@NonNull int[] grantResults) {
		// delegate the permission handling to generated method
		SplashActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
	}


	@NeedsPermissions({ permission.READ_PHONE_STATE, permission.WRITE_EXTERNAL_STORAGE })
	void getPermissions() {
		MainActivity.showInstance(this);
	}


	@DeniedPermissions({ permission.READ_PHONE_STATE, permission.WRITE_EXTERNAL_STORAGE })
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
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		super.onCreate(savedInstanceState);
		setContentView(LAYOUT);
	}

	@Override
	protected void onAppConfigLoaded() {
		super.onAppConfigLoaded();
		SplashActivityPermissionsDispatcher.getPermissionsWithCheck(this);
	}

	@Override
	protected void onAppConfigIgnored() {
		super.onAppConfigIgnored();
		SplashActivityPermissionsDispatcher.getPermissionsWithCheck(this);
	}

	@Override
	protected BasicPrefs getPrefs() {
		return Prefs.getInstance();
	}
}
