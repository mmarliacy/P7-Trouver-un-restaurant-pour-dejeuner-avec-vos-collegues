package com.oc.gofourlunch.view.activities;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.gms.common.api.ApiException;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.PhotoMetadata;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPhotoRequest;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.oc.gofourlunch.model.GooglePlaces.PlaceDetailModel;
import com.oc.gofourlunch.model.GooglePlaces.PlaceResponse;
import com.oc.gofourlunch.model.User.User;
import com.oc.gofourlunch.R;
import com.oc.gofourlunch.view.fragmentMap.MapFragment;

import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class YourLunch extends AppCompatActivity {
    private static final String TAG = "";
    private String api_key;
    public User user;
    public FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
    public FirebaseFirestore database = FirebaseFirestore.getInstance();

    public String placeId;
    public PlaceDetailModel detailsPlace;
    //-------------
    // FOR DESIGN
    //-------------

    private ImageView restaurantPicture;


    private PlacesClient placesClient;
    private Bitmap bitmap;
    private PhotoMetadata placePhoto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_your_lunch);
        try {
            //----------
            // FOR DATA
            //----------
            ApplicationInfo varInfo_key = getApplicationContext().getPackageManager().getApplicationInfo(getApplicationContext().getPackageName(), PackageManager.GET_META_DATA);
            api_key = varInfo_key.metaData.getString("com.google.android.geo.API_KEY");
        } catch (PackageManager.NameNotFoundException pE) {
            pE.printStackTrace();
        }
        placesClient = Places.createClient(this);
            getPlaceIdAccordingToCurrentUser();
    }

    private void getPlaceIdAccordingToCurrentUser(){
        CollectionReference userBookReference = database.collection("Users");
        userBookReference.get().addOnSuccessListener(pQueryDocumentSnapshots -> {
            for (QueryDocumentSnapshot varQueryDocumentSnapshot : pQueryDocumentSnapshots) {
                //-- :: Associate document with object POJO class :: --
                user = varQueryDocumentSnapshot.toObject(User.class);
                if(user.getName().equals(currentUser.getDisplayName())){
                    placeId = user.getRestaurantId();
                    if (placeId != null){
                        getRestaurantDetailsAccordingToId(placeId);
                    }else {
                        Toast.makeText(YourLunch.this, R.string.Choose_a_restaurant_first, Toast.LENGTH_LONG).show();
                    }


                }

            }
        });
    }


    private void getRestaurantDetailsAccordingToId(String pPlaceId){
        String url = "https://maps.googleapis.com/maps/api/place/details/json?"
                + "placeid="
                + pPlaceId + "&key=" + api_key;

        MapFragment.retrofitService.getDetailsById(url).enqueue(new Callback<PlaceResponse>() {
            @Override
            public void onResponse(@NonNull Call<PlaceResponse> call, @NonNull Response<PlaceResponse> response) {
                if (response.isSuccessful()) {
                    if (response.body() != null) {
                        detailsPlace = response.body().getPlaceDetails();
                        fetchViewsToData(detailsPlace);
                        configureButtons(detailsPlace);
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<PlaceResponse> call, @NonNull Throwable t) {
                Log.i("Error", t.getMessage());
            }
        });
    }

    public void fetchViewsToData(PlaceDetailModel placeDetail){
        restaurantPicture = findViewById(R.id.restaurant_status_picture);
        RatingBar varRestaurantRate = findViewById(R.id.restaurant_form_rating_bar);
        TextView varRestaurantName = findViewById(R.id.restaurant_status_name);
        TextView varRestaurantType = findViewById(R.id.restaurant_status_type);
        TextView varRestaurantAddress = findViewById(R.id.restaurant_status_address);

        varRestaurantName.setText(placeDetail.getName());
        varRestaurantType.setText(placeDetail.getTypes().get(0));
        varRestaurantRate.setRating(placeDetail.getRating().floatValue());
        varRestaurantAddress.setText(placeDetail.getFormattedAddress());
        if (placeDetail.getPhotos() != null) {
            fetchPlaceToImage(placeDetail);
        } else {
            Glide.with(this).load(R.drawable.no_image_found).into(restaurantPicture);
        }

    }
     private void configureButtons(PlaceDetailModel place){
         FloatingActionButton varCallBtn = findViewById(R.id.phone_button);
         FloatingActionButton varWebsiteBtn = findViewById(R.id.website_button);

         //--:: 1 -- Configure call button.
         varCallBtn.setOnClickListener(v -> {
             if (place.getInternationalPhoneNumber() != null){
                 String phoneNumber = place.getInternationalPhoneNumber();
                 Intent intent = new Intent(Intent.ACTION_DIAL);
                 intent.setData(Uri.parse("tel:" + phoneNumber));
                 startActivity(intent);
             } else {
                 Toast.makeText(this, "No phone number to dial", Toast.LENGTH_SHORT).show();
             }

         });

         //--:: 2 -- Configure website button.
         varWebsiteBtn.setOnClickListener(websiteBtn -> {
         if (place.getWebsite() != null){
             String website = place.getWebsite();
                 Uri uri = Uri.parse(website);
                 startActivity(new Intent(Intent.ACTION_VIEW, uri));

         } else {
             Toast.makeText(this, "No website attached", Toast.LENGTH_SHORT).show();
         }
         });

     }

    private void fetchPlaceToImage(PlaceDetailModel pPlace) {
        String placeId = pPlace.getPlaceId();
        // Specify fields. Requests for photos must always have the PHOTO_METADATA field.
        final List<Place.Field> fields = Collections.singletonList(Place.Field.PHOTO_METADATAS);

        // Get a Place object (this example uses fetchPlace(), but you can also use findCurrentPlace())
        assert placeId != null;
        final FetchPlaceRequest placeRequest = FetchPlaceRequest.newInstance(placeId, fields);

        placesClient.fetchPlace(placeRequest).addOnSuccessListener((response) -> {
            final Place placeFound = response.getPlace();

            // Get the photo metadata.
            final List<PhotoMetadata> metadata = placeFound.getPhotoMetadatas();
            if (metadata == null || metadata.isEmpty()) {
                Log.w(TAG, "No photo metadata.");
                return;
            }
            placePhoto = metadata.get(0);

            // Create a FetchPhotoRequest.
            final FetchPhotoRequest photoRequest = FetchPhotoRequest.builder(placePhoto)
                    .setMaxWidth(500) // Optional.
                    .setMaxHeight(300) // Optional.
                    .build();
            placesClient.fetchPhoto(photoRequest).addOnSuccessListener((fetchPhotoResponse) -> {
                bitmap = fetchPhotoResponse.getBitmap();
                Glide.with(this).load(bitmap)
                        .placeholder(R.drawable.restaurant_picture)
                        .into(restaurantPicture);

            }).addOnFailureListener((exception) -> {
                if (exception instanceof ApiException) {
                    Log.e(TAG, "Place not found: " + exception.getMessage());
                    // TODO: Handle error with given status code.
                }
            });
        });
    }


}