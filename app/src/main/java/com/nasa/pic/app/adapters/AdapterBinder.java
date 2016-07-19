package com.nasa.pic.app.adapters;

import android.databinding.BindingAdapter;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.chopping.utils.Utils;
import com.nasa.pic.R;
import com.nasa.pic.ds.PhotoUrlDB;

public final class AdapterBinder {


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
			     .thumbnail(0.1f)
			     .crossFade()
			     .diskCacheStrategy(DiskCacheStrategy.ALL)
			     .placeholder(R.drawable.placeholder)
			     .into(view);
		} catch (NullPointerException e) {
			Glide.with(view.getContext())
			     .load("http://tomatofish.com/wp-content/uploads/2013/08/placeholder-tomatofish.jpg")
			     .thumbnail(0.5f)
			     .diskCacheStrategy(DiskCacheStrategy.ALL)
			     .into(view);
		}
	}
}
