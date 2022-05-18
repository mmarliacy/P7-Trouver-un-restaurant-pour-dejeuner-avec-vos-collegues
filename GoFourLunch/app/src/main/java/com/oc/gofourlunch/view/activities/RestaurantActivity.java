package com.oc.gofourlunch.view.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
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
import com.google.firebase.firestore.SetOptions;
import com.oc.gofourlunch.model.GooglePlaces.PlaceDetailModel;
import com.oc.gofourlunch.model.GooglePlaces.PlaceResponse;
import com.oc.gofourlunch.model.GooglePlaces.PlacesModel;
import com.oc.gofourlunch.model.User.User;
import com.oc.gofourlunch.R;
import com.oc.gofourlunch.controller.adapter.BookingRestaurantAdapter;
import com.oc.gofourlunch.view.fragmentMap.MapFragment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RestaurantActivity extends AppCompatActivity {

    //-------------
    // FOR DESIGN
    //-------------
    private ImageView restaurantPicture;
    private RatingBar restaurantRating;
    private TextView restaurantName;
    private TextView restaurantType;
    private TextView restaurantAddress;
    private Button callBtn;
    private Button likeBtn;
    private Button websiteBtn;
    private FloatingActionButton choiceBtn;

    //----------
    // FOR DATA
    //----------
    private static final String TAG = Activity.class.getName();
    private String api_key;
    private static final String DEFAULT = "No data";
    private PlacesClient placesClient;
    private Bitmap bitmap;
    @SuppressLint("UseCompatLoadingForDrawables")
    private Drawable noImageFound;
    private PhotoMetadata placePhoto;
    private User user;
    public static List<User> usersById = new ArrayList<>();
    private final Set<String> usersName = new HashSet<>();
    private FirebaseFirestore database;
    static PlaceDetailModel detailsPlace;

    public String name;
    public String photo;
    public String restaurantId;
    public String mail;

    //-- ::> Save user's choice about restaurant, like button, and register list of users
    public static SharedPreferences lunchBtnSharesPref;
    public SharedPreferences likeBtnSharesPref;
    public static SharedPreferences subscribedUsersPref;
    SharedPreferences.Editor fEditor;

    //---------------------------------
    // ON-CREATE : RESTAURANT ACTIVITY
    //---------------------------------
    @SuppressLint("UseCompatLoadingForDrawables")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurant);
        try {
            ApplicationInfo varInfo_key = getApplicationContext().getPackageManager().getApplicationInfo(getApplicationContext().getPackageName(), PackageManager.GET_META_DATA);
            api_key = varInfo_key.metaData.getString("com.google.android.geo.API_KEY");
        } catch (PackageManager.NameNotFoundException pE) {
            pE.printStackTrace();
        }
        noImageFound = getResources().getDrawable(R.drawable.no_image_found);
        //-- ::> Instantiation of PlaceClient.
        placesClient = Places.createClient(this);
        //-- ::> Declare views
        declareViews();
        //-- ::> Set data.
        setPlaceData();
    }

    //-----------
    // GET VIEWS
    //-----------
    private void declareViews() {
        restaurantName = findViewById(R.id.restaurant_form_name);
        restaurantType = findViewById(R.id.restaurant_form_type);
        restaurantAddress = findViewById(R.id.restaurant_form_address);
        restaurantRating = findViewById(R.id.restaurant_form_rating_bar);
        restaurantPicture = findViewById(R.id.restaurant_form_picture);
        choiceBtn = findViewById(R.id.choiceBtn);
    }

    //---------------------------------------------
    // GET PLACE DATA BY PARCELABLE IMPLEMENTATION
    // (RESTAURANT/ANY PLACE)
    //---------------------------------------------
    private void setPlaceData() {
        if (getIntent().hasExtra("restaurant info")) {
            //-- ::> Get extras data
            PlacesModel pRestaurantModel = getIntent().getParcelableExtra("restaurant info");
            //-- ::> Bound views
            boundViews(pRestaurantModel);
            //-- ::> By Shares Pref store in phone, get user's choice about restaurant
            getLunchChoice(pRestaurantModel.getPlaceId(), pRestaurantModel.getName());
            //-- ::> Set image, if it has been sent by intent
            if (getIntent().hasExtra("restaurant image")) {
                byte[] imageByte = getIntent().getByteArrayExtra("restaurant image");
                //-- :: Decode transform byteArray type to Bitmap :: --
                Bitmap bitmap = BitmapFactory.decodeByteArray(imageByte, 0, imageByte.length);
                Glide.with(this).load(bitmap).into(restaurantPicture);
            } else if (getIntent().hasExtra("no image found")) {
                Glide.with(this).load(noImageFound).into(restaurantPicture);
            }
            //-- ::> Handle click button, and save the choice in Shared Preferences file
            whenUserClickedOnChoiceBtn( pRestaurantModel.getPlaceId(),
                                        pRestaurantModel.getName(),
                                        pRestaurantModel.getVicinity(),
                                        pRestaurantModel.getTypes().get(0));
            //-- ::> Go on Firestore and get all users that have same place id as the selected one
            getListOfSubscribedUsers(pRestaurantModel.getPlaceId());
            //-- ::> Configure buttons on Restaurant Activity, Call, Like, Website
            configureButtons(pRestaurantModel.getPlaceId());
            //-- ::> By Shares Pref store in phone, get user's choice about like button
            retrieveLikeBtn(pRestaurantModel.getPlaceId());
        } else if (getIntent().hasExtra("place info")) {
            Place pPlace = getIntent().getParcelableExtra("place info");
            boundPlaceViews(pPlace);
            if (getIntent().hasExtra("place image")) {
                fetchPlaceToImage(pPlace);
            } else {
                Glide.with(this).load(noImageFound).into(restaurantPicture);
            }
            //-- ::> Handle click button, and save the choice in Shared Preferences file
            whenUserClickedOnChoiceBtn(pPlace.getId(), pPlace.getName(), pPlace.getAddress(), Objects.requireNonNull(pPlace.getTypes()).get(0).toString());
            //-- ::> Go on Firestore and get all users that have same place id as the selected one
            getListOfSubscribedUsers(pPlace.getId());
            //-- ::> Configure buttons on Restaurant Activity, Call, Like, Website
            configureButtons(pPlace.getId());
            //-- ::> By Shares Pref store in phone, get user's choice about like button
            retrieveLikeBtn(pPlace.getId());
        }
    }


    //------------------------
    // BOUND VIEWS
    // (RESTAURANT/ANY PLACE)
    //------------------------
    private void boundViews(PlacesModel placesModel) {
        restaurantName.setText(placesModel.getName());
        restaurantType.setText(placesModel.getTypes().get(0));
        restaurantAddress.setText(placesModel.getVicinity());
        if (placesModel.getRating() != null) {
            restaurantRating.setNumStars(placesModel.getRating().intValue());
        } else {
            restaurantRating.setNumStars(0);
        }
    }

    private void boundPlaceViews(Place place) {
        restaurantName.setText(place.getName());
        restaurantType.setText(Objects.requireNonNull(place.getTypes()).get(0).toString());
        restaurantAddress.setText(place.getAddress());
        if (place.getRating() != null) {
            restaurantRating.setNumStars(place.getRating().intValue());
        } else {
            restaurantRating.setNumStars(0);
        }
    }

    private void fetchPlaceToImage(Place place) {
        String placeId = place.getId();
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
                Glide.with(this).load(bitmap).into(restaurantPicture);

            }).addOnFailureListener((exception) -> {
                if (exception instanceof ApiException) {
                    Log.e(TAG, "Place not found: " + exception.getMessage());
                    // TODO: Handle error with given status code.
                }
            });
        });
    }

    //--------------------------------
    // USER CHOOSE AND SAVE RESTAURANT
    //--------------------------------
    private void whenUserClickedOnChoiceBtn(String placeId, String name, String address, String placeType) {
        choiceBtn.setOnClickListener(v -> {
            if (lunchBtnSharesPref.getString("id", DEFAULT).equals(DEFAULT)) {
                saveUserChoice(placeId, name, address, placeType);
                choiceBtn.setColorFilter(getResources().getColor(R.color.green_light));
            } else if (lunchBtnSharesPref.getString("id", DEFAULT).equals(placeId)) {
                choiceBtn.setColorFilter(getResources().getColor(R.color.green_light));
                Toast.makeText(this, "you choose " + name, Toast.LENGTH_LONG).show();
            } else if (!placeId.equals(lunchBtnSharesPref.getString("id", DEFAULT))) {
                saveUserChoice(placeId, name, address, placeType);
                choiceBtn.setColorFilter(getResources().getColor(R.color.red));
                Toast.makeText(this, "you already choose " + lunchBtnSharesPref.getString("name", DEFAULT), Toast.LENGTH_LONG).show();
            }
        });
    }

    //-- :: Save user's choice by using Shared Preferences :: --
    private void saveUserChoice(String placeId, String placeName, String placeAddress, String placeType) {
        lunchBtnSharesPref = getSharedPreferences("lunchPref", MODE_PRIVATE);
        fEditor = lunchBtnSharesPref.edit();
        fEditor.putBoolean(placeId, true);
        fEditor.putString("id", placeId);
        fEditor.putString("name", placeName);
        fEditor.putString("address", placeAddress);
        fEditor.commit();
        fetchRestaurantWithCurrentUserForFirebase(placeId, placeName, placeType);

    }

    private void saveUsersList() {
        //Set the values
        subscribedUsersPref = getSharedPreferences("usersList", MODE_PRIVATE);
        SharedPreferences.Editor editor = subscribedUsersPref.edit();
        editor.putStringSet("list", usersName);
        editor.apply();
    }


    //-- :: Get info store in Shared Preferences :: --
    private void getLunchChoice(String placeId, String placeName) {
        lunchBtnSharesPref = getSharedPreferences("lunchPref", MODE_PRIVATE);
        boolean buttonState = lunchBtnSharesPref.getBoolean(placeId, true);
        if (buttonState) {
            if (placeId.equals(lunchBtnSharesPref.getString("id", DEFAULT))) {
                choiceBtn.setColorFilter(getResources().getColor(R.color.green_light));
                Toast.makeText(this, "You choose " + placeName, Toast.LENGTH_LONG).show();
            }
        } else {
            choiceBtn.setColorFilter(getResources().getColor(R.color.black));
        }
    }

    //-- :: Get an instance of Firestore to fetch user's choice :: --
    private void fetchRestaurantWithCurrentUserForFirebase(String pPlaceId, String placeName, String placeType ) {
        database = FirebaseFirestore.getInstance();
        CollectionReference userBookReference = database.collection("Users");
        userBookReference.get().addOnSuccessListener(pQueryDocumentSnapshots -> {
            for (QueryDocumentSnapshot varQueryDocumentSnapshot : pQueryDocumentSnapshots) {
                //-- :: Associate document with object POJO class :: --
                user = varQueryDocumentSnapshot.toObject(User.class);
                //-- :: Get connected current user :: --
                FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                //-- :: Define each user's attributes according to Firestore database  :: --
                name = user.getName();
                mail = user.getMail();
                photo = user.getPhotoURL();
                restaurantId = user.getRestaurantId();
                if (lunchBtnSharesPref.getString("id", DEFAULT).equals(pPlaceId)) {
                    assert currentUser != null;
                    if (user.getName().equals(currentUser.getDisplayName())) {
                        user.setRestaurantId(pPlaceId);
                        user.setRestaurantName(placeName);
                        user.setRestaurantType(placeType);
                        userBookReference.document(user.getName()).set(user, SetOptions.merge());
                    }
                }

            }
        });
    }

    //-- :: Get an instance of Firestore to fetch user's choice :: --
    private void getListOfSubscribedUsers(String pPlaceId) {
        database = FirebaseFirestore.getInstance();
        CollectionReference userBookReference = database.collection("Users");
        userBookReference.get().addOnSuccessListener(pQueryDocumentSnapshots -> {
            for (QueryDocumentSnapshot varQueryDocumentSnapshot : pQueryDocumentSnapshots) {
                //-- :: Associate document with object POJO class :: --
                user = varQueryDocumentSnapshot.toObject(User.class);
                restaurantId = user.getRestaurantId();
                //-- :: Get connected current user :: --
                FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

                if (currentUser != null) {
                    if (user.getRestaurantId() != null && user.getRestaurantId().equals(pPlaceId) && !user.getName().equals(currentUser.getDisplayName())) {
                        usersById.add(user);
                    }
                }
                ListView userListBooking = findViewById(R.id.registered_workmates_list);
                BookingRestaurantAdapter adapter = new BookingRestaurantAdapter(RestaurantActivity.this, usersById);
                userListBooking.setAdapter(adapter);
                adapter.notifyDataSetChanged();
            }
        });
        for (int i = 0; i < usersById.size(); i++) {
            usersName.add(usersById.get(i).getName());
        }
        saveUsersList();
    }

    private void configureButtons(String pPlaceId) {
        //--:: 1 -- Set design buttons.
        callBtn = findViewById(R.id.call_btn);
        likeBtn = findViewById(R.id.like_btn);
        websiteBtn = findViewById(R.id.website_btn);

        //--:: 2 -- Get place details by retrofit request .
        String url = "https://maps.googleapis.com/maps/api/place/details/json?"
                + "placeid="
                + pPlaceId + "&key=" + api_key;

        MapFragment.retrofitService.getDetailsById(url).enqueue(new Callback<PlaceResponse>() {
            @Override
            public void onResponse(@NonNull Call<PlaceResponse> call, @NonNull Response<PlaceResponse> response) {
                if (response.isSuccessful()) {
                    if (response.body() != null) {
                        detailsPlace = response.body().getPlaceDetails();

                        //--:: 3 -- Configure call button.
                        callBtn.setOnClickListener(v -> {
                            if (detailsPlace.getInternationalPhoneNumber() != null) {
                                String phoneNumber = detailsPlace.getInternationalPhoneNumber();
                                Intent intent = new Intent(Intent.ACTION_DIAL);
                                intent.setData(Uri.parse("tel:" + phoneNumber));
                                startActivity(intent);
                            } else {
                                Toast.makeText(RestaurantActivity.this, "No phone number to dial", Toast.LENGTH_SHORT).show();
                            }

                        });
                        //--:: 4 -- Configure website button.
                        websiteBtn.setOnClickListener(websiteBtn -> {
                            if (detailsPlace.getWebsite() != null) {
                                String website = detailsPlace.getWebsite();
                                Uri uri = Uri.parse(website);
                                startActivity(new Intent(Intent.ACTION_VIEW, uri));
                            } else {
                                Toast.makeText(RestaurantActivity.this, "No website attached", Toast.LENGTH_SHORT).show();
                            }

                        });
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<PlaceResponse> call, @NonNull Throwable t) {
                Log.i("Error", t.getMessage());
            }
        });

        //--:: 5 -- Configure like button.
        likeBtn.setOnClickListener(likeBtn -> {
            saveLikeBtn(detailsPlace.getPlaceId());
            likeBtn.setBackgroundColor(getResources().getColor(R.color.clear_orange_peach));
        });

    }

    //-- :: Save like btn state by using Shared Preferences :: --
    private void saveLikeBtn(String placeId) {
        likeBtnSharesPref = getSharedPreferences("btnState", MODE_PRIVATE);
        fEditor = likeBtnSharesPref.edit();
        fEditor.putBoolean("likeBtnState", true);
        fEditor.putString("id", placeId);
        fEditor.commit();
    }

    //-- :: Get state of like button store in Shared Preferences :: --
    private void retrieveLikeBtn(String placeId) {
        likeBtnSharesPref = getSharedPreferences("btnState", MODE_PRIVATE);
        boolean buttonState = likeBtnSharesPref.getBoolean("likeBtnState", false);
        if (buttonState) {
            if (placeId.equals(likeBtnSharesPref.getString("id", DEFAULT))) {
                likeBtn.setBackgroundColor(getResources().getColor(R.color.clear_orange_peach));
            }
        }
    }


}



