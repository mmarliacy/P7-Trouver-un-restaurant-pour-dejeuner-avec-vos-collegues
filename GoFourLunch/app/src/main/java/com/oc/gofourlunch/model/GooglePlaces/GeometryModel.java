package com.oc.gofourlunch.model.GooglePlaces;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class GeometryModel {

    @SerializedName("location")
    @Expose
    private LocationModel location;

    //--------
    // GETTER
    //--------
    public LocationModel getLocation() {
        return location;
    }
}
