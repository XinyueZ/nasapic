package com.nasa.pic.app.adapters;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.support.v4.animation.AnimatorCompatHelper;
import android.support.v4.animation.AnimatorUpdateListenerCompat;
import android.support.v4.animation.ValueAnimatorCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import android.widget.ImageView;

import com.nasa.pic.BR;
import com.nasa.pic.R;
import com.nasa.pic.events.FBShareEvent;
import com.nasa.pic.events.OpenPhotoEvent;
import com.nasa.pic.events.ShareEvent;
import com.nasa.pic.transition.BakedBezierInterpolator;
import com.nasa.pic.transition.Thumbnail;
import com.nasa.pic.transition.TransitCompat;
import com.nasa.pic.utils.DynamicShareActionProvider;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.List;

import de.greenrobot.event.EventBus;
import io.realm.RealmObject;


public final class PhotoListAdapter<T extends RealmObject> extends RecyclerView.Adapter<PhotoListAdapter.ViewHolder> {


	/**
	 * Main layout for this component.
	 */
	private static final int ITEM_LAYOUT = R.layout.item_photo_layout;
	/**
	 * A menu on each line of list.
	 */
	private static final int MENU_LIST_ITEM = R.menu.menu_item;
	/**
	 * Data-source.
	 */
	private List<T> mVisibleData;

	private int mCellSize;

	public PhotoListAdapter(int cellSize) {
		mCellSize = cellSize;
	}

	/**
	 * Get current used data-source.
	 *
	 * @return The data-source.
	 */
	public List<T> getData() {
		return mVisibleData;
	}

	/**
	 * Set data-source for list-view.
	 *
	 * @param data Data-source.
	 * @return This object.
	 */
	public PhotoListAdapter<T> setData(List<T> data) {
		mVisibleData = data;
		return this;
	}


	/**
	 * Add data-source for list-view.
	 *
	 * @param data Data-source.
	 * @return This object.
	 */
	public PhotoListAdapter<T> addData(T data) {
		mVisibleData.add(data);
		return this;
	}


	/**
	 * Add data-source for list-view.
	 *
	 * @param data Data-source.
	 * @return This object.
	 */
	public PhotoListAdapter<T> addData(List<T> data) {
		mVisibleData.addAll(data);
		return this;
	}

	@Override
	public int getItemCount() {
		return mVisibleData == null ?
		       0 :
		       mVisibleData.size();
	}

	@Override
	public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		Context cxt = parent.getContext();
		LayoutInflater inflater = LayoutInflater.from(cxt);
		ViewDataBinding binding = DataBindingUtil.inflate(inflater, ITEM_LAYOUT, parent, false);
		return new PhotoListAdapter.ViewHolder(binding);
	}

	@Override
	public void onBindViewHolder(final ViewHolder holder, final int position) {
		final T entry = mVisibleData.get(position);
		final ListItemHandlers handlers = new ListItemHandlers(holder, this);
		holder.mBinding.setVariable(BR.photoDB, entry);
		holder.mBinding.setVariable(BR.handler, handlers);
		holder.mBinding.setVariable(BR.formatter, new SimpleDateFormat("yyyy-M-d"));
		holder.mBinding.setVariable(BR.cardSize, mCellSize);

		MenuItem openMi = holder.mToolbar.getMenu()
		                                 .findItem(R.id.action_open);
		openMi.setOnMenuItemClickListener(new OnMenuItemClickListener() {
			@Override
			public boolean onMenuItemClick(MenuItem menuItem) {
				handlers.onOpenPhoto(holder.itemView);
				return true;
			}
		});

		MenuItem fbShareMi = holder.mToolbar.getMenu()
		                                    .findItem(R.id.action_fb_share_item);
		fbShareMi.setOnMenuItemClickListener(new OnMenuItemClickListener() {
			@Override
			public boolean onMenuItemClick(MenuItem item) {
				EventBus.getDefault()
				        .post(new FBShareEvent(entry));
				return true;
			}
		});


		MenuItem shareMi = holder.mToolbar.getMenu()
		                                  .findItem(R.id.action_share_item);
		DynamicShareActionProvider shareLaterProvider = (DynamicShareActionProvider) MenuItemCompat.getActionProvider(shareMi);
		shareLaterProvider.setShareDataType("text/plain");
		shareLaterProvider.setOnShareLaterListener(new DynamicShareActionProvider.OnShareLaterListener() {
			@Override
			public void onShareClick(Intent shareIntent) {
				EventBus.getDefault()
				        .post(new ShareEvent(shareIntent, entry));
			}
		});
		holder.mBinding.executePendingBindings();
	}

	/**
	 * ViewHolder for the list.
	 */
	public static class ViewHolder extends RecyclerView.ViewHolder {
		private ViewDataBinding mBinding;
		private Toolbar mToolbar;

		ViewHolder(ViewDataBinding binding) {
			super(binding.getRoot());
			mBinding = binding;
			mToolbar = (Toolbar) binding.getRoot()
			                            .findViewById(R.id.toolbar);
			mToolbar.inflateMenu(MENU_LIST_ITEM);
		}
	}

	public static final class ListItemHandlers {
		private ViewHolder mViewHolder;
		private PhotoListAdapter mAdapter;

		public ListItemHandlers(ViewHolder viewHolder, PhotoListAdapter adapter) {
			mViewHolder = viewHolder;
			mAdapter = adapter;
		}

		public void onOpenPhoto(View view) {
			int pos = mViewHolder.getAdapterPosition();
			if (pos != RecyclerView.NO_POSITION) {

				try {
					ImageView imageView = (ImageView) view.findViewById(R.id.thumbnail_iv);
					final WeakReference<ImageView> ivRef = new WeakReference<>(imageView);
					ValueAnimatorCompat animator = AnimatorCompatHelper.emptyValueAnimator();
					animator.setDuration(TransitCompat.ANIM_DURATION * 3);
					animator.setTarget(imageView);
					animator.addUpdateListener(new AnimatorUpdateListenerCompat() {
						private float old = 1;
						private float end = 0;
						private Interpolator interpolator2 = new BakedBezierInterpolator();

						@Override
						public void onAnimationUpdate(ValueAnimatorCompat animation) {
							if (ivRef.get() == null) {
								return;
							}
							float fraction = interpolator2.getInterpolation(animation.getAnimatedFraction());

							//Set background alpha
							float alpha = old + (fraction * (end - old));
							ViewCompat.setAlpha(ivRef.get(), alpha);
						}
					});
					animator.start();

					int[] screenLocation = new int[2];
					imageView.getLocationOnScreen(screenLocation);
					Thumbnail thumbnail = new Thumbnail(screenLocation[1], screenLocation[0], imageView.getWidth(), imageView.getHeight());
					EventBus.getDefault()
					        .post(new OpenPhotoEvent((RealmObject) mAdapter.getData()
					                                                       .get(pos), thumbnail, imageView));
				} catch (NullPointerException e) {
					EventBus.getDefault()
					        .post(new OpenPhotoEvent((RealmObject) mAdapter.getData()
					                                                       .get(pos), null, null));

				}
			}
		}
	}
}
