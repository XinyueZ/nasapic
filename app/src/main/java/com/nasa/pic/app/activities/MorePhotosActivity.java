package com.nasa.pic.app.activities;

import android.app.Activity;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import com.nasa.pic.R;
import com.nasa.pic.app.App;
import com.nasa.pic.app.adapters.PhotoListAdapter;
import com.nasa.pic.databinding.ActivityMorePhotosBinding;
import com.nasa.pic.ds.Photo;
import com.nasa.pic.ds.PhotoDB;

import io.realm.RealmObject;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import io.realm.Sort;

public final class MorePhotosActivity extends AppBasicActivity {
	/**
	 * Data-binding.
	 */
	private ActivityMorePhotosBinding mBinding;
	/**
	 * Main layout for this component.
	 */
	private static final int LAYOUT = R.layout.activity_more_photos;


	//[Begin for detecting scrolling onto bottom]
	private int mVisibleItemCount;
	private int mPastVisibleItems;
	private int mTotalItemCount;
	//[End]

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
		setSupportActionBar(mBinding.toolbar);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setTitle(R.string.lbl_more_photos);


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
				mBinding.setAdapter(new PhotoListAdapter());
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
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mBinding.fab.hide();
		mBinding.fab.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				PhotoDB photoMin = (PhotoDB) getData().get(getData().size() - 1);
				App.Instance.getFireManager().selectFrom(new Photo().newFromDB(photoMin));
				mBinding.contentSrl.setRefreshing(true);
				if (mBinding.fab.isShown()) {
					mBinding.fab.hide();
				}
			}
		});
		mBinding.responsesRv.addOnScrollListener(new RecyclerView.OnScrollListener() {
			@Override
			public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
				//Calc whether the list has been scrolled on bottom,
				//this lets app to getting next page.
				LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
				mVisibleItemCount = linearLayoutManager.getChildCount();
				mTotalItemCount = linearLayoutManager.getItemCount();
				mPastVisibleItems = linearLayoutManager.findFirstVisibleItemPosition();
				if (ViewCompat.getY(recyclerView) < dy) {
					if ((mVisibleItemCount + mPastVisibleItems) == mTotalItemCount) {
						if (!mBinding.fab.isShown()) {
							mBinding.fab.show();
						}
					}
				} else {
					if (mBinding.fab.isShown()) {
						mBinding.fab.hide();
					}
				}
			}
		});
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		// Respond to the action bar's Up/Home button
		case android.R.id.home:
			ActivityCompat.finishAfterTransition(this);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}


}
