package com.nasa.pic.app.noactivities;


import com.chopping.application.LL;
import com.google.android.gms.gcm.GcmNetworkManager;
import com.google.android.gms.gcm.GcmTaskService;
import com.google.android.gms.gcm.TaskParams;
import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.nasa.pic.utils.Prefs;

import io.realm.RealmObject;
import retrofit2.converter.gson.GsonConverterFactory;

public final class AppGuardService extends GcmTaskService {
	private static final String TAG = "AppGuardService";
	private static final Gson GSON = new GsonBuilder().setDateFormat("yyyy-M-d").setExclusionStrategies(new ExclusionStrategy() {
		@Override
		public boolean shouldSkipField(FieldAttributes f) {
			return f.getDeclaringClass().equals(RealmObject.class);
		}

		@Override
		public boolean shouldSkipClass(Class<?> clazz) {
			return false;
		}
	}).create();

	public static retrofit2.Retrofit Retrofit;



	@Override
	public int onRunTask( TaskParams taskParams ) {
		buildRetrofit();
		return GcmNetworkManager.RESULT_SUCCESS;
	}

	public static void buildRetrofit() {
		Prefs prefs = Prefs.getInstance();
		int count = prefs.getApiCount();
		int curPos = prefs.getCurrentApiPosition();
		String meta = prefs.getApiMeta();
		int apiIndex = curPos % count;
		String apiUrl = String.format(meta, apiIndex);
		Retrofit = new retrofit2.Retrofit.Builder().addConverterFactory(GsonConverterFactory.create(GSON))
				.baseUrl(apiUrl).build();
		LL.d("Retrofit url: " + apiUrl);
		prefs.setCurrentApiPosition(++curPos);
	}
}
