package com.nasa.pic.customtab;


import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.support.customtabs.CustomTabsClient;
import android.support.customtabs.CustomTabsIntent;
import android.support.customtabs.CustomTabsServiceConnection;
import android.support.customtabs.CustomTabsSession;

public final class CustomTabActivityHelper implements ServiceConnectionCallback {
	private CustomTabsSession mCustomTabsSession;
	private CustomTabsClient mClient;
	private CustomTabsServiceConnection mConnection;
	private ConnectionCallback mConnectionCallback;

	public static void openCustomTab(Activity activity, CustomTabsIntent customTabsIntent, String title,
			String description, String urlToPhoto, Date datetime, String type, CustomTabFallback fallback) {
		String packageName = CustomTabsHelper.getPackageNameToUse(activity);

		//If we cant find a package name, it means theres no browser that supports
		//Chrome Custom Tabs installed. So, we fallback to the webview
		if (packageName == null) {
			if (fallback != null) {
				fallback.openUri(activity, title, description, urlToPhoto, datetime, type);
			}
		} else {
			customTabsIntent.intent.setPackage(packageName);
			customTabsIntent.launchUrl(activity, Uri.parse(urlToPhoto));
		}
	}

	public interface CustomTabFallback {
		void openUri(Activity activity, String title, String description, String urlToPhoto, Date datetime,
				String type);
	}


	/**
	 * Unbinds the Activity from the Custom Tabs Service.
	 *
	 * @param activity
	 * 		the activity that is connected to the service.
	 */
	public void unbindCustomTabsService(Activity activity) {
		if (mConnection == null) {
			return;
		}
		activity.unbindService(mConnection);
		mClient = null;
		mCustomTabsSession = null;
		mConnection = null;
	}

	/**
	 * Binds the Activity to the Custom Tabs Service.
	 *
	 * @param activity
	 * 		the activity to be bound to the service.
	 */
	public void bindCustomTabsService(Activity activity) {
		if (mClient != null) {
			return;
		}

		String packageName = CustomTabsHelper.getPackageNameToUse(activity);
		if (packageName == null) {
			return;
		}

		mConnection = new ServiceConnection(this);
		CustomTabsClient.bindCustomTabsService(activity, packageName, mConnection);
	}


	/**
	 * @see {@link CustomTabsSession#mayLaunchUrl(Uri, Bundle, List)}.
	 * @return true if call to mayLaunchUrl was accepted.
	 */
	public boolean mayLaunchUrl(Uri uri, Bundle extras, List<Bundle> otherLikelyBundles) {
		if (mClient == null) return false;

		CustomTabsSession session = getSession();
		if (session == null) return false;

		return session.mayLaunchUrl(uri, extras, otherLikelyBundles);
	}


	/**
	 * A Callback for when the service is connected or disconnected. Use those callbacks to handle UI changes when the
	 * service is connected or disconnected.
	 */
	public interface ConnectionCallback {
		/**
		 * Called when the service is connected.
		 */
		void onCustomTabsConnected();

		/**
		 * Called when the service is disconnected.
		 */
		void onCustomTabsDisconnected();
	}

	/**
	 * Register a Callback to be called when connected or disconnected from the Custom Tabs Service.
	 *
	 * @param connectionCallback
	 */
	public void setConnectionCallback(ConnectionCallback connectionCallback) {
		this.mConnectionCallback = connectionCallback;
	}

	/**
	 * Creates or retrieves an exiting CustomTabsSession.
	 *
	 * @return a CustomTabsSession.
	 */
	public CustomTabsSession getSession() {
		if (mClient == null) {
			mCustomTabsSession = null;
		} else if (mCustomTabsSession == null) {
			mCustomTabsSession = mClient.newSession(null);
		}
		return mCustomTabsSession;
	}

	@Override
	public void onServiceConnected(CustomTabsClient client) {
		mClient = client;
		mClient.warmup(0L);
		if (mConnectionCallback != null) {
			mConnectionCallback.onCustomTabsConnected();
		}
	}

	@Override
	public void onServiceDisconnected() {
		mClient = null;
		mCustomTabsSession = null;
		if (mConnectionCallback != null) {
			mConnectionCallback.onCustomTabsDisconnected();
		}
	}



}
