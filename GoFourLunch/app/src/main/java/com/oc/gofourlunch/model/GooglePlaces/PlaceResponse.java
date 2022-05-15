package com.oc.gofourlunch.model.GooglePlaces;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class PlaceResponse {

    @SerializedName("result")
    @Expose
    private PlaceDetailModel placeDetails;

    //--------
    // GETTER
    //--------
    public PlaceDetailModel getPlaceDetails() {
        return placeDetails;
    }
}
