package com.oc.gofourlunch.view.fragmentRestaurantList;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.oc.gofourlunch.model.GooglePlaces.PlacesModel;
import com.oc.gofourlunch.R;
import com.oc.gofourlunch.controller.adapter.RestaurantListAdapter;

import java.util.List;

public class RestaurantListFragment extends Fragment {

    //--:: Data for settings ::--
    RecyclerView restaurantRecyclerView;
    RestaurantListAdapter adapter;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_restaurant_list, container, false);
        restaurantRecyclerView = (RecyclerView) view;
        this.configureRecyclerView();
        return view;
    }

    //-----------------------------
    // DISPLAYING RESTAURANT LIST
    //-----------------------------
    private void configureRecyclerView() {
        //--:: Set layout Manager to set the items in position ::--
        this.restaurantRecyclerView.setLayoutManager(new LinearLayoutManager(requireActivity()));
        this.restaurantRecyclerView.addItemDecoration(new DividerItemDecoration(requireActivity(),
                DividerItemDecoration.VERTICAL));
    }

    private void initList() {
        List<PlacesModel> restaurantPlaces = RestaurantListAdapter.restaurantList;
        //--:: Configuring adapter according to new list ::--
        this.adapter = new RestaurantListAdapter(restaurantPlaces);
        //--:: Attach the adapter to the recycler view to inflate items ::--
        this.restaurantRecyclerView.setAdapter(this.adapter);
    }

    @Override
    public void onResume() {
        initList();
        super.onResume();
    }
}