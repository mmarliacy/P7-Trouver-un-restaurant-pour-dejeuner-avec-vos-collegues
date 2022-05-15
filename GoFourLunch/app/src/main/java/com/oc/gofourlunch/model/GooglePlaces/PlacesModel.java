package com.oc.gofourlunch.model.GooglePlaces;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class PlacesModel implements Parcelable {

    @SerializedName("business_status")
    @Expose
    private String businessStatus;

    @SerializedName("geometry")
    @Expose
    private GeometryModel geometry;

    @SerializedName("icon")
    @Expose
    private String icon;

    @SerializedName("icon_background_color")
    @Expose
    private String iconBackgroundColor;

    @SerializedName("icon_mask_base_uri")
    @Expose
    private String iconMaskBaseUri;

    @SerializedName("name")
    @Expose
    private String name;

    @SerializedName("international_phone_number")
    @Expose
    private String phoneNumber;

    @SerializedName("opening_hours")
    @Expose
    private OpeningHoursModel openingHours;

    @SerializedName("photos")
    @Expose
    private List<PhotoModel> photos = null;

    @SerializedName("place_id")
    @Expose
    private String placeId;

    @SerializedName("price_level")
    @Expose
    private Integer priceLevel;

    @SerializedName("rating")
    @Expose
    private Double rating;

    @SerializedName("reference")
    @Expose
    private String reference;

    @SerializedName("scope")
    @Expose
    private String scope;

    @SerializedName("types")
    @Expose
    private List<String> types = null;

    @SerializedName("user_ratings_total")
    @Expose
    private Integer userRatingsTotal;

    @SerializedName("vicinity")
    @Expose
    private String vicinity;

    public PlacesModel() {

    }

    //-------------------
    // GETTERS & SETTERS
    //-------------------
    public GeometryModel getGeometry() {
        return geometry;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public OpeningHoursModel getOpeningHours() {
        return openingHours;
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

    public void setRating(Double rating) {
        this.rating = rating;
    }

    public List<String> getTypes() {
        return types;
    }

    public String getVicinity() {
        return vicinity;
    }

    //----------------------------
    // PARCELABLE IMPLEMENTATION
    //----------------------------
    public PlacesModel(Parcel in) {
        businessStatus = in.readString();
        icon = in.readString();
        iconBackgroundColor = in.readString();
        iconMaskBaseUri = in.readString();
        name = in.readString();
        placeId = in.readString();
        if (in.readByte() == 0) {
            priceLevel = null;
        } else {
            priceLevel = in.readInt();
        }
        if (in.readByte() == 0) {
            rating = null;
        } else {
            rating = in.readDouble();
        }
        reference = in.readString();
        scope = in.readString();
        types = in.createStringArrayList();
        if (in.readByte() == 0) {
            userRatingsTotal = null;
        } else {
            userRatingsTotal = in.readInt();
        }
        vicinity = in.readString();
    }

    public static final Creator<PlacesModel> CREATOR = new Creator<PlacesModel>() {
        @Override
        public PlacesModel createFromParcel(Parcel in) {
            return new PlacesModel(in);
        }

        @Override
        public PlacesModel[] newArray(int size) {
            return new PlacesModel[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(businessStatus);
        dest.writeString(icon);
        dest.writeString(iconBackgroundColor);
        dest.writeString(iconMaskBaseUri);
        dest.writeString(name);
        dest.writeString(placeId);
        if (priceLevel == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(priceLevel);
        }
        if (rating == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeDouble(rating);
        }
        dest.writeString(reference);
        dest.writeString(scope);
        dest.writeStringList(types);
        if (userRatingsTotal == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(userRatingsTotal);
        }
        dest.writeString(vicinity);
    }
}