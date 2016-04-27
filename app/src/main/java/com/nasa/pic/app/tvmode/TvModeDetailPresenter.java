/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 *
 */

package com.nasa.pic.app.tvmode;

import android.content.Context;
import android.support.v17.leanback.widget.Presenter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.nasa.pic.R;
import com.nasa.pic.ds.PhotoDB;
import com.nasa.pic.utils.ResourceCache;


public class TvModeDetailPresenter
		extends Presenter {

	private ResourceCache mResourceCache = new ResourceCache();
	private Context mContext;

	public TvModeDetailPresenter(Context context) {
		mContext = context;
	}

	@Override
	public ViewHolder onCreateViewHolder(ViewGroup parent) {
		View view = LayoutInflater.from(mContext)
		                          .inflate(R.layout.fragment_tv_mode_detail,
		                                   null);
		return new ViewHolder(view);
	}

	@Override
	public void onBindViewHolder(ViewHolder viewHolder,
	                             Object item) {
		TextView primaryText = mResourceCache.getViewById(viewHolder.view,
		                                                  R.id.primary_text);
		TextView extraText   = mResourceCache.getViewById(viewHolder.view,
		                                                  R.id.extra_text);

		PhotoDB photo = (PhotoDB) item;
		primaryText.setText(photo.getTitle());
		extraText.setText(photo.getDescription());
	}

	@Override
	public void onUnbindViewHolder(ViewHolder viewHolder) {
		// Nothing to do here.
	}
}
