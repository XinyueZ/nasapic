package com.nasa.pic.app.adapters;

import java.text.SimpleDateFormat;
import java.util.List;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.nasa.pic.BR;
import com.nasa.pic.R;

import io.realm.RealmObject;


public final class PhotoListAdapter<T extends RealmObject> extends RecyclerView.Adapter<PhotoListAdapter.ViewHolder> {


	/**
	 * Main layout for this component.
	 */
	private static final int ITEM_LAYOUT = R.layout.item_photo_layout;
	/**
	 * Data-source.
	 */
	private List<T> mVisibleData;

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
	 * @param data
	 * 		Data-source.
	 *
	 * @return This object.
	 */
	public PhotoListAdapter<T> setData( List<T> data ) {
		mVisibleData = data;
		return this;
	}


	/**
	 * Add data-source for list-view.
	 *
	 * @param data
	 * 		Data-source.
	 *
	 * @return This object.
	 */
	public PhotoListAdapter<T> addData( T data ) {
		mVisibleData.add( data );
		return this;
	}


	/**
	 * Add data-source for list-view.
	 *
	 * @param data
	 * 		Data-source.
	 *
	 * @return This object.
	 */
	public PhotoListAdapter<T> addData( List<T> data ) {
		mVisibleData.addAll( data );
		return this;
	}

	@Override
	public int getItemCount() {
		return mVisibleData == null ? 0 : mVisibleData.size();
	}

	@Override
	public ViewHolder onCreateViewHolder( ViewGroup parent, int viewType ) {
		Context        cxt      = parent.getContext();
		LayoutInflater inflater = LayoutInflater.from( cxt );
		ViewDataBinding binding = DataBindingUtil.inflate(
				inflater,
				ITEM_LAYOUT,
				parent,
				false
		);
		return new PhotoListAdapter.ViewHolder( binding );
	}

	@Override
	public void onBindViewHolder( final ViewHolder holder, final int position ) {
		T entry = mVisibleData.get( position );
		holder.mBinding.setVariable(
				BR.photoDB,
				entry
		);

		holder.mBinding.setVariable(
				BR.formatter,
				new SimpleDateFormat( "yyyy-M-d" )
		);

		holder.mBinding.executePendingBindings();
	}

	/**
	 * ViewHolder for the list.
	 */
	public static class ViewHolder extends RecyclerView.ViewHolder {
		private ViewDataBinding mBinding;

		ViewHolder( ViewDataBinding binding ) {
			super( binding.getRoot() );
			mBinding = binding;
		}
	}


}
