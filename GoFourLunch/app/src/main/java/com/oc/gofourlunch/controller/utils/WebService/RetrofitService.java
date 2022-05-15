package com.oc.gofourlunch.controller.utils.WebService;


import com.oc.gofourlunch.model.GooglePlaces.GooglePlacesModel;
import com.oc.gofourlunch.model.GooglePlaces.PlaceResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Url;

public interface RetrofitService {
    @GET
    Call<GooglePlacesModel> getNearByPlaces(@Url String url);

    //https://maps.googleapis.com/maps/api/place/details/json?placeid=ChIJJalOaKRZwokRNLnbupB5UrY&key=AIzaSyCTRGzNhcI0CG6sZ-WBQDvd_aLfxiKrHXM
    @GET
    Call<PlaceResponse> getDetailsById(@Url String url);
}
