package com.nasa.pic.app.adapters;

import android.databinding.BindingAdapter;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.chopping.utils.Utils;
import com.nasa.pic.R;
import com.nasa.pic.ds.PhotoUrlDB;

public final class AdapterBinder {


	@SuppressWarnings("unchecked")
	@BindingAdapter("cardSize")
	public static void setCellSize(View view,
	                               int size) {
		view.getLayoutParams().width = size;
		view.getLayoutParams().height = size;
	}


	@SuppressWarnings("unchecked")
	@BindingAdapter("listAdapter")
	public static void setEntriesBinder(RecyclerView recyclerView,
	                                    RecyclerView.Adapter adp) {
		recyclerView.setAdapter(adp);
	}


	@BindingAdapter({ "imageNormalUrl" })
	public static void loadNormalImage(ImageView view,
	                                   PhotoUrlDB urls) {
		String url = "http://tomatofish.com/wp-content/uploads/2013/08/placeholder-tomatofish.jpg";
		if (urls != null) {
			url = urls.getNormal();
		}
		try {
			Glide.with(view.getContext())
			     .load(Utils.uriStr2URI(url)
			                .toASCIIString())
			     .centerCrop()
			     .crossFade()
			     .diskCacheStrategy(DiskCacheStrategy.ALL)
			     .placeholder(R.drawable.placeholder)
			     .into(view);
		} catch (NullPointerException e) {
			Glide.with(view.getContext())
			     .load("http://tomatofish.com/wp-content/uploads/2013/08/placeholder-tomatofish.jpg")
			     .diskCacheStrategy(DiskCacheStrategy.ALL)
			     .into(view);
		}
	}
}
