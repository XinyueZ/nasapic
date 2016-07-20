package com.nasa.pic.events;


import android.support.v7.widget.RecyclerView;

public final class ClickPhotoItemEvent {
	private final  RecyclerView.ViewHolder mViewHolder;


	public ClickPhotoItemEvent( RecyclerView.ViewHolder  viewHolder) {
		mViewHolder = viewHolder;
	}

	public  RecyclerView.ViewHolder  getViewHolder() {
		return mViewHolder;
	}
}
