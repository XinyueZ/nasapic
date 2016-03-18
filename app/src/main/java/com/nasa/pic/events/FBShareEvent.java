package com.nasa.pic.events;


import io.realm.RealmObject;

public final class FBShareEvent {
	private RealmObject mObject;


	public FBShareEvent(RealmObject object) {
		mObject = object;
	}


	public RealmObject getObject() {
		return mObject;
	}
}
