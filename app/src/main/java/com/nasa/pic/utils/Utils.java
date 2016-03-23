package com.nasa.pic.utils;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import com.facebook.FacebookException;
import com.facebook.widget.WebDialog;
import com.facebook.widget.WebDialog.OnCompleteListener;
import com.nasa.pic.R;
import com.nasa.pic.app.App;
import com.nasa.pic.ds.PhotoDB;
import com.nasa.pic.events.CompleteShareEvent;
import com.tinyurl4j.Api;
import com.tinyurl4j.Api.TinyUrl;
import com.tinyurl4j.data.Response;

import de.greenrobot.event.EventBus;
import retrofit2.Call;
import retrofit2.Callback;

public final class Utils {

	public static void facebookShare(Context cxt, PhotoDB photoDB) {
		final WebDialog fbDlg;
		Bundle postParams = new Bundle();
		String title = !TextUtils.isEmpty(photoDB.getTitle()) ? photoDB.getTitle() : "";
		String desc = !TextUtils.isEmpty(photoDB.getDescription()) ? photoDB.getDescription() : "";
		String url = !TextUtils.isEmpty(photoDB.getUrls().getHd()) ? photoDB.getUrls().getHd() :
				photoDB.getUrls().getNormal();
		facebookShare(cxt, title, desc, url);
	}


	public static void facebookShare(Context cxt, String title, String description, String url) {
		final WebDialog fbDlg;
		Bundle postParams = new Bundle();
		if (!TextUtils.isEmpty(url)) {
			fbDlg = new WebDialog.FeedDialogBuilder(cxt, cxt.getString(R.string.applicationId), postParams).setName(
					title).setDescription(description).setLink(url).build();
			fbDlg.setOnCompleteListener(new OnCompleteListener() {
				@Override
				public void onComplete(Bundle bundle, FacebookException e) {
					fbDlg.dismiss();
				}
			});
			fbDlg.show();
		}
	}


	public static void share(PhotoDB photoDB, final Intent intent) {
		final String title = !TextUtils.isEmpty(photoDB.getTitle()) ? photoDB.getTitle() : "";
		final String desc = !TextUtils.isEmpty(photoDB.getDescription()) ? photoDB.getDescription() : "";
		final String url = !TextUtils.isEmpty(photoDB.getUrls().getHd()) ? photoDB.getUrls().getHd() :
				photoDB.getUrls().getNormal();
		share(title, desc, url, intent);
	}


	public static void share(final String title, final String description, final String url, final Intent intent) {
		if (!TextUtils.isEmpty(url)) {
			Call<Response> tinyUrlCall = Api.Retrofit.create(TinyUrl.class).getTinyUrl(url);
			tinyUrlCall.enqueue(new Callback<Response>() {
				@Override
				public void onResponse(Call<Response> call, retrofit2.Response<Response> res) {
					if (res.isSuccess()) {
						Response response = res.body();
						String text = App.Instance.getString(R.string.lbl_share_item_content, description,
								TextUtils.isEmpty(response.getResult()) ? url : response.getResult(),
								Prefs.getInstance().getAppDownloadInfo());
						intent.putExtra(Intent.EXTRA_SUBJECT, title);
						intent.putExtra(Intent.EXTRA_TEXT, text);
						EventBus.getDefault().post(new CompleteShareEvent(intent));
					} else {
						onFailure(null, null);
					}
				}

				@Override
				public void onFailure(Call<Response> call, Throwable t) {
					String text = App.Instance.getString(R.string.lbl_share_item_content, description, url,
							Prefs.getInstance().getAppDownloadInfo());
					intent.putExtra(Intent.EXTRA_SUBJECT, title);
					intent.putExtra(Intent.EXTRA_TEXT, text);
					EventBus.getDefault().post(new CompleteShareEvent(intent));
				}
			});
		}
	}


	public static void share(final Context cxt, final String title, final String description, final String url ) {
		if (!TextUtils.isEmpty(url)) {
			Call<Response> tinyUrlCall = Api.Retrofit.create(TinyUrl.class).getTinyUrl(url);
			tinyUrlCall.enqueue(new Callback<Response>() {
				@Override
				public void onResponse(Call<Response> call, retrofit2.Response<Response> res) {
					if (res.isSuccess()) {
						Response response = res.body();
						String text = App.Instance.getString(R.string.lbl_share_item_content, description,
								TextUtils.isEmpty(response.getResult()) ? url : response.getResult(),
								Prefs.getInstance().getAppDownloadInfo());

						Intent intent = com.chopping.utils.Utils.getShareInformation(title, text);
						intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
						cxt.startActivity(intent);
					} else {
						onFailure(null, null);
					}
				}

				@Override
				public void onFailure(Call<Response> call, Throwable t) {
					String text = App.Instance.getString(R.string.lbl_share_item_content, description, url,
							Prefs.getInstance().getAppDownloadInfo());
					Intent intent = com.chopping.utils.Utils.getShareInformation(title, text);
					intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
					cxt.startActivity(intent);
				}
			});
		}
	}
}
