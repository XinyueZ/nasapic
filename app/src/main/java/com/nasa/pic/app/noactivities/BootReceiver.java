package com.nasa.pic.app.noactivities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.nasa.pic.app.noactivities.wallpaper.CreateWallpaperDaily;
import com.nasa.pic.utils.Prefs;

/**
 * Handling device boot by {@link BroadcastReceiver}.
 *
 * @author Xinyue Zhao
 */
public final class BootReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		final Prefs prefs = Prefs.getInstance();
		if (prefs.doesWallpaperChangeDaily()) {
			CreateWallpaperDaily.setDailyUpdate(context, prefs.getWallpaperDailyTimePlan() * Prefs.TIME_BASE);
		}
	}
}

