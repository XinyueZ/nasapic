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
		String title = !TextUtils.isEmpty(photoDB.getTitle()) ? photoDB.getTitle() : null;
		String desc = !TextUtils.isEmpty(photoDB.getDescription()) ? photoDB.getDescription() : null;
		String url = !TextUtils.isEmpty(photoDB.getUrls().getHd()) ? photoDB.getUrls().getHd() :
				photoDB.getUrls().getNormal();
		if (desc == null && title == null) {
			fbDlg = new WebDialog.FeedDialogBuilder(cxt, cxt.getString(R.string.applicationId), postParams).setLink(url)
					.build();
		} else if (desc != null && title == null) {
			fbDlg = new WebDialog.FeedDialogBuilder(cxt, cxt.getString(R.string.applicationId), postParams)
					.setDescription(desc).setLink(url).build();
		} else if (desc == null && title != null) {
			fbDlg = new WebDialog.FeedDialogBuilder(cxt, cxt.getString(R.string.applicationId), postParams).setName(
					title).setLink(url).build();
		} else {
			fbDlg = new WebDialog.FeedDialogBuilder(cxt, cxt.getString(R.string.applicationId), postParams).setName(
					title).setDescription(desc).setLink(url).build();
		}
		fbDlg.setOnCompleteListener(new OnCompleteListener() {
			@Override
			public void onComplete(Bundle bundle, FacebookException e) {
				fbDlg.dismiss();
			}
		});
		fbDlg.show();
	}


	public static void share(  PhotoDB photoDB, final Intent intent) {
		final String title = !TextUtils.isEmpty(photoDB.getTitle()) ? photoDB.getTitle() : null;
		final String desc = !TextUtils.isEmpty(photoDB.getDescription()) ? photoDB.getDescription() : null;
		final String url = !TextUtils.isEmpty(photoDB.getUrls().getHd()) ? photoDB.getUrls().getHd() :
				photoDB.getUrls().getNormal();



		Call<Response> tinyUrlCall = Api.Retrofit.create( TinyUrl.class )
				.getTinyUrl( url );
		tinyUrlCall.enqueue( new Callback<Response>() {
			@Override
			public void onResponse(Call<Response> call,  retrofit2.Response<Response> res ) {
				if(res.isSuccess()) {
					Response response  = res.body();
					String text = App.Instance.getString( R.string.lbl_share_item_content,
							desc,
							TextUtils.isEmpty( response.getResult() ) ? url:
									response.getResult(),
							Prefs.getInstance()
									.getAppDownloadInfo()
					);
					intent.putExtra(
							Intent.EXTRA_SUBJECT,
							title
					);
					intent.putExtra(
							Intent.EXTRA_TEXT,
							text
					);
					EventBus.getDefault().post(new CompleteShareEvent(intent));
				} else {
					onFailure( null, null );
				}
			}

			@Override
			public void onFailure( Call<Response> call, Throwable t ) {
				String text = App.Instance.getString( R.string.lbl_share_item_content,
						desc,
						url,
						Prefs.getInstance().getAppDownloadInfo()
				);
				intent.putExtra(
						Intent.EXTRA_SUBJECT,
						title
				);
				intent.putExtra(
						Intent.EXTRA_TEXT,
						text
				);
				EventBus.getDefault().post(new CompleteShareEvent(intent));
			}
		} );

	}

}
