package com.oc.gofourlunch.model.User;

import androidx.annotation.NonNull;

public class User {

    private String name;
    private String mail;
    private String photoURL;
    private String restaurantId;
    private String restaurantType;
    private String restaurantName;

    //--------------
    // CONSTRUCTORS
    //--------------
    public User() {
    }

    public User(String name, String mail) {
        this.name = name;
        this.mail = mail;
    }

    public User(String name, String mail, String photoURL) {
        this.name = name;
        this.mail = mail;
        this.photoURL = photoURL;
    }

    public User(String name, String mail, String photoURL, String restaurantId, String restaurantType, String restaurantName) {
        this.name = name;
        this.mail = mail;
        this.photoURL = photoURL;
        this.restaurantId = restaurantId;
        this.restaurantType=restaurantType;
        this.restaurantName=restaurantName;
    }

    //-------------------
    // GETTERS & SETTERS
    //-------------------
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMail() {
        return mail;
    }

    public String getPhotoURL() {
        return photoURL;
    }

    public String getRestaurantId() {
        return restaurantId;
    }

    public void setRestaurantId(String pRestaurantId) {
        restaurantId = pRestaurantId;
    }

    public void setRestaurantType(String pRestaurantType) {
        restaurantType = pRestaurantType;
    }

    public void setRestaurantName(String pRestaurantName) {
        restaurantName = pRestaurantName;
    }

    public String getRestaurantType() {
        return restaurantType;
    }

    public String getRestaurantName() {
        return restaurantName;
    }

    @NonNull
    @Override
    public String toString() {
        return "User{" +
                "name='" + name + '\'' +
                '}';
    }
}
