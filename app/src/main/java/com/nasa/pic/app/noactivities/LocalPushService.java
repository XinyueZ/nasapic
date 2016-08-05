package com.nasa.pic.app.noactivities;

import com.google.android.gms.gcm.GcmNetworkManager;
import com.google.android.gms.gcm.GcmTaskService;
import com.google.android.gms.gcm.TaskParams;
import com.nasa.pic.app.noactivities.wallpaper.WallpaperSettingNotification;
import com.nasa.pic.utils.Prefs;


public final class LocalPushService extends GcmTaskService {
	@Override
	public int onRunTask(TaskParams taskParams) {
		if(!Prefs.getInstance().doesWallpaperChangeDaily()) {
			WallpaperSettingNotification.createWallpaperDailyRemind(this);
		}
		return GcmNetworkManager.RESULT_SUCCESS;
	}
}
