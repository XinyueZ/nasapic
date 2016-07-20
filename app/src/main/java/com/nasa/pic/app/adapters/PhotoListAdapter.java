package com.nasa.pic.app.adapters;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.view.ViewGroup;

import com.nasa.pic.BR;
import com.nasa.pic.R;
import com.nasa.pic.databinding.ItemBinding;
import com.nasa.pic.databinding.VideoBinding;
import com.nasa.pic.ds.PhotoDB;
import com.nasa.pic.events.ClickPhotoItemEvent;
import com.nasa.pic.events.FBShareEvent;
import com.nasa.pic.events.ShareEvent;
import com.nasa.pic.utils.DynamicShareActionProvider;

import java.util.List;

import de.greenrobot.event.EventBus;
import io.realm.RealmObject;


public final class PhotoListAdapter <T extends RealmObject>   extends RecyclerView.Adapter<RecyclerView.ViewHolder > {


	/**
	 * A menu on each line of list.
	 */
	private static final int MENU_LIST_ITEM = R.menu.menu_item;
	/**
	 * Data-source.
	 */
	private List<T> mVisibleData;

	private static final int TYPE_IMAGE = 0;
	private static final int TYPE_VIDEO = 1;

	/**
	 * Set data-source for list-view.
	 *
	 * @param data Data-source.
	 * @return This object.
	 */
	public PhotoListAdapter setData(List<T> data) {
		mVisibleData = data;
		return this;
	}


	@Override
	public int getItemCount() {
		return mVisibleData == null ?
		       0 :
		       mVisibleData.size();
	}

	@Override
	public int getItemViewType(int position) {
		switch (((PhotoDB)mVisibleData.get(position)).getType()){
			case "image":
				return TYPE_IMAGE;
			default:
				return TYPE_VIDEO;
		}
	}

	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		Context cxt = parent.getContext();
		LayoutInflater inflater = LayoutInflater.from(cxt);
		if(viewType == TYPE_IMAGE) {
			ItemBinding binding = DataBindingUtil.inflate(inflater,  R.layout.item_photo_layout, parent, false);
			return new ItemViewHolder(binding);
		} else {
			VideoBinding binding = DataBindingUtil.inflate(inflater,  R.layout.item_video_layout, parent, false);
			return new VideoViewHolder(binding);
		}
	}

	@Override
	public void onBindViewHolder(final  RecyclerView.ViewHolder  holder, final int position) {
		final T entry = mVisibleData.get(position);
		if(getItemViewType(position) == TYPE_IMAGE) {
			ItemViewHolder itemViewHolder = (ItemViewHolder) holder;
			final ListItemHandlers handlers = new ListItemHandlers(holder);
			itemViewHolder.mBinding.setVariable(BR.photoDB, entry);
			itemViewHolder.mBinding.setVariable(BR.handler, handlers);


			MenuItem openMi = itemViewHolder.mToolbar.getMenu()
			                                 .findItem(R.id.action_open);
			openMi.setOnMenuItemClickListener(new OnMenuItemClickListener() {
				@Override
				public boolean onMenuItemClick(MenuItem menuItem) {
					handlers.onOpenPhoto(holder.itemView);
					return true;
				}
			});

			MenuItem fbShareMi = itemViewHolder.mToolbar.getMenu()
			                                    .findItem(R.id.action_fb_share_item);
			fbShareMi.setOnMenuItemClickListener(new OnMenuItemClickListener() {
				@Override
				public boolean onMenuItemClick(MenuItem item) {
					EventBus.getDefault()
					        .post(new FBShareEvent(entry));
					return true;
				}
			});


			MenuItem shareMi = itemViewHolder.mToolbar.getMenu()
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
			itemViewHolder.mBinding.executePendingBindings();
		} else {
			VideoViewHolder videoViewHolder = (VideoViewHolder) holder;
			final ListItemHandlers handlers = new ListItemHandlers(holder);
			videoViewHolder.mBinding.setVariable(BR.handler, handlers);


			MenuItem openMi = videoViewHolder.mToolbar.getMenu()
			                                         .findItem(R.id.action_open);
			openMi.setOnMenuItemClickListener(new OnMenuItemClickListener() {
				@Override
				public boolean onMenuItemClick(MenuItem menuItem) {
					handlers.onOpenPhoto(holder.itemView);
					return true;
				}
			});

			MenuItem fbShareMi = videoViewHolder.mToolbar.getMenu()
			                                            .findItem(R.id.action_fb_share_item);
			fbShareMi.setOnMenuItemClickListener(new OnMenuItemClickListener() {
				@Override
				public boolean onMenuItemClick(MenuItem item) {
					EventBus.getDefault()
					        .post(new FBShareEvent(entry));
					return true;
				}
			});


			MenuItem shareMi = videoViewHolder.mToolbar.getMenu()
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
			videoViewHolder.mBinding.executePendingBindings();
		}
	}

	public static class ItemViewHolder extends RecyclerView.ViewHolder {
		public final ItemBinding mBinding;
		private final Toolbar mToolbar;

		ItemViewHolder(ItemBinding binding) {
			super(binding.getRoot());
			mBinding = binding;
			mToolbar = (Toolbar) binding.getRoot()
			                            .findViewById(R.id.toolbar);
			mToolbar.inflateMenu(MENU_LIST_ITEM);
		}
	}


	public static class VideoViewHolder extends RecyclerView.ViewHolder {
		public final VideoBinding mBinding;
		private final Toolbar mToolbar;

		VideoViewHolder(VideoBinding binding) {
			super(binding.getRoot());
			mBinding = binding;
			mToolbar = (Toolbar) binding.getRoot()
			                            .findViewById(R.id.toolbar);
			mToolbar.inflateMenu(MENU_LIST_ITEM);
		}
	}


	public static final class ListItemHandlers {
		private final RecyclerView.ViewHolder mViewHolder;

		public ListItemHandlers(RecyclerView.ViewHolder viewHolder) {
			mViewHolder = viewHolder;
		}

		public void onOpenPhoto(@SuppressWarnings("UnusedParameters") View view) {
			EventBus.getDefault()
			        .post(new ClickPhotoItemEvent(mViewHolder));
		}
	}
}
