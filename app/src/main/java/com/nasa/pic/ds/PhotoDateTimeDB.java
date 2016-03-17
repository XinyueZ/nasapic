package com.nasa.pic.ds;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.RealmClass;

@RealmClass
public   class PhotoDateTimeDB extends RealmObject {
	@PrimaryKey
	private String   dateTime;

	public String getDateTime() {
		return dateTime;
	}

	public void setDateTime( String dateTime ) {
		this.dateTime = dateTime;
	}
}
