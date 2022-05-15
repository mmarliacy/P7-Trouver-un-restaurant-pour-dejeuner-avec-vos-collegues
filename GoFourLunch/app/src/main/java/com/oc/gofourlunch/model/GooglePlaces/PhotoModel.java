package com.oc.gofourlunch.model.GooglePlaces;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class PhotoModel implements Parcelable {

    @SerializedName("height")
    @Expose
    private Integer height;

    @SerializedName("html_attributions")
    @Expose
    private final List<String> htmlAttributions;

    @SerializedName("photo_reference")
    @Expose
    private final String photoReference;

    @SerializedName("width")
    @Expose
    private Integer width;

    @SerializedName("photo")
    @Expose
    private Bitmap image;



    //-------------------
    // GETTERS & SETTERS
    //-------------------
    public Integer getHeight() {
        return height;
    }

    public void setHeight(Integer height) {
        this.height = height;
    }

    public Integer getWidth() {
        return width;
    }

    public void setWidth(Integer width) {
        this.width = width;
    }

    public Bitmap getImage() {
        return image;
    }

    public void setImage(Bitmap image) {
        this.image = image;
    }



    //---------------------------
    // PARCELABLE IMPLEMENTATION
    //---------------------------

    protected PhotoModel(Parcel in) {
        if (in.readByte() == 0) {
            height = null;
        } else {
            height = in.readInt();
        }
        htmlAttributions = in.createStringArrayList();
        photoReference = in.readString();
        if (in.readByte() == 0) {
            width = null;
        } else {
            width = in.readInt();
        }
        image = in.readParcelable(Bitmap.class.getClassLoader());
    }

    public static final Creator<PhotoModel> CREATOR = new Creator<PhotoModel>() {
        @Override
        public PhotoModel createFromParcel(Parcel in) {
            return new PhotoModel(in);
        }

        @Override
        public PhotoModel[] newArray(int size) {
            return new PhotoModel[size];
        }
    };
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        if (height == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(height);
        }
        dest.writeStringList(htmlAttributions);
        dest.writeString(photoReference);
        if (width == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(width);
        }
        dest.writeParcelable(image, flags);
    }
}
