package com.oc.gofourlunch.model.GooglePlaces;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class GooglePlacesModel {

    @SerializedName("results")
    @Expose
    private List<PlacesModel> googlePlaceList;

    //--------
    // GETTER
    //--------
    public List<PlacesModel> getGooglePlaceList() {
        return googlePlaceList;
    }
}
