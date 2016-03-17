package com.nasa.pic.ds;


import com.chopping.rest.RestObject;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.SerializedName;

import io.realm.Realm;
import io.realm.RealmObject;

public final class RequestPhotoList extends RestObject {
	@JsonProperty("reqId")
	@SerializedName("reqId")
	private String mReqId;
	@JsonProperty("year")
	@SerializedName("year")
	private int    mYear;
	@JsonProperty("month")
	@SerializedName("month")
	private int    mMonth;
	@JsonProperty("timeZone")
	@SerializedName("timeZone")
	private String mTimeZone;


	@Override
	public String getReqId() {
		return mReqId;
	}

	public void setReqId( String reqId ) {
		mReqId = reqId;
	}

	public int getYear() {
		return mYear;
	}

	public void setYear( int year ) {
		mYear = year;
	}

	public int getMonth() {
		return mMonth;
	}

	public void setMonth( int month ) {
		mMonth = month;
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
		RequestPhotoListDB dbPayLoad = new RequestPhotoListDB();
		dbPayLoad.setReqId( getReqId() );
		dbPayLoad.setYear( getYear() );
		dbPayLoad.setMonth( getMonth() );
		dbPayLoad.setTimeZone( getTimeZone() );
		dbPayLoad.setStatus( status );
		return new RealmObject[] { dbPayLoad };
	}


	@Override
	public RestObject newFromDB( RealmObject dbItem ) {
		RequestPhotoListDB dbPayLoad        = (RequestPhotoListDB) dbItem;
		RequestPhotoList   requestPhotoList = new RequestPhotoList();
		requestPhotoList.setReqId( dbPayLoad.getReqId() );
		requestPhotoList.setYear( dbPayLoad.getYear() );
		requestPhotoList.setMonth( dbPayLoad.getMonth() );
		requestPhotoList.setTimeZone( dbPayLoad.getTimeZone() );
		return requestPhotoList;
	}
}
