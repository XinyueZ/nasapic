package com.nasa.pic.app.adapters;

import android.databinding.BindingAdapter;
import android.support.v7.widget.RecyclerView;

public final class AdapterBinder {


	@SuppressWarnings("unchecked")
	@BindingAdapter("listAdapter")
	public static void setEntriesBinder( RecyclerView recyclerView, RecyclerView.Adapter adp ) {
		recyclerView.setAdapter( adp );
	}
}
