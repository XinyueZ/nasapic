package com.nasa.pic.app.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.google.android.gms.ads.AdRequest;
import com.nasa.pic.R;
import com.nasa.pic.app.adapters.PhotoListAdapter;
import com.nasa.pic.app.fragments.DatePickerDialogFragment;
import com.nasa.pic.databinding.ActivityAbstractMainBinding;
import com.nasa.pic.events.ClickPhotoItemEvent;
import com.nasa.pic.events.OpenPhotoEvent;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import de.greenrobot.event.EventBus;
import io.realm.RealmObject;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import io.realm.Sort;

public final class SearchResultActivity extends AbstractMainActivity {
	private static final String EXTRAS_KEYWORD = SearchResultActivity.class.getName() + ".EXTRAS.keyword";

	//------------------------------------------------
	//Subscribes, event-handlers
	//------------------------------------------------

	/**
	 * Handler for {@link ClickPhotoItemEvent}.
	 *
	 * @param e Event {@link ClickPhotoItemEvent}.
	 */
	public void onEvent(ClickPhotoItemEvent e) {
		final RecyclerView.ViewHolder viewHolder = e.getViewHolder();
		int pos = viewHolder.getAdapterPosition();
		if (viewHolder instanceof PhotoListAdapter.ItemViewHolder) {
			openPhoto((PhotoListAdapter.ItemViewHolder) viewHolder, pos);
		} else {
			EventBus.getDefault()
			        .post(new OpenPhotoEvent(getData().get(pos), null, null));
		}
	}
	//------------------------------------------------

	/**
	 * Show single instance of {@link SearchResultActivity}
	 *
	 * @param keyword "year-month" .
	 * @param cxt     {@link Activity}.
	 */
	public static void showInstance(Activity cxt, String keyword) {
		Intent intent = new Intent(cxt, SearchResultActivity.class);
		intent.putExtra(EXTRAS_KEYWORD, keyword);
		intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
		ActivityCompat.startActivity(cxt, intent, null);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ActivityAbstractMainBinding binding = getBinding();
		binding.av.setVisibility(View.VISIBLE);
		binding.av.loadAd(new AdRequest.Builder().build());

		String keyword = getIntent().getStringExtra(EXTRAS_KEYWORD);
		binding.toolbar.setLogo(null);
		binding.searchFab.hide();
		binding.toolbar.setTitle(keyword);
		binding.responsesRv.clearOnScrollListeners();
	}

	// [START add_lifecycle_methods]

	/**
	 * Called when leaving the activity
	 */
	@Override
	public void onPause() {
		getBinding().av.pause();
		super.onPause();
	}

	/**
	 * Called when returning to the activity
	 */
	@Override
	public void onResume() {
		super.onResume();
		getBinding().av.resume();
	}

	/**
	 * Called before the activity is destroyed
	 */
	@Override
	public void onDestroy() {
		getBinding().av.destroy();
		super.onDestroy();
	}

	@Override
	protected RealmResults<? extends RealmObject> createQuery(RealmQuery<? extends RealmObject> q) {
		String keyword = getIntent().getStringExtra(EXTRAS_KEYWORD);
		String[] words = keyword.split("-");
		if (words.length > 2) {
			try {
				q.equalTo("date", new SimpleDateFormat("yyyy-M-d").parse(keyword));
				return q.findAllSortedAsync("date", Sort.DESCENDING);
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
		int year = Integer.valueOf(words[0]);
		int month = Integer.valueOf(words[1]);
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.YEAR, year);
		calendar.set(Calendar.MONTH, month - 1);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		int lastDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
		calendar.set(Calendar.DAY_OF_MONTH, lastDay);
		Date timeMax = calendar.getTime();
		calendar.set(Calendar.DAY_OF_MONTH, 0);
		Date timeMin = calendar.getTime();
		q.between("date", timeMin, timeMax);
		return q.findAllSortedAsync("date", Sort.DESCENDING);
	}


	@Override
	protected void loadList() {
		String keyword = getIntent().getStringExtra(EXTRAS_KEYWORD);
		String[] words = keyword.split("-");
		String timeZone = Calendar.getInstance()
		                          .getTimeZone()
		                          .getID();

		if (words.length > 2) {
			int year = Integer.valueOf(words[0]);
			int month = Integer.valueOf(words[1]);
			int day = Integer.valueOf(words[2]);

			loadPhotoList(year, month, day, timeZone);
		} else {
			int year = Integer.valueOf(words[0]);
			int month = Integer.valueOf(words[1]);

			loadPhotoList(year, month, DatePickerDialogFragment.IGNORED_DAY, timeZone);
		}
	}


	@Override
	protected void initMenu() {
		super.initMenu();
		getBinding().toolbar.setNavigationIcon(R.drawable.ic_back_home);
		getBinding().toolbar.setNavigationOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				ActivityCompat.finishAfterTransition(SearchResultActivity.this);
			}
		});
	}

	@Override
	protected void buildViews() {
		setHasShownDataOnUI(!getData().isEmpty());
		if (isDataLoaded()) {
			if (getBinding().responsesRv.getAdapter() == null) {
				//Data
				mPhotoListAdapter = new PhotoListAdapter();
				mPhotoListAdapter.setData(getData());
				getBinding().responsesRv.setAdapter(mPhotoListAdapter);
			} else {
				mPhotoListAdapter.setData(getData());
				mPhotoListAdapter.notifyDataSetChanged();
			}
			getBinding().noResultsTv.setVisibility(getData().isEmpty() ?
			                                       View.VISIBLE :
			                                       View.GONE);
		}
	}


	@Override
	protected boolean shouldLoadLocal(Context cxt) {
		return false;
	}
}
