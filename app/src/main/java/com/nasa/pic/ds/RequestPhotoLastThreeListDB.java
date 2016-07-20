package com.nasa.pic.ds;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.RealmClass;

@RealmClass
public   class RequestPhotoLastThreeListDB extends RealmObject {
	@PrimaryKey
	private String reqId;
	private long   reqTime;
	private int    status;

	private String timeZone;


	public String getReqId() {
		return reqId;
	}

	public void setReqId( String reqId ) {
		this.reqId = reqId;
	}

	public long getReqTime() {
		return reqTime;
	}

	public void setReqTime( long reqTime ) {
		this.reqTime = reqTime;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus( int status ) {
		this.status = status;
	}


	public String getTimeZone() {
		return timeZone;
	}

	public void setTimeZone( String timeZone ) {
		this.timeZone = timeZone;
	}
}
