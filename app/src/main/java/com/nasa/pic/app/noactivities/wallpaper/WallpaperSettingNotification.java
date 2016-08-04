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


class WallpaperSettingNotification {
	private static final int WALLPAPER_CHANGED_NOTIFICATION_ID = 0x34;
	private static final int CHANGING_WALLPAPER_NOTIFICATION_ID = 0x35;

	@UiThread
	@MainThread
	static void createChangedNotification(Context context, CharSequence message) {
		Resources res = context.getResources();
		PendingIntent contentPendingIntent = PendingIntent.getActivities(context, 0, new Intent[] { new Intent(context, MainActivity.class) }, PendingIntent.FLAG_UPDATE_CURRENT);
		Intent undoChangeIntent = new Intent(context, SetWallpaperService.class);
		undoChangeIntent.putExtra(SetWallpaperService.EXTRA_UNDO_OPERATION, true);
		PendingIntent undoChangePendingIntent = PendingIntent.getService(context, SetWallpaperService.UNDO_REQUEST_CODE, undoChangeIntent, PendingIntent.FLAG_ONE_SHOT);
		String undoChangeButtonTitle = context.getResources()
		                                      .getString(R.string.wallpaper_change_undo);
		Notification notification = new NotificationCompat.Builder(context).setSmallIcon(R.drawable.ic_logo_splash)
		                                                                   .setLargeIcon(BitmapFactory.decodeResource(res, R.drawable.ic_logo_splash))
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
		PendingIntent contentPendingIntent = PendingIntent.getActivities(context, 0, new Intent[] { new Intent(context, MainActivity.class) }, PendingIntent.FLAG_UPDATE_CURRENT);
		Notification notification = new NotificationCompat.Builder(context).setSmallIcon(R.drawable.ic_logo_splash)
		                                                                   .setLargeIcon(BitmapFactory.decodeResource(res, R.drawable.ic_logo_splash))
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
		PendingIntent contentPendingIntent = PendingIntent.getActivities(context, 0, new Intent[] { new Intent(context, MainActivity.class) }, PendingIntent.FLAG_UPDATE_CURRENT);
		Notification notification = new NotificationCompat.Builder(context).setSmallIcon(R.drawable.ic_logo_splash)
		                                                                   .setLargeIcon(BitmapFactory.decodeResource(res, R.drawable.ic_logo_splash))
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

	@UiThread
	@MainThread
	static void removeChangingNotification(Context context) {
		NotificationManagerCompat.from(context)
		                         .cancel(WallpaperSettingNotification.CHANGING_WALLPAPER_NOTIFICATION_ID);
	}
}
