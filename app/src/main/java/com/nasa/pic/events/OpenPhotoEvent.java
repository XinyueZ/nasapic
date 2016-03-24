package com.nasa.pic.events;


import com.nasa.pic.transaction.Thumbnail;

import io.realm.RealmObject;

public final class OpenPhotoEvent {
	private RealmObject mObject;
	private Thumbnail mThumbnail;


	public OpenPhotoEvent(RealmObject object, Thumbnail thumbnail) {
		mObject = object;
		mThumbnail = thumbnail;
	}


	public RealmObject getObject() {
		return mObject;
	}


	public Thumbnail getThumbnail() {
		return mThumbnail;
	}
}
