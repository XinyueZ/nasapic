package com.nasa.pic.events;


import android.content.Intent;

import io.realm.RealmObject;

public final class ShareEvent {
	private Intent mIntent;
	private RealmObject mObject;


	public ShareEvent(Intent intent, RealmObject object) {
		mIntent = intent;
		mObject = object;
	}


	public RealmObject getObject() {
		return mObject;
	}

	public Intent getIntent() {
		return mIntent;
	}
}
