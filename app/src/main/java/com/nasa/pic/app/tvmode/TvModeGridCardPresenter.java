
package com.nasa.pic.app.tvmode;

import android.content.Context;
import android.support.v17.leanback.widget.ImageCardView;
import android.support.v17.leanback.widget.Presenter;
import android.text.TextUtils;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.nasa.pic.R;
import com.nasa.pic.ds.PhotoDB;

/**
 * This abstract, generic class will create and manage the
 * ViewHolder and will provide typed Presenter callbacks such that you do not have to perform casts
 * on your own.
 */
public class TvModeGridCardPresenter
		extends Presenter {
	private final int     mCardStyleResId;
	private final Context mContext;

	public TvModeGridCardPresenter(Context context,
	                               int cardStyleResId) {
		mContext = context;
		mCardStyleResId = cardStyleResId;
	}

	public TvModeGridCardPresenter(Context context) {
		mContext = context;
		mCardStyleResId = R.style.DefaultCardStyle;
	}

	@Override
	public final ViewHolder onCreateViewHolder(ViewGroup parent) {
		ImageCardView cardView = new ImageCardView(mContext,
		                                           mCardStyleResId);
		return new ViewHolder(cardView);
	}

	@Override
	public final void onBindViewHolder(ViewHolder viewHolder,
	                                   Object item) {
		PhotoDB photo = (PhotoDB) item;

		ImageCardView imageCardView = (ImageCardView) viewHolder.view;
		imageCardView.setTag(photo);
		imageCardView.setTitleText(photo.getTitle());
		imageCardView.setContentText(photo.getDescription());
		String photoUrl = photo.getUrls()
		                       .getNormal();
		if (!TextUtils.isEmpty(photoUrl)) {
			Glide.with(mContext)
			     .load(photoUrl)
			     .crossFade()
			     .centerCrop()
			     .diskCacheStrategy(DiskCacheStrategy.ALL)
			     .into(imageCardView.getMainImageView());
		}
	}

	@Override
	public final void onUnbindViewHolder(ViewHolder viewHolder) {
		// Nothing to clean up. Override if necessary.
	}

}
