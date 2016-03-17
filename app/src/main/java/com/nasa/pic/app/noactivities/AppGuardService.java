package com.nasa.pic.app.noactivities;


import com.google.android.gms.gcm.GcmNetworkManager;
import com.google.android.gms.gcm.GcmTaskService;
import com.google.android.gms.gcm.TaskParams;

public final class AppGuardService extends GcmTaskService {
	private static final String TAG = "AppGuardService";

	@Override
	public int onRunTask( TaskParams taskParams ) {
		return GcmNetworkManager.RESULT_SUCCESS;
	}
}
