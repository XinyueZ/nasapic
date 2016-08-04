package com.nasa.pic.app.noactivities.wallpaper;

import android.app.Service;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;
import android.support.v4.os.AsyncTaskCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.chopping.utils.Utils;
import com.nasa.pic.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;


public final class SetWallpaperService extends Service {
	private static final String EXTRAS_PHOTO_LOCATION = SetWallpaperService.class.getName() + ".EXTRAS.photo.location";
	private static final String LAST_WALLPAPER_FILE_NAME = "last_wallpaper";
	static final String EXTRA_UNDO_OPERATION = "UNDO_OPERATION_EXTRA";
	static final int UNDO_REQUEST_CODE = 0x99;

	public static void createService(Context cxt, String photoLocation) {
		createService(cxt, photoLocation, false);
	}

	public static void createService(Context cxt, String photoLocation, boolean undo) {
		Intent intent = new Intent(cxt, SetWallpaperService.class);
		intent.putExtra(EXTRAS_PHOTO_LOCATION, photoLocation);
		intent.putExtra(EXTRA_UNDO_OPERATION, undo);
		cxt.startService(intent);
	}

	@Nullable
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}


	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		String url = intent.getStringExtra(EXTRAS_PHOTO_LOCATION);
		boolean undo = intent.getBooleanExtra(EXTRA_UNDO_OPERATION, false);
		WallpaperSettingNotification.createChangingNotification(this);
		if (undo) {
			try {
				undoLastWallpaperChangeSync(SetWallpaperService.this);
			} catch (IOException e1) {
				e1.printStackTrace();
			}

			WallpaperSettingNotification.removeChangingNotification(this);
			WallpaperSettingNotification.createUndoFinishedNotification(this, getString(R.string.wallpaper_undo_finished));
		} else {
			Glide.with(this)
			     .load(Utils.uriStr2URI(url)
			                .toASCIIString())
			     .asBitmap()
			     .diskCacheStrategy(DiskCacheStrategy.ALL)
			     .into(new SimpleTarget<Bitmap>() {
				     @Override
				     public void onResourceReady(Bitmap loadedBitmap, GlideAnimation<? super Bitmap> glideAnimation) {
					     AsyncTaskCompat.executeParallel(new AsyncTask<Bitmap, Void, Void>() {
						     @Override
						     protected Void doInBackground(Bitmap... bitmaps) {
							     Bitmap bitmap = bitmaps[0];
							     if (bitmap != null && !bitmap.isRecycled()) {
								     doChangeWallpaper(bitmap);
								     stopSelf();
							     }
							     return null;
						     }
					     }, loadedBitmap);
				     }
			     });
		}
		return super.onStartCommand(intent, flags, startId);
	}

	@WorkerThread
	private static void storeLastWallpaper(Context cxt) throws IOException {
		WallpaperManager wallpaperMgr = WallpaperManager.getInstance(cxt);
		File lastWallpaperFile = new File(cxt.getFilesDir(), LAST_WALLPAPER_FILE_NAME);
		if (!lastWallpaperFile.createNewFile()) {
			lastWallpaperFile.delete();
			lastWallpaperFile.createNewFile();
		}
		if (wallpaperMgr.getDrawable() instanceof BitmapDrawable) {
			FileOutputStream outputStream = cxt.openFileOutput(LAST_WALLPAPER_FILE_NAME, Context.MODE_PRIVATE);
			((BitmapDrawable) wallpaperMgr.getDrawable()).getBitmap()
			                                             .compress(Bitmap.CompressFormat.PNG, 100, outputStream);
			outputStream.close();
		}
	}


	@WorkerThread
	private static void undoLastWallpaperChangeSync(Context cxt) throws IOException {
		Bitmap bitmap = retrieveLastWallpaper(cxt);
		if (bitmap != null) {
			WallpaperManager wallpaperMgr = WallpaperManager.getInstance(cxt);
			wallpaperMgr.setWallpaperOffsetSteps(0.5f, 1.0f);
			wallpaperMgr.setBitmap(bitmap);
		}
	}

	@WorkerThread
	private static Bitmap retrieveLastWallpaper(Context cxt) throws IOException {
		File lastWallpaperFile = new File(cxt.getFilesDir(), LAST_WALLPAPER_FILE_NAME);
		if (lastWallpaperFile.exists() && lastWallpaperFile.length() > 0) {
			FileInputStream inputStream = null;
			try {
				inputStream = cxt.openFileInput(LAST_WALLPAPER_FILE_NAME);
				return BitmapFactory.decodeStream(inputStream);
			} finally {
				if (inputStream != null) {
					inputStream.close();
				}
			}
		} else {
			return null;
		}
	}


	@WorkerThread
	private void doChangeWallpaper(Bitmap loadedBitmap) {
		try {
			storeLastWallpaper(SetWallpaperService.this);
		} catch (IOException e) {
			e.printStackTrace();
		}
		WallpaperManager wallpaperMgr = WallpaperManager.getInstance(SetWallpaperService.this);
		wallpaperMgr.setWallpaperOffsetSteps(0.5f, 1.0f);
		try {
			wallpaperMgr.setBitmap(loadedBitmap);
		} catch (IOException e) {
			e.printStackTrace();
			try {
				undoLastWallpaperChangeSync(SetWallpaperService.this);
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
		WallpaperSettingNotification.removeChangingNotification(this);
		WallpaperSettingNotification.createChangedNotification(SetWallpaperService.this, SetWallpaperService.this.getString(R.string.wallpaper_changed));
	}
}
