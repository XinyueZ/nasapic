package com.nasa.pic.events;


import android.widget.ImageView;

import com.nasa.pic.transition.Thumbnail;

import java.lang.ref.WeakReference;

import io.realm.RealmObject;

public final class OpenPhotoEvent {
	private RealmObject mObject;
	private Thumbnail mThumbnail;
	private WeakReference<ImageView> mSenderIv;

	public OpenPhotoEvent(RealmObject object, Thumbnail thumbnail, ImageView imageView) {
		mObject = object;
		mThumbnail = thumbnail;
		mSenderIv = new WeakReference<>(imageView);
	}


	public RealmObject getObject() {
		return mObject;
	}


	public Thumbnail getThumbnail() {
		return mThumbnail;
	}


	public WeakReference<ImageView> getSenderIv() {
		return mSenderIv;
	}
}
