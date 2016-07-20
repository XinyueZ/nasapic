package com.nasa.pic.api;

import com.nasa.pic.ds.PhotoList;
import com.nasa.pic.ds.RequestPhotoDayList;
import com.nasa.pic.ds.RequestPhotoLastThreeList;
import com.nasa.pic.ds.RequestPhotoList;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface Api {
	@POST("/month_list")
	Call<PhotoList> getPhotoMonthList( @Body RequestPhotoList requestPhotoList );



	@POST("/list")
	Call<PhotoList> getPhotoList( @Body RequestPhotoDayList requestPhotoList );

	@POST("/last_three_list")
	Call<PhotoList> getPhotoListLast3Days( @Body RequestPhotoLastThreeList requestPhotoList );
}