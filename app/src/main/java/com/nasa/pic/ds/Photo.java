package com.nasa.pic.ds;


import java.util.Date;

import com.chopping.rest.RestObject;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.SerializedName;

import io.realm.Realm;
import io.realm.RealmObject;
import io.realm.RealmResults;

public final class Photo extends RestObject {
	@JsonProperty("reqId")
	@SerializedName("reqId")
	private String   mReqId;
	@JsonProperty("description")
	@SerializedName("description")
	private String   mDescription;
	@JsonProperty("date")
	@SerializedName("date")
	private Date     mDate;
	@JsonProperty("title")
	@SerializedName("title")
	private String   mTitle;
	@JsonProperty("type")
	@SerializedName("type")
	private String   mType;
	@JsonProperty("urls")
	@SerializedName("urls")
	private PhotoUrl mPhotoUrl;

	@Override
	public String getReqId() {
		return mReqId;
	}

	public void setReqId( String reqId ) {
		mReqId = reqId;
	}

	public String getDescription() {
		return mDescription;
	}

	public void setDescription( String description ) {
		mDescription = description;
	}

	public Date getDate() {
		return mDate;
	}

	public void setDate( Date date ) {
		mDate = date;
	}

	public String getTitle() {
		return mTitle;
	}

	public void setTitle( String title ) {
		mTitle = title;
	}

	public String getType() {
		return mType;
	}

	public void setType( String type ) {
		mType = type;
	}

	public PhotoUrl getPhotoUrl() {
		return mPhotoUrl;
	}

	public void setPhotoUrl( PhotoUrl photoUrl ) {
		mPhotoUrl = photoUrl;
	}

	@Override
	public long getReqTime() {
		//Can ignore.
		return 0;
	}

	@Override
	protected RealmObject[] newInstances( Realm db, int status ) {
		PhotoDB    photoDbPayload;
		PhotoUrlDB photoUrlDbPayload;

		switch( status ) {
			case DELETE_SYNCED:
				RealmResults<? extends RealmObject> dbItems = db.where( this.DBType() )
																.equalTo(
																		"reqId",
																		getReqId()
																)
																.findAll();
				photoDbPayload = (PhotoDB) dbItems.first();
				break;
			default:
				photoUrlDbPayload = new PhotoUrlDB();
				photoUrlDbPayload.setReqId( getReqId() );
				photoUrlDbPayload.setReqTime( System.currentTimeMillis() );
				photoUrlDbPayload.setStatus( status );
				photoUrlDbPayload.setHd( getPhotoUrl().getHd() );
				photoUrlDbPayload.setNormal( getPhotoUrl().getNormal() );


				photoDbPayload = new PhotoDB();
				photoDbPayload.setReqId( getReqId() );
				photoDbPayload.setReqTime( System.currentTimeMillis() );
				photoDbPayload.setStatus( status );
				photoDbPayload.setTitle( getTitle() );
				photoDbPayload.setDescription( getDescription() );
				photoDbPayload.setDate(   getDate()   );
				photoDbPayload.setUrls( photoUrlDbPayload );
				photoDbPayload.setType( getType() );
				break;
		}
		return new RealmObject[] { photoDbPayload };
	}

	@Override
	public Class<? extends RealmObject> DBType() {
		return PhotoDB.class;
	}

	@Override
	public RestObject newFromDB( RealmObject dbItem ) {
		PhotoDB photoDbPayload = (PhotoDB) dbItem;
		Photo   photo   = new Photo();
		photo.setReqId( photoDbPayload.getReqId() );
		photo.setDate( photoDbPayload.getDate() );
		photo.setType( photoDbPayload.getType() );
		photo.setTitle( photoDbPayload.getTitle() );
		photo.setDescription( photoDbPayload.getDescription() );

		PhotoUrl photoUrl = new PhotoUrl();
		photoUrl.setReqId( photoDbPayload.getUrls()
								  .getReqId() );
		photoUrl.setHd( photoDbPayload.getUrls()
							   .getHd() );
		photoUrl.setNormal( photoDbPayload.getUrls()
								   .getNormal() );
		photo.setPhotoUrl( photoUrl );
		return photo;
	}
}
