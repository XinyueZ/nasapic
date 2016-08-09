package com.nasa.pic.app.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;

import com.nasa.pic.app.fragments.DailyTimePlanBottomDialogFragment;

public final class MainActivity extends AbstractMainActivity {
	private static final String EXTRAS_SET_WALLPAPER_DAILY = MainActivity.class.getName() + ".EXTRAS.set.wallpaper.daily";

	public static void showInstance(Activity cxt) {
		showInstance(cxt, false);
	}

	public static void showInstance(Activity cxt, boolean setDailyWallpaper) {
		ActivityCompat.startActivity(cxt, createActivityIntent(cxt, setDailyWallpaper), null);
	}

	public static Intent createActivityIntent(Context cxt, boolean setDailyWallpaper) {
		Intent intent = new Intent(cxt, MainActivity.class);
		intent.putExtra(EXTRAS_SET_WALLPAPER_DAILY, setDailyWallpaper);
		intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
		return intent;
	}

	@Override
	protected final void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		handleIntent(getIntent());
	}

	@Override
	protected final void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		setIntent(intent);
		handleIntent(intent);
	}

	private void handleIntent(Intent intent) {
		if(intent.getBooleanExtra(EXTRAS_SET_WALLPAPER_DAILY, false)) {
			com.nasa.pic.utils.Utils.showDialogFragment(getSupportFragmentManager(), DailyTimePlanBottomDialogFragment.newInstance(true), null);
		}
	}
}
