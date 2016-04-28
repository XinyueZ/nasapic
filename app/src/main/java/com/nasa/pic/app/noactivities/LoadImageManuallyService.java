package com.nasa.pic.app.noactivities;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.os.ResultReceiver;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.nasa.pic.R;
import com.nasa.pic.app.App;
import com.nasa.pic.app.fragments.TvModeDetailFragment;

import java.util.concurrent.ExecutionException;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class LoadImageManuallyService
		extends IntentService {
	private static final String EXTRAS_IMG_URL  = "com.nasa.pic.app.noactivities.extra.LOAD_IMAGE_URL";
	private static final String EXTRAS_RECEIVER = "com.nasa.pic.app.noactivities.extra.LOAD_IMAGE_RECEIVER";

	public static void startService(Context context,
	                                String url,
	                                ResultReceiver resultReceiver) {
		Intent intent = new Intent(context,
		                           LoadImageManuallyService.class);
		intent.putExtra(EXTRAS_IMG_URL,
		                url);
		intent.putExtra(EXTRAS_RECEIVER,
		                resultReceiver);
		context.startService(intent);
	}

	public LoadImageManuallyService() {
		super("LoadImageManually");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		if (intent != null) {
			String         url            = intent.getStringExtra(EXTRAS_IMG_URL);
			ResultReceiver resultReceiver = intent.getParcelableExtra(EXTRAS_RECEIVER);
			Bitmap         bitmap;
			try {
				Resources resources = getResources();
				bitmap = Glide.with(App.Instance)
				              .load(url)
				              .asBitmap()
				              .diskCacheStrategy(DiskCacheStrategy.ALL)
				              .into(resources.getDimensionPixelSize(R.dimen.tv_mode_detail_photo_width),
				                    resources.getDimensionPixelSize(R.dimen.tv_mode_detail_photo_height))
				              .get();
			} catch (InterruptedException | ExecutionException e) {
				bitmap = null;
			}
			Bundle args = new Bundle();
			args.putParcelable(TvModeDetailFragment.EXTRAS_IMAGE,
			                   bitmap);
			resultReceiver.send(0x90,
			                    args);
		}
	}

}
