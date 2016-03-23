package com.nasa.pic.app.activities;

import java.util.Date;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.support.v7.app.ActionBar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.nasa.pic.R;
import com.nasa.pic.databinding.ActivityWebviewBinding;


public final class WebViewActivity extends AppNormalActivity {
	/**
	 * Main layout for this component.
	 */
	private static final int LAYOUT = R.layout.activity_webview;
	/**
	 * The menu to this view.
	 */
	private static final int MENU = R.menu.menu_webview;

	/**
	 * Data-binding.
	 */
	private ActivityWebviewBinding mBinding;


	public static void showInstance(Context cxt, String title, String description, String urlToPhoto, Date datetime,
			String type) {
		Intent intent = new Intent(cxt, WebViewActivity.class);
		intent.putExtra(EXTRAS_TYPE, type);
		intent.putExtra(EXTRAS_TITLE, title);
		intent.putExtra(EXTRAS_DATE, datetime);
		intent.putExtra(EXTRAS_DESCRIPTION, description);
		intent.putExtra(EXTRAS_URL_TO_PHOTO, urlToPhoto);
		intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
		cxt.startActivity(intent);
	}


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mBinding = DataBindingUtil.setContentView(this, LAYOUT);
		setUpErrorHandling((ViewGroup) findViewById(R.id.error_content));

		initPull2Load(mBinding.contentSrl);
		mBinding.contentSrl.setOnRefreshListener(new OnRefreshListener() {
			@Override
			public void onRefresh() {
				mBinding.webView.reload();
			}
		});

		//Actionbar and navi-drawer.
		setSupportActionBar(mBinding.toolbar);
		ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setDefaultDisplayHomeAsUpEnabled(true);
		actionBar.setTitle(getIntent().getStringExtra(EXTRAS_TITLE));


		WebSettings settings = mBinding.webView.getSettings();
		settings.setLoadWithOverviewMode(true);
		settings.setJavaScriptEnabled(true);
		settings.setLoadsImagesAutomatically(true);
		settings.setJavaScriptCanOpenWindowsAutomatically(true);
		settings.setCacheMode(WebSettings.LOAD_NORMAL);
		settings.setSupportZoom(true);
		settings.setBuiltInZoomControls(false);
		settings.setDomStorageEnabled(true);
		mBinding.webView.setWebViewClient(new WebViewClient() {
			@Override
			public void onPageStarted(WebView view, String url, Bitmap favicon) {
				mBinding.contentSrl.setRefreshing(true);
			}

			@Override
			public void onPageFinished(WebView view, String url) {
				mBinding.contentSrl.setRefreshing(false);
			}

			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				view.loadUrl(url);
				return true;
			}
		});
		handleIntent(getIntent());
	}



	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		setIntent(intent);
		handleIntent(getIntent());
	}

	/**
	 * Handle intent to show web.
	 *
	 * @param intent
	 * 		Data for the activity.
	 */
	private void handleIntent(Intent intent) {
		String url = intent.getStringExtra(EXTRAS_URL_TO_PHOTO);
		if (!TextUtils.isEmpty(url)) {
			mBinding.webView.loadUrl(url);
		}
	}


	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_forward:
			if (mBinding.webView.canGoForward()) {
				mBinding.webView.goForward();
			}
			break;
		case R.id.action_backward:
			if (mBinding.webView.canGoBack()) {
				mBinding.webView.goBack();
			}
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected int getMenuRes() {
		return MENU;
	}
}
