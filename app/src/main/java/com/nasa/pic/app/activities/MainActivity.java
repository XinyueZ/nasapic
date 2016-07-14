package com.nasa.pic.app.activities;

import android.app.Activity;
import android.content.Intent;
import android.support.v4.app.ActivityCompat;

public final class MainActivity extends  AbstractMainActivity {
	/**
	 * Show single instance of {@link MainActivity}
	 *
	 * @param cxt
	 * 		{@link Activity}.
	 */
	public static void showInstance(Activity cxt) {
		Intent intent = new Intent(cxt, MainActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
		ActivityCompat.startActivity(cxt, intent, null);
	}


}
