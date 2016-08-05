package com.nasa.pic.app.noactivities.wallpaper;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.text.TextUtils;

import com.nasa.pic.ds.PhotoDB;
import com.nasa.pic.utils.Prefs;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import io.realm.Realm;
import io.realm.RealmResults;

public final class DailyAlarmService extends WakefulBroadcastReceiver {
	private static final String TAG = DailyAlarmService.class.getName();

	@Override
	public void onReceive(Context context, Intent intent) {
		final RealmResults<PhotoDB> photos = Realm.getDefaultInstance()
		                                          .where(PhotoDB.class)
		                                          .findAll();
		int size = photos.size();
		int photoIndex = randInt(0, size, size);
		for (int i = 0;
				i < size;
				i++) {
			PhotoDB photo = photos.get(photoIndex);
			if (!TextUtils.equals(photo.getType(), "image")) {
				continue;
			}
			String url = photo.getUrls()
			                  .getNormal();
			startWakefulService(context, SetWallpaperService.createServiceIntent(context, url, false));
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
				CreateWallpaperDaily.setDailyUpdate(context,
				                                    Prefs.getInstance()
				                                         .getWallpaperDailyTimePlan() * Prefs.TIME_BASE);
			}
			return;
		}
	}


	private int randInt(int min, int max, int size) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			return ThreadLocalRandom.current()
			                        .nextInt(0, size);
		} else {
			return new Random().nextInt((max - min) + 1) + min;
		}
	}
}
