package com.nasa.pic.ds;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.RealmClass;

@RealmClass
public   class RequestPhotoDayListDB extends RealmObject {
	@PrimaryKey
	private String reqId;
	private long   reqTime;
	private int    status;

	private RealmList<PhotoDateTimeDB> dates;
	private String                     timeZone;


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

	public RealmList<PhotoDateTimeDB> getDates() {
		return dates;
	}

	public void setDates( RealmList<PhotoDateTimeDB> dates ) {
		this.dates = dates;
	}

	public String getTimeZone() {
		return timeZone;
	}

	public void setTimeZone( String timeZone ) {
		this.timeZone = timeZone;
	}
}
