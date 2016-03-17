package com.nasa.pic.ds;


import android.support.annotation.NonNull;

import com.chopping.rest.RestObject;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.SerializedName;

import io.realm.Realm;
import io.realm.RealmObject;

public final class PhotoUrl extends RestObject {

	@JsonProperty("reqId")
	private String mReqId;


	@SerializedName("normal")
	@JsonProperty("normal")
	private String mNormal;

	@SerializedName("hd")
	@JsonProperty("hd")
	private String mHd;


	@Override
	public String getReqId() {
		return mReqId;
	}

	public void setReqId( String reqId ) {
		mReqId = reqId;
	}

	public String getNormal() {
		return mNormal;
	}

	public void setNormal( String normal ) {
		mNormal = normal;
	}

	public String getHd() {
		return mHd;
	}

	public void setHd( String hd ) {
		mHd = hd;
	}

	@Override
	public long getReqTime() {
		//Can ignore.
		return 0;
	}

	@NonNull
	@Override
	protected RealmObject[] newInstances( Realm db, int status ) {
		PhotoUrlDB dbPayload = new PhotoUrlDB();
		dbPayload.setReqId( getReqId() );
		dbPayload.setHd( getHd() );
		dbPayload.setNormal( getNormal() );
		dbPayload.setStatus( status );
		return new RealmObject[] { dbPayload };
	}

	@Override
	public Class<? extends RealmObject> DBType() {
		return PhotoUrlDB.class;
	}

	@Override
	public RestObject newFromDB( RealmObject dbItem ) {
		PhotoUrlDB dbPayLoad = (PhotoUrlDB) dbItem;
		PhotoUrl   url   = new PhotoUrl();
		url.setReqId( dbPayLoad.getReqId() );
		url.setHd( dbPayLoad.getHd() );
		url.setNormal( dbPayLoad.getNormal() );
		return url;
	}
}
