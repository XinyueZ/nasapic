package com.nasa.pic.app.activities;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;

import com.nasa.pic.events.FBShareCompleteEvent;
import com.nasa.pic.utils.Utils;

import de.greenrobot.event.EventBus;

public final class FacebookShareHelperActivity extends Activity {
	//------------------------------------------------
	//Subscribes, event-handlers
	//------------------------------------------------

	/**
	 * Handler for {@link FBShareCompleteEvent}.
	 *
	 * @param e
	 * 		Event {@link FBShareCompleteEvent}.
	 */
	public void onEvent(FBShareCompleteEvent e) {
		ActivityCompat.finishAfterTransition(this);
	}
	//------------------------------------------------

	/**
	 * Show single instance of {@link FacebookShareHelperActivity}
	 *
	 * @param cxt
	 * 		{@link android.content.Context}.
	 */
	public static void showInstance(Context cxt, Intent additionalIntent) {
		Intent intent = new Intent(cxt, FacebookShareHelperActivity.class);
		intent.putExtras(additionalIntent);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK );
		cxt.startActivity(intent);
	}


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Intent intent = getIntent();
		String title = intent.getStringExtra(AppNormalActivity.EXTRAS_TYPE);
		String description = intent.getStringExtra(AppNormalActivity.EXTRAS_DESCRIPTION);
		String urlToShare = intent.getStringExtra(AppNormalActivity.EXTRAS_URL_TO_PHOTO);
		Utils.facebookShare(this, title, description, urlToShare);
	}

	@Override
	protected void onPause() {
		EventBus.getDefault().unregister(this);
		super.onPause();
	}

	@Override
	protected void onResume() {
		super.onResume();
		EventBus.getDefault().register(this);
	}
}
