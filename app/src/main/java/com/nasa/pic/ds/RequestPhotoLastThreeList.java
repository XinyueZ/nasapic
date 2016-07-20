package com.nasa.pic.ds;


import com.chopping.rest.RestObject;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.SerializedName;

import io.realm.Realm;
import io.realm.RealmObject;

public final class RequestPhotoLastThreeList extends RestObject {
	@JsonProperty("reqId")
	@SerializedName("reqId")
	private String mReqId;
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
	protected RealmObject[] newInstances(Realm db, int status ) {
		RequestPhotoListDB dbPayLoad = new RequestPhotoListDB();
		dbPayLoad.setReqId( getReqId() );
		dbPayLoad.setTimeZone( getTimeZone() );
		dbPayLoad.setStatus( status );
		return new RealmObject[] { dbPayLoad };
	}


	@Override
	public RestObject newFromDB(RealmObject dbItem ) {
		RequestPhotoLastThreeListDB        dbPayLoad        = (RequestPhotoLastThreeListDB) dbItem;
		RequestPhotoLastThreeList requestPhotoList = new RequestPhotoLastThreeList();
		requestPhotoList.setReqId( dbPayLoad.getReqId() );
		requestPhotoList.setTimeZone( dbPayLoad.getTimeZone() );
		return requestPhotoList;
	}
}
