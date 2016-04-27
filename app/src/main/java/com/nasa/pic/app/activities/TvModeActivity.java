

package com.nasa.pic.app.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;

import com.nasa.pic.R;


public class TvModeActivity
		extends Activity {

	/**
	 * Show single instance of {@link TvModeActivity}
	 *
	 * @param cxt {@link Activity}.
	 */
	public static void showInstance(Activity cxt) {
		Intent intent = new Intent(cxt,
		                           TvModeActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
		ActivityCompat.startActivity(cxt,
		                             intent,
		                             null);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		com.nasa.pic.utils.Utils.hideStatusBar(this);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_tv_mode);
	}
}
