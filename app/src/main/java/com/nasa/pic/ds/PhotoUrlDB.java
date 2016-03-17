package com.nasa.pic.ds;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.RealmClass;

@RealmClass
public   class PhotoUrlDB extends RealmObject {
	@PrimaryKey
	private String reqId;
	private long   reqTime;
	private int    status;

	private String hd;
	private String normal;

	public String getHd() {
		return hd;
	}

	public void setHd( String hd ) {
		this.hd = hd;
	}

	public String getNormal() {
		return normal;
	}

	public void setNormal( String normal ) {
		this.normal = normal;
	}

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
}
