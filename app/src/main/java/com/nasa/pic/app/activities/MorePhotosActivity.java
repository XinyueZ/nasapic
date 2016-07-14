package com.nasa.pic.app.activities;

import android.app.Activity;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.support.v4.app.ActivityCompat;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import com.nasa.pic.R;
import com.nasa.pic.app.App;
import com.nasa.pic.app.adapters.PhotoListAdapter;
import com.nasa.pic.databinding.ActivityMorePhotosBinding;
import com.nasa.pic.ds.Photo;

import io.realm.RealmObject;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import io.realm.Sort;

public final class MorePhotosActivity extends AppRestfulActivity {
	/**
	 * Data-binding.
	 */
	private ActivityMorePhotosBinding mBinding;
	/**
	 * Main layout for this component.
	 */
	private static final int LAYOUT = R.layout.activity_more_photos;


	/**
	 * Show single instance of {@link MorePhotosActivity}
	 *
	 * @param cxt
	 * 		{@link Activity}.
	 */
	public static void showInstance(Activity cxt) {
		Intent intent = new Intent(cxt, MorePhotosActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
		ActivityCompat.startActivity(cxt, intent, null);
	}


	@Override
	protected void loadList() {
		App.Instance.getFireManager().selectAll(Photo.class);
	}


	@Override
	protected void sendPending() {

	}


	@Override
	protected void initDataBinding() {
		mBinding = DataBindingUtil.setContentView(this, LAYOUT);
		setUpErrorHandling((ViewGroup) findViewById(R.id.error_content));
		initPull2Load(mBinding.contentSrl);
	}


	@Override
	protected void queryLocalData() {
		buildListView(this, mBinding.responsesRv);
		super.queryLocalData();
	}


	@Override
	protected void buildViews() {
		if (isDataLoaded()) {
			if (mBinding.getAdapter() == null) {
				mBinding.setAdapter(new PhotoListAdapter(getCellSize()));
			}
			if (mBinding.getAdapter().getData() == null) {
				mBinding.getAdapter().setData(getData());
			}
			mBinding.getAdapter().notifyDataSetChanged();
		}

		mBinding.contentSrl.setRefreshing(false);
	}


	@Override
	protected void buildQuery(RealmQuery<? extends RealmObject> q) {
		//		if( !TextUtils.isEmpty( mKeyword ) ) {
		//			q.contains(
		//					"description",
		//					mKeyword
		//			);
		//		}
	}


	@Override
	protected RealmResults<? extends RealmObject> createQuery(RealmQuery<? extends RealmObject> q) {
		RealmResults<? extends RealmObject> results = q.findAllSortedAsync("date", Sort.DESCENDING);
		return results;
	}



	@Override
	protected void initNavi() {
		mBinding.toolbar.setNavigationIcon(R.drawable.ic_back_home);
		mBinding.toolbar.setNavigationOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				ActivityCompat.finishAfterTransition(MorePhotosActivity.this);
			}
		});
	}



	@Override
	protected void initMenu() {
		mBinding.toolbar.setTitle(R.string.lbl_more_photos);
	}

	@Override
	protected int getMenuRes() {
		return -1;
	}
}
