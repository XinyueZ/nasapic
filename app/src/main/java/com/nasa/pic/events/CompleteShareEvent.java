package com.nasa.pic.events;


import android.content.Intent;

public final class CompleteShareEvent {
	private Intent mIntent;


	public CompleteShareEvent(Intent intent ) {
		mIntent = intent;
	}



	public Intent getIntent() {
		return mIntent;
	}
}
