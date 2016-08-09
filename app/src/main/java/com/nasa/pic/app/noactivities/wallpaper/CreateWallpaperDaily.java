package com.nasa.pic.app.noactivities.wallpaper;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.StringRes;

import com.chopping.application.LL;
import com.nasa.pic.R;
import com.nasa.pic.app.App;
import com.nasa.pic.events.WallpaperDailyChangedEvent;
import com.nasa.pic.utils.Prefs;

import java.util.Calendar;

import de.greenrobot.event.EventBus;

public final class CreateWallpaperDaily {
	private static PendingIntent createDailyPendingIntent() {
		return PendingIntent.getBroadcast(App.Instance, SetWallpaperService.SET_REQUEST_CODE, new Intent(App.Instance, DailyAlarmService.class), PendingIntent.FLAG_UPDATE_CURRENT);
	}

	public static void setDailyUpdate(Context cxt, long plan) {
		Prefs prefs = Prefs.getInstance();
		if (!prefs.doesWallpaperChangeDaily()) {
			prefs.setWallpaperChangeDaily(true);
		}
		EventBus.getDefault()
		        .post(new WallpaperDailyChangedEvent(String.format(App.Instance.getString(R.string.wallpaper_update_frequency), App.Instance.getString(getCurrentSelectedPlanString()))));

		((AlarmManager) cxt.getSystemService(Context.ALARM_SERVICE)).setInexactRepeating(AlarmManager.RTC_WAKEUP,
		                                                                                 Calendar.getInstance()
		                                                                                         .getTimeInMillis() + plan,
		                                                                                 plan,
		                                                                                 createDailyPendingIntent());
		LL.d("setDailyUpdate");
	}

	public static void cancelDailyUpdate(Context cxt) {
		Prefs prefs = Prefs.getInstance();
		if (prefs.doesWallpaperChangeDaily()) {
			prefs.setWallpaperChangeDaily(false);
		}
		EventBus.getDefault()
		        .post(new WallpaperDailyChangedEvent(App.Instance.getString(R.string.wallpaper_cancel_wallpaper_update)));

		((AlarmManager) cxt.getSystemService(Context.ALARM_SERVICE)).cancel(createDailyPendingIntent());
		LL.d("cancelDailyUpdate");
	}

	private static
	@StringRes
	int getCurrentSelectedPlanString() {
		switch ((int) Prefs.getInstance()
		                   .getWallpaperDailyTimePlan()) {
			case 1:
				return R.string.wallpaper_daily_time_plan_1_hour;
			case 2:
				return R.string.wallpaper_daily_time_plan_2_hour;
			case 3:
				return R.string.wallpaper_daily_time_plan_3_hour;
			case 4:
				return R.string.wallpaper_daily_time_plan_4_hour;
			case 5:
				return R.string.wallpaper_daily_time_plan_5_hour;
			case 6:
				return R.string.wallpaper_daily_time_plan_6_hour;
			default:
				return R.string.wallpaper_daily_time_plan_3_hour;
		}
	}
}
