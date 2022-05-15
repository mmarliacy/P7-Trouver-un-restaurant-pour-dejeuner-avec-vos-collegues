package com.oc.gofourlunch.model.GooglePlaces;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class OpeningHoursModel {

    @SerializedName("open_now")
    @Expose
    private Boolean openNow;

    @SerializedName("weekday_text")
    @Expose
    List<String> getWeekdayText;

    public OpeningHoursModel(Boolean pOpenNow, List<String> pGetWeekdayText) {
        openNow = pOpenNow;
        getWeekdayText = pGetWeekdayText;
    }

    //--------
    // GETTER
    //--------
    public Boolean getOpenNow() {
        return openNow;
    }

    public List<String> getGetWeekdayText() {
        return getWeekdayText;
    }
}
