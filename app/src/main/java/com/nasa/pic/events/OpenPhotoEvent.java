package com.nasa.pic.events;


import io.realm.RealmObject;

public final class OpenPhotoEvent {
	private RealmObject mObject;


	public OpenPhotoEvent(RealmObject object) {
		mObject = object;
	}


	public RealmObject getObject() {
		return mObject;
	}
}
