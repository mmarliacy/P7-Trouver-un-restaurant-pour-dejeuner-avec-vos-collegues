package com.oc.gofourlunch.model.GooglePlaces;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class LocationModel {

    @SerializedName("lat")
    @Expose
    private Double lat;

    @SerializedName("lng")
    @Expose
    private Double lng;

    //---------
    // GETTERS
    //---------
    public Double getLat() {
        return lat;
    }

    public Double getLng() {
        return lng;
    }
}
