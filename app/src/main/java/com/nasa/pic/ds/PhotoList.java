package com.nasa.pic.ds;


import com.chopping.rest.RestObject;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.SerializedName;

import java.util.List;

import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.RealmResults;

public final class PhotoList extends RestObject {
	@JsonProperty("status")
	@SerializedName("status")
	private int    mStatus;
	@JsonProperty("reqId")
	@SerializedName("reqId")
	private String mReqId;
	@JsonProperty("result")
	@SerializedName("result")
	private List<Photo> mResult;

	public int getStatus() {
		return mStatus;
	}

	public void setStatus( int status ) {
		mStatus = status;
	}

	@Override
	public String getReqId() {
		return mReqId;
	}

	public void setReqId( String reqId ) {
		mReqId = reqId;
	}

	public List<Photo> getResult() {
		return mResult;
	}

	public void setResult( List<Photo> result ) {
		mResult = result;
	}

	@Override
	public long getReqTime() {
		//Can ignore.
		return 0;
	}


	@Override
	public Class<? extends RealmObject> DBType() {
		return PhotoListDB.class;
	}


	@Override
	protected RealmObject[] newInstances( Realm db, int status ) {
		List<Photo>        photoList  = getResult();
		if(photoList == null) return null;
		RealmList<PhotoDB> photoDBList = new RealmList<>();
		for( Photo photo : photoList ) {
			RealmObject[] objects = photo.newInstances(
					db,
					status
			);
			photoDBList.add( (PhotoDB) objects[ 0 ] );
		}

		PhotoListDB photoListDB = new PhotoListDB();
		photoListDB.setReqId( getReqId() );
		photoListDB.setStatus( getStatus() );
		photoListDB.setResult( photoDBList );
		photoListDB.setReqTime( System.currentTimeMillis() );

		//Update requests
		RealmResults<RequestPhotoListDB> listRequestDBs = db.where( RequestPhotoListDB.class )
														  .equalTo(
																  "reqId",
																  getReqId()
														  )
														  .findAll();
		if(listRequestDBs.size() > 0) {
			RequestPhotoListDB reqItemDb = listRequestDBs.first();
			reqItemDb.setStatus( status );
		}



		return new RealmObject[] { photoListDB ,
								   //reqItemDb
		};
	}
}
