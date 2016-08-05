package com.nasa.pic.app.noactivities.wallpaper;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.support.annotation.MainThread;
import android.support.annotation.UiThread;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.NotificationCompat;

import com.nasa.pic.R;
import com.nasa.pic.app.activities.MainActivity;
import com.nasa.pic.events.WallpaperChangedEvent;
import com.nasa.pic.events.WallpaperChangingEvent;
import com.nasa.pic.events.WallpaperUndoEvent;

import de.greenrobot.event.EventBus;

import static android.support.v4.app.NotificationCompat.PRIORITY_DEFAULT;
import static android.support.v4.app.NotificationCompat.PRIORITY_MAX;


public final class WallpaperSettingNotification {
	private static final int WALLPAPER_CHANGED_NOTIFICATION_ID = 0x34;
	private static final int CHANGING_WALLPAPER_NOTIFICATION_ID = 0x35;
	private static final int REMIND_NOTIFICATION_ID = 0x35;
	private static final int SMALL_ICON = R.drawable.ic_logo_toolbar;
	private static final int LARGE_ICON = R.drawable.ic_logo_splash;

	@UiThread
	@MainThread
	static void createChangedNotification(Context context, CharSequence message) {
		Resources res = context.getResources();
		PendingIntent contentPendingIntent = PendingIntent.getActivities(context, 0, new Intent[] { MainActivity.createActivityIntent(context, false) }, PendingIntent.FLAG_UPDATE_CURRENT);
		PendingIntent undoChangePendingIntent = PendingIntent.getService(context,
		                                                                 SetWallpaperService.UNDO_REQUEST_CODE,
		                                                                 SetWallpaperService.createServiceIntent(context, null, true),
		                                                                 PendingIntent.FLAG_ONE_SHOT);
		String undoChangeButtonTitle = context.getResources()
		                                      .getString(R.string.wallpaper_change_undo);
		Notification notification = new NotificationCompat.Builder(context).setSmallIcon(SMALL_ICON)
		                                                                   .setLargeIcon(BitmapFactory.decodeResource(res, LARGE_ICON))
		                                                                   .setColor(ContextCompat.getColor(context, R.color.colorPrimary))
		                                                                   .setContentTitle(res.getString(R.string.application_name))
		                                                                   .setContentText(message)
		                                                                   .setContentIntent(contentPendingIntent)
		                                                                   .setPriority(PRIORITY_MAX)
		                                                                   .setVibrate(new long[] { 1000 })
		                                                                   .addAction(android.R.drawable.ic_menu_revert, undoChangeButtonTitle, undoChangePendingIntent)
		                                                                   .build();
		NotificationManagerCompat.from(context)
		                         .notify(WALLPAPER_CHANGED_NOTIFICATION_ID, notification);
		if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
			EventBus.getDefault()
			        .post(new WallpaperChangedEvent(message));
		}

	}

	@UiThread
	@MainThread
	static void createUndoFinishedNotification(Context context, CharSequence message) {
		Resources res = context.getResources();
		PendingIntent contentPendingIntent = PendingIntent.getActivities(context, 0, new Intent[] { MainActivity.createActivityIntent(context, false)}, PendingIntent.FLAG_UPDATE_CURRENT);
		Notification notification = new NotificationCompat.Builder(context).setSmallIcon(SMALL_ICON)
		                                                                   .setLargeIcon(BitmapFactory.decodeResource(res, LARGE_ICON))
		                                                                   .setColor(ContextCompat.getColor(context, R.color.colorPrimary))
		                                                                   .setContentTitle(res.getString(R.string.application_name))
		                                                                   .setContentText(message)
		                                                                   .setPriority(PRIORITY_MAX)
		                                                                   .setVibrate(new long[] { 1000 })
		                                                                   .setContentIntent(contentPendingIntent)
		                                                                   .build();
		NotificationManagerCompat.from(context)
		                         .notify(WALLPAPER_CHANGED_NOTIFICATION_ID, notification);
		if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
			EventBus.getDefault()
			        .post(new WallpaperUndoEvent(message));
		}
	}

	@UiThread
	@MainThread
	static void createChangingNotification(Context context) {
		String message = context.getString(R.string.wallpaper_changing);
		Resources res = context.getResources();
		PendingIntent contentPendingIntent = PendingIntent.getActivities(context, 0, new Intent[] { MainActivity.createActivityIntent(context, false) }, PendingIntent.FLAG_UPDATE_CURRENT);
		Notification notification = new NotificationCompat.Builder(context).setSmallIcon(SMALL_ICON)
		                                                                   .setLargeIcon(BitmapFactory.decodeResource(res, LARGE_ICON))
		                                                                   .setColor(ContextCompat.getColor(context, R.color.colorPrimary))
		                                                                   .setContentTitle(res.getString(R.string.application_name))
		                                                                   .setContentText(message)
		                                                                   .setPriority(PRIORITY_DEFAULT)
		                                                                   .setVibrate(new long[] { 1000 })
		                                                                   .setContentIntent(contentPendingIntent)
		                                                                   .build();
		NotificationManagerCompat.from(context)
		                         .notify(CHANGING_WALLPAPER_NOTIFICATION_ID, notification);
		if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
			EventBus.getDefault()
			        .post(new WallpaperChangingEvent(message));
		}
	}

	public static void createWallpaperDailyRemind(Context context) {
		String message = context.getString(R.string.wallpaper_remind);
		Resources res = context.getResources();
		PendingIntent contentPendingIntent = PendingIntent.getActivities(context, 0, new Intent[] { MainActivity.createActivityIntent(context, true)}, PendingIntent.FLAG_UPDATE_CURRENT);
		Notification notification = new NotificationCompat.Builder(context).setSmallIcon(SMALL_ICON)
		                                                                   .setLargeIcon(BitmapFactory.decodeResource(res, LARGE_ICON))
		                                                                   .setColor(ContextCompat.getColor(context, R.color.colorPrimary))
		                                                                   .setContentTitle(res.getString(R.string.application_name))
		                                                                   .setContentText(message)
		                                                                   .setPriority(PRIORITY_MAX)
		                                                                   .setVibrate(new long[] { 1000 })
		                                                                   .setContentIntent(contentPendingIntent)
		                                                                   .build();
		NotificationManagerCompat.from(context)
		                         .notify(REMIND_NOTIFICATION_ID, notification);
	}

	@UiThread
	@MainThread
	static void removeChangingNotification(Context context) {
		NotificationManagerCompat.from(context)
		                         .cancel(WallpaperSettingNotification.REMIND_NOTIFICATION_ID);
		NotificationManagerCompat.from(context)
		                         .cancel(WallpaperSettingNotification.CHANGING_WALLPAPER_NOTIFICATION_ID);
	}
}
