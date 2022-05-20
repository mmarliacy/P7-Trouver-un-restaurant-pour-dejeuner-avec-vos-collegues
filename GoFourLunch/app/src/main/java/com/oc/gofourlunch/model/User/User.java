package com.oc.gofourlunch.model.User;

import androidx.annotation.NonNull;

import java.util.List;

public class User {


    private String name;
    private String mail;
    private String photoURL;
    private String restaurantId;
    private String restaurantType;
    private String restaurantName;
    private boolean likeRestaurant;
    private String restaurantAddress;
    private String restaurantLiked;
    private List<String> subscribedCoworkers;

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

    public User(String name, String mail, String photoURL, String restaurantId, String restaurantType, String restaurantName, String restaurantAddress) {
        this.name = name;
        this.mail = mail;
        this.photoURL = photoURL;
        this.restaurantId = restaurantId;
        this.restaurantType=restaurantType;
        this.restaurantName=restaurantName;
        this.restaurantAddress = restaurantAddress;
    }

    public User(String name,
                String mail,
                String photoURL,
                String restaurantId,
                String restaurantType,
                String restaurantName,
                boolean likeRestaurant,
                String restaurantLiked,
                List<String> subscribedCoworkers) {
        this.name = name;
        this.mail = mail;
        this.photoURL = photoURL;
        this.restaurantId = restaurantId;
        this.restaurantType=restaurantType;
        this.restaurantName=restaurantName;
        this.likeRestaurant = likeRestaurant;
        this.restaurantLiked = restaurantLiked;
        this.subscribedCoworkers = subscribedCoworkers;
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

    public String getRestaurantAddress() {
        return restaurantAddress;
    }

    public void setRestaurantAddress(String pRestaurantAddress) {
        restaurantAddress = pRestaurantAddress;
    }

    public boolean isLikeRestaurant() {
        return likeRestaurant;
    }

    public void setLikeRestaurant(boolean pLikeRestaurant) {
        likeRestaurant = pLikeRestaurant;
    }

    public String getRestaurantLiked() {
        return restaurantLiked;
    }

    public void setRestaurantLiked(String pRestaurantLiked) {
        restaurantLiked = pRestaurantLiked;
    }

    public List<String> getSubscribedCoworkers() {
        return subscribedCoworkers;
    }

    public void setSubscribedCoworkers(List<String> pSubscribedCoworkers) {
        subscribedCoworkers = pSubscribedCoworkers;
    }

    @NonNull
    @Override
    public String toString() {
        return "User{" +
                "name='" + name + '\'' +
                '}';
    }
}
