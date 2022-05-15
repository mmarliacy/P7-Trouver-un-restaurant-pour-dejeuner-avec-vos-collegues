package com.oc.gofourlunch.controller.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.oc.gofourlunch.model.User.User;
import com.oc.gofourlunch.R;

import java.util.List;

public class BookingRestaurantAdapter extends BaseAdapter {

    Context fContext;
    List<User> registeredUserList;
    LayoutInflater fLayoutInflater;

    public BookingRestaurantAdapter(Context pContext, List<User> pRegisteredUserList) {
        this.fContext = pContext;
        this.registeredUserList = pRegisteredUserList;
        this.fLayoutInflater = LayoutInflater.from(pContext);
    }

    @Override
    public int getCount() {
        return registeredUserList.size();
    }

    @Override
    public User getItem(int position) {
        return registeredUserList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @SuppressLint({"ViewHolder", "InflateParams", "SetTextI18n"})
    @Override
    public View getView(int position, View view, ViewGroup parent) {
        view =  fLayoutInflater.inflate(R.layout.item_registered_workmates_restaurant, null);
        User currentUser = registeredUserList.get(position);
        String name = currentUser.getName();
        String photoURL = currentUser.getPhotoURL();

        TextView nameTv = view.findViewById(R.id.coworker_is_joining);
        ImageView photoIv = view.findViewById(R.id.coworker_picture_item);

        nameTv.setText(name + fContext.getString(R.string.user_is_joining));
        Glide.with(fContext).load(photoURL).into(photoIv);
        return view;
    }
}
