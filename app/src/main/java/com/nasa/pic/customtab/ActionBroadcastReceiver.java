package com.nasa.pic.customtab;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.nasa.pic.app.activities.AppNormalActivity;
import com.nasa.pic.app.activities.FacebookShareHelperActivity;
import com.nasa.pic.utils.Utils;


public final class ActionBroadcastReceiver extends BroadcastReceiver {
	public static final String KEY_ACTION_SOURCE = "org.chromium.customtabsdemos.ACTION_SOURCE";
	public static final int ACTION_ACTION_BUTTON_1 = 0x1;
	public static final int ACTION_ACTION_BUTTON_2 = 0x2;

	@Override
	public void onReceive(Context context, Intent intent) {
		String url = intent.getDataString();
		if (url != null) {
			int actionId = intent.getIntExtra(KEY_ACTION_SOURCE, -1);
			doAction(context, actionId, intent);
		}
	}

	private void doAction(Context context, int actionId, Intent intent) {
		String title = intent.getStringExtra(AppNormalActivity.EXTRAS_TYPE);
		String description = intent.getStringExtra(AppNormalActivity.EXTRAS_DESCRIPTION);
		String urlToShare = intent.getStringExtra(AppNormalActivity.EXTRAS_URL_TO_PHOTO);

		switch (actionId) {
		case ACTION_ACTION_BUTTON_1:
			FacebookShareHelperActivity.showInstance(context, intent);
			break;
		case ACTION_ACTION_BUTTON_2:
			Utils.share(context, title, description, urlToShare);
			break;
		}
	}
}