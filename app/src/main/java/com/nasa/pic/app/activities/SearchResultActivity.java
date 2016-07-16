package com.nasa.pic.app.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.view.View;

import com.nasa.pic.R;

import java.util.Calendar;
import java.util.Date;

import io.realm.RealmObject;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import io.realm.Sort;

public final class SearchResultActivity extends AbstractMainActivity {
	private static final String EXTRAS_KEYWORD = SearchResultActivity.class.getName() + ".EXTRAS.keyword";

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
		String keyword = getIntent().getStringExtra(EXTRAS_KEYWORD);
		getBinding().showDayFab.hide();
		getBinding().loadMoreFab.hide();
		getBinding().showMonthFab.hide();
		getBinding().toolbar.setTitle(keyword);
		getBinding().responsesRv.clearOnScrollListeners();
	}

	@Override
	protected RealmResults<? extends RealmObject> createQuery(RealmQuery<? extends RealmObject> q) {
		String keyword = getIntent().getStringExtra(EXTRAS_KEYWORD);
		String[] words = keyword.split("-");
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
		q.lessThanOrEqualTo("date", timeMax);
		calendar.set(Calendar.DAY_OF_MONTH, 0);
		Date timeMin = calendar.getTime();
		q.greaterThanOrEqualTo("date", timeMin);

		RealmResults<? extends RealmObject> results = q.findAllSortedAsync("date", Sort.DESCENDING);
		return results;
	}


	@Override
	protected void loadList() {
		String keyword = getIntent().getStringExtra(EXTRAS_KEYWORD);
		String[] words = keyword.split("-");
		int year = Integer.valueOf(words[0]);
		int month = Integer.valueOf(words[1]);

		String timeZone = Calendar.getInstance()
		                          .getTimeZone()
		                          .getID();

		loadPhotoList(year, month, timeZone);
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
}
