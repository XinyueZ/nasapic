package com.nasa.pic.utils;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.facebook.FacebookException;
import com.facebook.widget.WebDialog;
import com.facebook.widget.WebDialog.OnCompleteListener;
import com.nasa.pic.R;
import com.nasa.pic.app.App;
import com.nasa.pic.app.adapters.SectionedGridRecyclerViewAdapter;
import com.nasa.pic.ds.PhotoDB;
import com.nasa.pic.events.CompleteShareEvent;
import com.nasa.pic.events.FBShareCompleteEvent;
import com.tinyurl4j.Api;
import com.tinyurl4j.Api.TinyUrl;
import com.tinyurl4j.data.Response;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import de.greenrobot.event.EventBus;
import io.realm.RealmObject;
import io.realm.RealmResults;
import retrofit2.Call;
import retrofit2.Callback;

import static android.support.design.widget.Snackbar.make;

public final class Utils {

	public static void fullScreen(Activity activity) {
		Window window = activity.getWindow();
		window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
	}


	public static void facebookShare(Context cxt, PhotoDB photoDB) {
		String title = !TextUtils.isEmpty(photoDB.getTitle()) ?
		               photoDB.getTitle() :
		               "";
		String desc = !TextUtils.isEmpty(photoDB.getDescription()) ?
		              photoDB.getDescription() :
		              "";
		String url = !TextUtils.isEmpty(photoDB.getUrls()
		                                       .getHd()) ?
		             photoDB.getUrls()
		                    .getHd() :
		             photoDB.getUrls()
		                    .getNormal();
		facebookShare(cxt, title, desc, url);
	}


	public static void facebookShare(Context cxt, String title, String description, String url) {
		final WebDialog fbDlg;
		Bundle postParams = new Bundle();
		if (!TextUtils.isEmpty(url)) {
			fbDlg = new WebDialog.FeedDialogBuilder(cxt, cxt.getString(R.string.applicationId), postParams).setName(title)
			                                                                                               .setDescription(description)
			                                                                                               .setLink(url)
			                                                                                               .build();
			fbDlg.setOnCompleteListener(new OnCompleteListener() {
				@Override
				public void onComplete(Bundle bundle, FacebookException e) {
					EventBus.getDefault()
					        .post(new FBShareCompleteEvent());
					fbDlg.dismiss();
				}
			});
			fbDlg.show();
		}
	}


	public static void share(PhotoDB photoDB, final Intent intent) {
		final String title = !TextUtils.isEmpty(photoDB.getTitle()) ?
		                     photoDB.getTitle() :
		                     "";
		final String desc = !TextUtils.isEmpty(photoDB.getDescription()) ?
		                    photoDB.getDescription() :
		                    "";
		final String url = !TextUtils.isEmpty(photoDB.getUrls()
		                                             .getHd()) ?
		                   photoDB.getUrls()
		                          .getHd() :
		                   photoDB.getUrls()
		                          .getNormal();
		share(title, desc, url, intent);
	}


	public static void share(final String title, final String description, final String url, final Intent intent) {
		if (!TextUtils.isEmpty(url)) {
			Call<Response> tinyUrlCall = Api.Retrofit.create(TinyUrl.class)
			                                         .getTinyUrl(url);
			tinyUrlCall.enqueue(new Callback<Response>() {
				@Override
				public void onResponse(Call<Response> call, retrofit2.Response<Response> res) {
					if (res.isSuccessful()) {
						Response response = res.body();
						String text = App.Instance.getString(R.string.lbl_share_item_content,
						                                     description,
						                                     TextUtils.isEmpty(response.getResult()) ?
						                                     url :
						                                     response.getResult(),
						                                     Prefs.getInstance()
						                                          .getAppDownloadInfo());
						intent.putExtra(Intent.EXTRA_SUBJECT, title);
						intent.putExtra(Intent.EXTRA_TEXT, text);
						EventBus.getDefault()
						        .post(new CompleteShareEvent(intent));
					} else {
						onFailure(null, null);
					}
				}

				@Override
				public void onFailure(Call<Response> call, Throwable t) {
					String text = App.Instance.getString(R.string.lbl_share_item_content,
					                                     description,
					                                     url,
					                                     Prefs.getInstance()
					                                          .getAppDownloadInfo());
					intent.putExtra(Intent.EXTRA_SUBJECT, title);
					intent.putExtra(Intent.EXTRA_TEXT, text);
					EventBus.getDefault()
					        .post(new CompleteShareEvent(intent));
				}
			});
		}
	}


	public static void share(final Context cxt, final String title, final String description, final String url) {
		if (!TextUtils.isEmpty(url)) {
			Call<Response> tinyUrlCall = Api.Retrofit.create(TinyUrl.class)
			                                         .getTinyUrl(url);
			tinyUrlCall.enqueue(new Callback<Response>() {
				@Override
				public void onResponse(Call<Response> call, retrofit2.Response<Response> res) {
					if (res.isSuccessful()) {
						Response response = res.body();
						String text = App.Instance.getString(R.string.lbl_share_item_content,
						                                     description,
						                                     TextUtils.isEmpty(response.getResult()) ?
						                                     url :
						                                     response.getResult(),
						                                     Prefs.getInstance()
						                                          .getAppDownloadInfo());

						Intent intent = com.chopping.utils.Utils.getShareInformation(title, text);
						intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
						cxt.startActivity(intent);
					} else {
						onFailure(null, null);
					}
				}

				@Override
				public void onFailure(Call<Response> call, Throwable t) {
					String text = App.Instance.getString(R.string.lbl_share_item_content,
					                                     description,
					                                     url,
					                                     Prefs.getInstance()
					                                          .getAppDownloadInfo());
					Intent intent = com.chopping.utils.Utils.getShareInformation(title, text);
					intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
					cxt.startActivity(intent);
				}
			});
		}
	}

	public static boolean validateDateTimeSelection(int year, int month, int dayOfMonth, View anchorV) {
		Calendar c = Calendar.getInstance();
		int thisMonth = c.get(Calendar.MONTH);
		int thisYear = c.get(Calendar.YEAR);
		int today = c.get(Calendar.DAY_OF_MONTH);
		if (year < 1998) {
			Snackbar snackbar = make(anchorV, R.string.lbl_earlier_than_1998, Snackbar.LENGTH_INDEFINITE);
			final WeakReference<Snackbar> barRef = new WeakReference<>(snackbar);
			snackbar.setAction(android.R.string.ok, new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					if (barRef.get() != null) {
						barRef.get()
						      .dismiss();
					}
				}
			});
			snackbar.show();
			return false;
		} else if (year > thisYear || (year == thisYear && month > thisMonth + 1) || ((year == thisYear && month == thisMonth + 1 && dayOfMonth != -1 && dayOfMonth > today))) {
			Snackbar snackbar = make(anchorV, R.string.lbl_before_today, Snackbar.LENGTH_INDEFINITE);
			final WeakReference<Snackbar> barRef = new WeakReference<>(snackbar);
			snackbar.setAction(android.R.string.ok, new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					if (barRef.get() != null) {
						barRef.get()
						      .dismiss();
					}
				}
			});
			snackbar.show();
			return false;
		}
		return true;
	}

	@NonNull
	public static SectionedGridRecyclerViewAdapter.Section[] extractSections(RealmResults<? extends RealmObject> data) {
		List<SectionedGridRecyclerViewAdapter.Section> sections = new ArrayList<>();
		String usedSection = null;
		SimpleDateFormat fmt = new SimpleDateFormat("yyyy-M-d");
		int position = 0;
		for (RealmObject object : data) {
			PhotoDB photoDB = (PhotoDB) object;

			String date = fmt.format(photoDB.getDate());
			String[] dateUnitGroup = date.split("-");
			StringBuilder sb = new StringBuilder(3);
			sb.append(dateUnitGroup[0])
			  .append('-')
			  .append(dateUnitGroup[1]);
			String candidate = sb.toString();
			if (!TextUtils.equals(usedSection, candidate)) {
				usedSection = candidate;
				sections.add(new SectionedGridRecyclerViewAdapter.Section(position, candidate));
			}
			position++;
		}
		SectionedGridRecyclerViewAdapter.Section[] sectionsArray = new SectionedGridRecyclerViewAdapter.Section[sections.size()];
		sectionsArray = sections.toArray(sectionsArray);
		return sectionsArray;
	}
}
