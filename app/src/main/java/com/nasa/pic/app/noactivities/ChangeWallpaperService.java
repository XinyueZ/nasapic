package com.nasa.pic.app.noactivities;

import android.app.IntentService;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;


public final class ChangeWallpaperService extends IntentService {
	private static final String EXTRAS_PHOTO_LOCATION = ChangeWallpaperService.class.getName() + ".EXTRAS.photo.location";
	public static final String TAG = ChangeWallpaperService.class.getName();
	private static final String LAST_WALLPAPER_FILE_NAME = "last_wallpaper";

	public static void createService(Context cxt, String photoLocation) {
		Intent intent = new Intent(cxt, ChangeWallpaperService.class);
		intent.putExtra(EXTRAS_PHOTO_LOCATION, photoLocation);
		cxt.startService(intent);
	}

	public ChangeWallpaperService() {
		super(TAG);
	}

	@Override
	protected void onHandleIntent(Intent intent) {

	}

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


	private static void undoLastWallpaperChangeSync(Context cxt) throws IOException {
		Bitmap bitmap = retrieveLastWallpaper(cxt);
		if (bitmap != null) {
			WallpaperManager wallpaperMgr = WallpaperManager.getInstance(cxt);
			wallpaperMgr.setWallpaperOffsetSteps(0.5f, 1.0f);
			wallpaperMgr.setBitmap(bitmap);
		}
	}

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
}
