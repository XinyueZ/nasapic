package com.nasa.pic.ds;


import java.util.ArrayList;
import java.util.List;

import com.chopping.rest.RestObject;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.SerializedName;

import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmObject;

public final class RequestPhotoDayList extends RestObject {
	@JsonProperty("reqId")
	@SerializedName("reqId")
	private String       mReqId;
	@JsonProperty("dates")
	@SerializedName("dates")
	private List<String> mDateTimes;
	@JsonProperty("timeZone")
	@SerializedName("timeZone")
	private String       mTimeZone;


	@Override
	public String getReqId() {
		return mReqId;
	}

	public void setReqId( String reqId ) {
		mReqId = reqId;
	}

	public List<String> getDateTimes() {
		return mDateTimes;
	}

	public void setDateTimes( List<String> dateTimes ) {
		mDateTimes = dateTimes;
	}

	public String getTimeZone() {
		return mTimeZone;
	}

	public void setTimeZone( String timeZone ) {
		mTimeZone = timeZone;
	}


	@Override
	public long getReqTime() {
		//Can ignore.
		return 0;
	}


	@Override
	public Class<? extends RealmObject> DBType() {
		return RequestPhotoListDB.class;
	}


	@Override
	protected RealmObject[] newInstances( Realm db, int status ) {
		RequestPhotoDayListDB dbPayLoad = new RequestPhotoDayListDB();
		dbPayLoad.setReqId( getReqId() );
		//Date-times
		RealmList<PhotoDateTimeDB> dateTimeDBs = new RealmList<>();
		List<String>               dateTimes   = getDateTimes();
		for( String datetime : dateTimes ) {
			PhotoDateTimeDB photoDateTimeDB = new PhotoDateTimeDB();
			photoDateTimeDB.setDateTime( datetime );
			dateTimeDBs.add( photoDateTimeDB );
		}
		dbPayLoad.setTimeZone( getTimeZone() );
		dbPayLoad.setStatus( status );
		return new RealmObject[] { dbPayLoad };
	}


	@Override
	public RestObject newFromDB( RealmObject dbItem ) {
		RequestPhotoDayListDB dbPayLoad           = (RequestPhotoDayListDB) dbItem;
		RequestPhotoDayList   requestPhotoDayList = new RequestPhotoDayList();
		requestPhotoDayList.setReqId( dbPayLoad.getReqId() );
		List<String> datetimes = new ArrayList<>();
		for( PhotoDateTimeDB photoDateTimeDBPayload : dbPayLoad.getDates() ) {
			datetimes.add( photoDateTimeDBPayload.getDateTime() );
			requestPhotoDayList.setDateTimes( datetimes );
		}
		requestPhotoDayList.setTimeZone( dbPayLoad.getTimeZone() );
		return requestPhotoDayList;
	}
}
