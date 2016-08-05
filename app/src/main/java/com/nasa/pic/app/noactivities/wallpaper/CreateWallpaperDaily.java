package com.nasa.pic.app.noactivities.wallpaper;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.nasa.pic.app.App;

import java.util.Calendar;

public final class CreateWallpaperDaily {
	private static PendingIntent createDailyPendingIntent() {
		return PendingIntent.getBroadcast(App.Instance, SetWallpaperService.SET_REQUEST_CODE, new Intent(App.Instance, DailyAlarmService.class), PendingIntent.FLAG_UPDATE_CURRENT);
	}

	public static void setDailyUpdate(Context cxt, long plan) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			((AlarmManager) cxt.getSystemService(Context.ALARM_SERVICE)).setExact(AlarmManager.RTC_WAKEUP, plan, createDailyPendingIntent());
		} else {
			((AlarmManager) cxt.getSystemService(Context.ALARM_SERVICE)).setRepeating(AlarmManager.RTC_WAKEUP,
			                                                                          Calendar.getInstance()
			                                                                                  .getTimeInMillis(),
			                                                                          plan,
			                                                                          createDailyPendingIntent());
		}
	}

	public static void cancelDailyUpdate(Context cxt) {
		((AlarmManager) cxt.getSystemService(Context.ALARM_SERVICE)).cancel(createDailyPendingIntent());
	}
}
