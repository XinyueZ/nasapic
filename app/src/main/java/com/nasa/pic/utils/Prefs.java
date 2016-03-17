package com.nasa.pic.utils;

import android.content.Context;

import com.chopping.application.BasicPrefs;

/**
 * Store app and device information.
 *
 * @author Chris.Xinyue Zhao
 */
public final class Prefs extends BasicPrefs {
	/**
	 * Storage. Whether the "End User License Agreement" has been shown and agreed at application's first start.
	 * <p/>
	 * {@code true} if EULA has been shown and agreed.
	 */
	private static final String KEY_EULA_SHOWN              = "key_eula_shown";
	/**
	 * Download-info of application.
	 */
	private static final String KEY_APP_DOWNLOAD           = "key.app.download";

	private static final String KEY_API_COUNT = "api_count";

	private static final String KEY_CURRENT_API_POSITION = "key.api.position";

	/**
	 * The Instance.
	 */
	private static Prefs sInstance;


	private Prefs() {
		super( null );
	}

	/**
	 * Created a DeviceData storage.
	 *
	 * @param context
	 * 		A context object.
	 */
	private Prefs( Context context ) {
		super( context );
	}

	/**
	 * Singleton method.
	 *
	 * @param context
	 * 		A context object.
	 *
	 * @return single instance of DeviceData
	 */
	public static Prefs createInstance( Context context ) {
		if( sInstance == null ) {
			synchronized( Prefs.class ) {
				if( sInstance == null ) {
					sInstance = new Prefs( context );
				}
			}
		}
		return sInstance;
	}

	/**
	 * Singleton getInstance().
	 *
	 * @return The instance of Prefs.
	 */
	public static Prefs getInstance() {
		return sInstance;
	}


	/**
	 * Whether the "End User License Agreement" has been shown and agreed at application's first start.
	 * <p/>
	 *
	 * @return {@code true} if EULA has been shown and agreed.
	 */
	public boolean isEULAOnceConfirmed() {
		return getBoolean(
				KEY_EULA_SHOWN,
				false
		);
	}

	/**
	 * Set whether the "End User License Agreement" has been shown and agreed at application's first start.
	 * <p/>
	 *
	 * @param isConfirmed
	 * 		{@code true} if EULA has been shown and agreed.
	 */
	public void setEULAOnceConfirmed( boolean isConfirmed ) {
		setBoolean(
				KEY_EULA_SHOWN,
				isConfirmed
		);
	}


	/**
	 * @return Download-info of application.
	 */
	public String getAppDownloadInfo() {
		return getString( KEY_APP_DOWNLOAD, null );
	}

	/**
	 * Set download-info of application.
	 */
	public void setAppDownloadInfo( String appDownloadInfo ) {
		setString( KEY_APP_DOWNLOAD, appDownloadInfo );
	}


	public void setCurrentApiPosition(int pos) {
		setInt(KEY_CURRENT_API_POSITION, pos);
	}

	public int getCurrentApiPosition() {
		return getInt(KEY_CURRENT_API_POSITION, 0);
	}

	public int getApiCount() {
		return getInt(KEY_API_COUNT, 3);
	}
 }
