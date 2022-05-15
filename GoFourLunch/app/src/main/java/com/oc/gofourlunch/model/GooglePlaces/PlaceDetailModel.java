package com.oc.gofourlunch.model.GooglePlaces;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class PlaceDetailModel {

    @SerializedName("formatted_address")
    @Expose
    public String formattedAddress;

    @SerializedName("international_phone_number")
    @Expose
    public String internationalPhoneNumber;

    @SerializedName("name")
    @Expose
    public String name;

    @SerializedName("photos")
    @Expose
    public List<PhotoModel> photos = null;

    @SerializedName("opening_hours")
    @Expose
    public OpeningHoursModel openingHours;

    @SerializedName("weekday_text")
    @Expose
    List<String> getWeekdayText;

    @SerializedName("place_id")
    @Expose
    public String placeId;

    @SerializedName("rating")
    @Expose
    public Double rating;

    @SerializedName("types")
    @Expose
    public List<String> types = null;

    @SerializedName("website")
    @Expose
    public String website;

    //-------------
    // CONSTRUCTOR
    //-------------
    public PlaceDetailModel() {
    }

    //-------------------
    // GETTERS & SETTERS
    //-------------------
    public String getFormattedAddress() {
        return formattedAddress;
    }

    public String getInternationalPhoneNumber() {
        return internationalPhoneNumber;
    }

    public String getName() {
        return name;
    }

    public void setName(String pName) {
        name = pName;
    }

    public List<PhotoModel> getPhotos() {
        return photos;
    }

    public String getPlaceId() {
        return placeId;
    }

    public Double getRating() {
        return rating;
    }

    public void setRating(Double pRating) {
        rating = pRating;
    }

    public List<String> getTypes() {
        return types;
    }

    public String getWebsite() {
        return website;
    }
}