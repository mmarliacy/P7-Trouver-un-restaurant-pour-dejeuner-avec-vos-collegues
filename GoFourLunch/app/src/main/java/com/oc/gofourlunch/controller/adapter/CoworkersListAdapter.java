package com.oc.gofourlunch.controller.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.oc.gofourlunch.model.GooglePlaces.PlacesModel;
import com.oc.gofourlunch.model.User.User;
import com.oc.gofourlunch.R;


import java.util.List;

public class CoworkersListAdapter extends BaseAdapter {

    //----------
    // FOR DATA
    //----------
    public Context fContext;
    public LayoutInflater fLayoutInflater;

    private final List<User> coworkersEatingList;
    private List<PlacesModel> placesList;


    public CoworkersListAdapter(Context context, List<User> users) {
        this.fContext = context;
        this.coworkersEatingList = users;
        this.fLayoutInflater = LayoutInflater.from(context);
    }


    @Override
    public int getCount() {
        if (coworkersEatingList != null) {
            return coworkersEatingList.size();
        } else {
            return 0;
        }
    }

    @Override
    public User getItem(int position) {
        return coworkersEatingList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @SuppressLint({"ViewHolder", "InflateParams", "SetTextI18n"})
    @NonNull
    @Override
    public View getView(int position, @Nullable View view, @NonNull ViewGroup parent) {
        view =  fLayoutInflater.inflate(R.layout.item_coworkers_list, null);

        User currentUser = coworkersEatingList.get(position);
        String name = currentUser.getName();
        String photoURL = currentUser.getPhotoURL();
        String restaurantType = currentUser.getRestaurantType();
        String restaurantName = currentUser.getRestaurantName();

        TextView nameTv = view.findViewById(R.id.coworker_is_eating);
        ImageView photoIv = view.findViewById(R.id.coworker_picture_item);
        if (restaurantName != null){
            nameTv.setText(name + fContext.getString(R.string.user_is_eating)  + restaurantType +" " +"(" + restaurantName + ")");
            Glide.with(fContext).load(photoURL).into(photoIv);
        } else {
            nameTv.setText(name + " " + fContext.getString(R.string.no_restaurant_choice_set));
            Glide.with(fContext).load(R.drawable.no_image_found).into(photoIv);
        }

        return view;
    }

}
