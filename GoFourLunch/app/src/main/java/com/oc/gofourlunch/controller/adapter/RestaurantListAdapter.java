package com.oc.gofourlunch.controller.adapter;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.oc.gofourlunch.model.GooglePlaces.OpeningHoursModel;
import com.oc.gofourlunch.model.GooglePlaces.PlacesModel;
import com.oc.gofourlunch.model.User.User;
import com.oc.gofourlunch.R;
import com.oc.gofourlunch.view.activities.RestaurantActivity;
import com.oc.gofourlunch.view.fragmentMap.MapFragment;

import java.io.ByteArrayOutputStream;
import java.util.List;

public class RestaurantListAdapter extends RecyclerView.Adapter<RestaurantListAdapter.RestaurantViewHolder> {

    public static List<PlacesModel> restaurantList;
    int noImageFound = R.drawable.no_image_found;
    Bitmap photo;


    public RestaurantListAdapter(List<PlacesModel> restaurantList) {
        RestaurantListAdapter.restaurantList = restaurantList;
    }

    @NonNull
    @Override
    public RestaurantViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.item_restaurant_list, parent, false);
        return new RestaurantViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RestaurantViewHolder holder, int position) {
        PlacesModel itemRestaurant = restaurantList.get(position);
        OpeningHoursModel openings = itemRestaurant.getOpeningHours();
        if (itemRestaurant.getPhotos() != null) {
            photo = itemRestaurant.getPhotos().get(0).getImage();
            holder.getPhoto(photo);
        } else {
            holder.noPhoto(noImageFound);
        }
        holder.bind(itemRestaurant, openings);

        holder.itemView.setOnClickListener(view -> {
            Intent intent = new Intent(view.getContext(), RestaurantActivity.class);
            //Convert Bitmap to byte array
            photo = itemRestaurant.getPhotos().get(0).getImage();
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            photo.compress(Bitmap.CompressFormat.PNG, 100, stream);
            byte[] byteArray = stream.toByteArray();
            intent.putExtra("restaurant info", itemRestaurant);
            if (itemRestaurant.getPhotos() != null) {
                intent.putExtra("restaurant image", byteArray);
            } else {
                intent.putExtra("no image found", noImageFound);
            }
            PackageManager packageManager = view.getContext().getPackageManager();
            if (intent.resolveActivity(packageManager) != null) {
                view.getContext().startActivity(intent);
                Log.i("Transmission : ", itemRestaurant.getName() + " has been transferred by intent");
            } else {
                Toast.makeText(view.getContext(), R.string.none_happened, Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        if (restaurantList != null) {
            return restaurantList.size();
        } else {
            return 0;
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setGooglePlacesList(List<PlacesModel> googlePlacesList) {
        restaurantList = googlePlacesList;
        notifyDataSetChanged();
    }

    public static class RestaurantViewHolder extends RecyclerView.ViewHolder {

        //----------
        // FOR DATA
        //----------
        private static final int DEFAULT = 0;
        private static final String DEFAULT1 = "No data";
        FirebaseUser currentUser;
        static User user;
        int counted = 0;
        int moreCoworkers = 1;
        //-------------
        // FOR DESIGN
        //-------------
        TextView restaurantName;
        TextView restaurantType;
        TextView restaurantAddress;
        TextView restaurantOpenings;
        TextView distanceFromRestaurant;
        TextView countedCoworkers;
        RatingBar ratingBar;
        ImageView restaurantPicture;

        public RestaurantViewHolder(@NonNull View itemView) {
            super(itemView);
            restaurantName = itemView.findViewById(R.id.restaurant_name);
            restaurantType = itemView.findViewById(R.id.restaurant_type);
            restaurantAddress = itemView.findViewById(R.id.restaurant_address);
            restaurantOpenings = itemView.findViewById(R.id.restaurant_openings);
            distanceFromRestaurant = itemView.findViewById(R.id.distance_from_restaurant); //-::> Missing
            countedCoworkers = itemView.findViewById(R.id.counted_coworkers);
            ratingBar = itemView.findViewById(R.id.restaurant_rating_bar);
            restaurantPicture = itemView.findViewById(R.id.restaurant_picture);

        }

        public void bind(PlacesModel itemRestaurant, OpeningHoursModel openings) {
            restaurantName.setText(itemRestaurant.getName());
            restaurantAddress.setText(itemRestaurant.getVicinity());
            restaurantType.setText(itemRestaurant.getTypes().get(0));
            //--------------------------------------------------------
            //--:: Turn Rating Bar count double into Float ::--
            if (itemRestaurant.getRating() != null) {
                ratingBar.setNumStars(itemRestaurant.getRating().intValue());
            } else {
                ratingBar.setNumStars(DEFAULT);
            }

            addCoworkers(itemRestaurant.getPlaceId());
            getDistance(itemRestaurant);
            //--:: Set openings status ::--
            String openNow = "Opened";
            String closeNow = "Closed";
            if (openings != null) {
                if (openings.getOpenNow().equals(true)) {
                    restaurantOpenings.setText(openNow);
                } else {
                    restaurantOpenings.setText(closeNow);
                }
            } else {
                restaurantOpenings.setText(R.string.unavailable_opening_hours);
            }
        }

        public void getPhoto(Bitmap photo) {
            Glide.with(restaurantPicture).load(photo).into(restaurantPicture);
        }

        public void noPhoto(int noImageFound) {
            Glide.with(restaurantPicture).load(noImageFound).into(restaurantPicture);
        }

        @SuppressLint("SetTextI18n")
        public void addCoworkers(String placeId) {
            FirebaseFirestore database = FirebaseFirestore.getInstance();
            CollectionReference userBookReference = database.collection("Users");
            userBookReference.get().addOnSuccessListener(pQueryDocumentSnapshots -> {

                for (QueryDocumentSnapshot varQueryDocumentSnapshot : pQueryDocumentSnapshots) {
                    //-- :: Associate document with object POJO class :: --
                    user = varQueryDocumentSnapshot.toObject(User.class);
                    //-- :: Get connected current user :: --
                    currentUser = FirebaseAuth.getInstance().getCurrentUser();
                    //-- :: Define each user's attributes according to Firestore database  :: --
                    String restaurantId = user.getRestaurantId();
                    if (restaurantId != null){
                        if ((restaurantId).equals(placeId)) {
                            counted = +moreCoworkers;
                        }
                    }
                }
                countedCoworkers.setText("" + counted);
            });

        }

        @SuppressLint("SetTextI18n")
        public void getDistance(PlacesModel itemRestaurant) {
            double placeLatitude = itemRestaurant.getGeometry().getLocation().getLat();
            double placeLongitude = itemRestaurant.getGeometry().getLocation().getLng();
            float[] results = new float[15];
            Location.distanceBetween(MapFragment.latLngLatitude, MapFragment.latLngLongitude, placeLatitude, placeLongitude, results);
            float f = results[0];
            int distance = (int) f;
            distanceFromRestaurant.setText("" + distance +" m");

        }
    }


}
