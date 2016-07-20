package com.nasa.pic.app.adapters;

import android.databinding.BindingAdapter;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.chopping.utils.Utils;
import com.nasa.pic.R;
import com.nasa.pic.ds.PhotoUrlDB;

public final class AdapterBinder {


	@BindingAdapter({ "imageNormalUrl",
	                  "thumbnail" })
	public static void loadNormalImage(ImageView view, PhotoUrlDB urls, double thumbnail) {
		String url = "http://tomatofish.com/wp-content/uploads/2013/08/placeholder-tomatofish.jpg";
		if (urls != null) {
			url = urls.getNormal();
		}
		try {
			Glide.with(view.getContext())
			     .load(Utils.uriStr2URI(url)
			                .toASCIIString())
			     .asBitmap()
			     .format(DecodeFormat.PREFER_RGB_565)
			     .thumbnail((float) thumbnail)
			     .skipMemoryCache(false)
			     .diskCacheStrategy(DiskCacheStrategy.ALL)
			     .placeholder(R.drawable.placeholder)
			     .into(view);
		} catch (NullPointerException e) {
			Glide.with(view.getContext())
			     .load("http://tomatofish.com/wp-content/uploads/2013/08/placeholder-tomatofish.jpg")
			     .skipMemoryCache(false)
			     .diskCacheStrategy(DiskCacheStrategy.ALL)
			     .into(view);
		}
	}
}
