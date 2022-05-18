package com.oc.gofourlunch.view.fragmentRestaurantList;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.libraries.places.api.model.PhotoMetadata;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.oc.gofourlunch.R;
import com.oc.gofourlunch.controller.adapter.RestaurantListAdapter;
import com.oc.gofourlunch.model.GooglePlaces.PlacesModel;
import com.oc.gofourlunch.view.activities.RestaurantActivity;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class RestaurantListFragment extends Fragment {

    //--:: Data for settings ::--
    RecyclerView restaurantRecyclerView;
    RestaurantListAdapter adapter;

    //--:: Autocomplete Places ::--
    private static final int AUTOCOMPLETE_REQUEST_CODE = 1;
    Toolbar toolbar;
    Place place;
    private final String TAG = "Alert";


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_restaurant_list, container, false);
        restaurantRecyclerView = (RecyclerView) view;
        this.configureRecyclerView();
        setHasOptionsMenu(true);
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

    //-----------------------------------
    // AUTOCOMPLETE : GETTING PREDICTIONS
    //-----------------------------------

    //--:: 1 -- Create search icon by implemented a menu ::-->
    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        requireActivity().getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        menu.findItem(R.id.search).setVisible(true);
        super.onCreateOptionsMenu(menu, inflater);
    }

    //--:: 2 -- Connect search icon to "getting places procedure" ::-->
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int search_id = R.id.search;
        if (item.getItemId() == search_id) {
            SearchBarViewListener();
        }
        return super.onOptionsItemSelected(item);
    }

    //--:: 3 -- Handling click on search menu item and initialize intent for getting predictions ::-->
    private void SearchBarViewListener() {
        //--:: Set the fields to specify which types of place data to return after the user has made a selection ::--
        List<Place.Field> fields =
                Arrays.asList(
                        Place.Field.ID,
                        Place.Field.NAME,
                        Place.Field.LAT_LNG,
                        Place.Field.ADDRESS,
                        Place.Field.TYPES,
                        Place.Field.PHOTO_METADATAS,
                        Place.Field.RATING);
        //--:: Start the autocomplete intent ::--
        Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fields).build(requireActivity());
        startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE);
    }

    //--:: 4 -- Get place from intent, and define a marker to locate the place position ::-->
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                assert data != null;
                place = Autocomplete.getPlaceFromIntent(data);
                PhotoMetadata placePhoto = place.getPhotoMetadatas().get(0);
                Log.i(TAG, "Place: " + place.getName() + ", " + place.getAddress());
                Intent intent = new Intent(requireActivity(), RestaurantActivity.class);
                intent.putExtra("place info", place);
                intent.putExtra("place image", placePhoto);
                startActivity(intent);
            } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                // TODO: Handle the error.
                assert data != null;
                Status status = Autocomplete.getStatusFromIntent(data);
                Log.i(TAG, status.getStatusMessage());
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(requireActivity(), "You cancelled the research", Toast.LENGTH_SHORT).show();
            }
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    @Override
    public void onResume() {
        initList();
        super.onResume();
    }
}