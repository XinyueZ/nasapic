package com.nasa.pic.events;


import com.nasa.pic.app.adapters.PhotoListAdapter;

public final class ClickPhotoItemEvent {
	private final PhotoListAdapter.ViewHolder mViewHolder;


	public ClickPhotoItemEvent(PhotoListAdapter.ViewHolder viewHolder) {
		mViewHolder = viewHolder;
	}

	public PhotoListAdapter.ViewHolder getViewHolder() {
		return mViewHolder;
	}
}
