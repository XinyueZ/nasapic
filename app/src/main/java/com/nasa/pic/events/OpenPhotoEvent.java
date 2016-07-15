package com.nasa.pic.events;


import com.nasa.pic.databinding.ItemBinding;
import com.nasa.pic.transition.Thumbnail;

import io.realm.RealmObject;

public final class OpenPhotoEvent {
	private RealmObject mObject;
	private Thumbnail mThumbnail;
	private ItemBinding mItemBinding;

	public OpenPhotoEvent(RealmObject object, Thumbnail thumbnail, ItemBinding binding) {
		mObject = object;
		mThumbnail = thumbnail;
		mItemBinding = binding;
	}


	public RealmObject getObject() {
		return mObject;
	}


	public Thumbnail getThumbnail() {
		return mThumbnail;
	}

	public ItemBinding getItemBinding() {
		return mItemBinding;
	}
}
