package com.oc.gofourlunch.view.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
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
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.SetOptions;
import com.oc.gofourlunch.R;
import com.oc.gofourlunch.controller.adapter.BookingRestaurantAdapter;
import com.oc.gofourlunch.model.GooglePlaces.PlaceDetailModel;
import com.oc.gofourlunch.model.GooglePlaces.PlaceResponse;
import com.oc.gofourlunch.model.GooglePlaces.PlacesModel;
import com.oc.gofourlunch.model.User.User;
import com.oc.gofourlunch.view.fragmentMap.MapFragment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

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
    private PlacesClient placesClient;
    private Bitmap bitmap;
    @SuppressLint("UseCompatLoadingForDrawables")
    private Drawable noImageFound;
    private PhotoMetadata placePhoto;
    private User user;
    public List<User> usersById = new ArrayList<>();
    private FirebaseFirestore database;
    static PlaceDetailModel detailsPlace;

    public String name;
    public String photo;
    public String restaurantId;
    public String placeName;
    public String mail;
    public List<String> restaurantLikedId;

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
            //--::Get extras data ::--
            PlacesModel pRestaurantModel = getIntent().getParcelableExtra("restaurant info");
            //--:: Bound views ::--
            boundViews(pRestaurantModel);
            //--:: By getting Firestore data, get user's choice about restaurant ::--
            getLunchChoice(pRestaurantModel.getPlaceId());

            //--:: Set image, if it has been sent by intent ::--
            if (getIntent().hasExtra("restaurant image")) {
                byte[] imageByte = getIntent().getByteArrayExtra("restaurant image");
                //-- :: Decode transform byteArray type to Bitmap :: --
                Bitmap bitmap = BitmapFactory.decodeByteArray(imageByte, 0, imageByte.length);
                Glide.with(this).load(bitmap).into(restaurantPicture);
            } else if (getIntent().hasExtra("no image found")) {
                Glide.with(this).load(noImageFound).into(restaurantPicture);
            }
            //--:: Go on Firestore and get all users that have same place id as the selected one ::--
            getListOfSubscribedUsers(pRestaurantModel.getPlaceId());
            //--:: Handle click button, and save the choice in on Firestore database ::--
            whenUserClickedOnChoiceBtn(pRestaurantModel.getPlaceId(),
                    pRestaurantModel.getName(), pRestaurantModel.getVicinity(),
                    pRestaurantModel.getTypes().get(0));

            //--:: Configure buttons on Restaurant Activity, Call,& Website ::--
            configureButtons(pRestaurantModel.getPlaceId());
            //--:: Get like button state on Firestore database ::--
            configureLikeButton(pRestaurantModel.getPlaceId());
            retrieveLikeBtn(pRestaurantModel.getPlaceId());

        } else if (getIntent().hasExtra("place info")) {
            Place pPlace = getIntent().getParcelableExtra("place info");
            boundPlaceViews(pPlace);
            if (getIntent().hasExtra("place image")) {
                fetchPlaceToImage(pPlace);
            } else {
                Glide.with(this).load(noImageFound).into(restaurantPicture);
            }
            //--:: Handle click button, and save the choice in on Firestore database ::--
            whenUserClickedOnChoiceBtn(pPlace.getId(), pPlace.getName(), pPlace.getAddress(), Objects.requireNonNull(pPlace.getTypes()).get(0).toString());
            //--:: Go on Firestore and get all users that have same place id as the selected one ::--
            getListOfSubscribedUsers(pPlace.getId());
            //--:: Configure buttons on Restaurant Activity, Call, Like, Website ::--
            configureButtons(pPlace.getId());
            //--:: Get like button state on Firestore database ::--
            configureLikeButton(pPlace.getId());
            //--:: Get like button state on Firestore database ::--
            retrieveLikeBtn(pPlace.getId());
            //--:: By getting Firestore data, get user's choice about place ::--
            getLunchChoice(pPlace.getId());
        }
    }


    //------------------------
    // BOUND VIEWS
    // (RESTAURANT/ANY PLACE)
    //------------------------
    //--:: 1 -- Bound views according to Places Model Object ::-->
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

    //--:: 2 -- Bound views according to Place Object ::-->
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

    //--:: 3 -- Use a FetchPlaceRequest to get place photo (bitmap) ::-->
    private void fetchPlaceToImage(Place place) {
        String placeId = place.getId();
        //--:: Specify fields. Requests for photos must always have the PHOTO_METADATA field ::--
        final List<Place.Field> fields = Collections.singletonList(Place.Field.PHOTO_METADATAS);
        //--:: Get a Place object ::--
        assert placeId != null;
        final FetchPlaceRequest placeRequest = FetchPlaceRequest.newInstance(placeId, fields);
        placesClient.fetchPlace(placeRequest).addOnSuccessListener((response) -> {
            final Place placeFound = response.getPlace();
            //--:: Get the photo metadata ::--
            final List<PhotoMetadata> metadata = placeFound.getPhotoMetadatas();
            if (metadata == null || metadata.isEmpty()) {
                Log.w(TAG, "No photo metadata.");
                return;
            }
            placePhoto = metadata.get(0);

            //--:: Create a FetchPhotoRequest ::--
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

    //--------------------------------------
    // SET USER LUNCH CHOICE (on Firestore)
    //--------------------------------------
    //--:: 1 -- Handling click on choice Button ::-->
    private void whenUserClickedOnChoiceBtn(String placeId, String name, String address, String placeType) {
        choiceBtn.setOnClickListener(v -> fetchRestaurantWithCurrentUserForFirebase(placeId, name, address, placeType));
    }

    //--:: 2 -- Only If user hasn't choose restaurant yet, set it on firebase ::-->
    private void fetchRestaurantWithCurrentUserForFirebase(String pPlaceId, String pPlaceName, String placeAddress, String placeType) {
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
                placeName = user.getRestaurantName();

                assert currentUser != null;
                if (user.getName().equals(currentUser.getDisplayName())) {
                    //if (restaurantId == null) {
                    user.setRestaurantId(pPlaceId);
                    user.setRestaurantName(pPlaceName);
                    user.setRestaurantType(placeType);
                    user.setRestaurantAddress(placeAddress);
                    userBookReference.document(user.getName()).set(user, SetOptions.merge());
                    choiceBtn.setColorFilter(getResources().getColor(R.color.green_light));
                    Toast.makeText(this, getString(R.string.Youre_eating_at) + pPlaceName, Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(this, getString(R.string.Youve_already_choose) + placeName, Toast.LENGTH_LONG).show();
                    choiceBtn.setColorFilter(getResources().getColor(R.color.black));
                }
            }
        });
    }

    //--:: 3 -- Set color of Lunch button according to data (restaurantId) stores in database  ::-->
    private void getLunchChoice(String placeId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference varCollectionReference = db.collection("Users");
        FirebaseUser userConnected = FirebaseAuth.getInstance().getCurrentUser();
        assert userConnected != null;
        DocumentReference varDocumentReference = varCollectionReference.document(Objects.requireNonNull(userConnected.getDisplayName()));
        varDocumentReference.get().addOnSuccessListener(pDocumentSnapshot -> {
            if (pDocumentSnapshot.exists()) {
                restaurantId = pDocumentSnapshot.getString("restaurantId");
                if (Objects.equals(restaurantId, placeId)) {
                    choiceBtn.setColorFilter(getResources().getColor(R.color.green_light));
                } else {
                    choiceBtn.setColorFilter(getResources().getColor(R.color.black));
                }
            }
        });
    }

    //-------------------------
    // GET DATA FOR USERS LIST
    //-------------------------
    //-- :: Get an instance of Firestore to get all users that choose a restaurant :: --
    private void getListOfSubscribedUsers(String pPlaceId) {
        database = FirebaseFirestore.getInstance();
        CollectionReference userBookReference = database.collection("Users");
        usersById.clear();
        userBookReference.get().addOnSuccessListener(pQueryDocumentSnapshots -> {
            for (QueryDocumentSnapshot varQueryDocumentSnapshot : pQueryDocumentSnapshots) {
                //-- :: Associate document with object POJO class :: --
                user = varQueryDocumentSnapshot.toObject(User.class);
                restaurantId = user.getRestaurantId();
                //-- :: Get connected current user :: --
                FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                if (pPlaceId.equals(restaurantId) && !usersById.contains(user)) {
                    usersById.add(user);
                }
                assert currentUser != null;
                if (user.getName().equals(currentUser.getDisplayName())) {
                    usersById.remove(user);
                }
            }
            ListView userListBooking = findViewById(R.id.registered_workmates_list);
            BookingRestaurantAdapter adapter = new BookingRestaurantAdapter(RestaurantActivity.this, usersById);
            userListBooking.setAdapter(adapter);
            adapter.notifyDataSetChanged();
        });
    }

    //-----------------------
    // CALL & WEBSITE BUTTONS
    //-----------------------
    private void configureButtons(String pPlaceId) {
        //--:: 1 -- Set design buttons ::-->
        callBtn = findViewById(R.id.call_btn);
        likeBtn = findViewById(R.id.like_btn);
        websiteBtn = findViewById(R.id.website_btn);

        //--:: 2 -- Get place details by retrofit request ::-->
        String url = "https://maps.googleapis.com/maps/api/place/details/json?"
                + "placeid="
                + pPlaceId + "&key=" + api_key;

        MapFragment.retrofitService.getDetailsById(url).enqueue(new Callback<PlaceResponse>() {
            @Override
            public void onResponse(@NonNull Call<PlaceResponse> call, @NonNull Response<PlaceResponse> response) {
                if (response.isSuccessful()) {
                    if (response.body() != null) {
                        detailsPlace = response.body().getPlaceDetails();

                        //--:: 3 -- Configure call button ::-->
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
                        //--:: 4 -- Configure website button ::-->
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
    }

    //---------------------------------
    // LIKE BUTTON (Database Firestore)
    //---------------------------------
    //--:: 1 -- Set on click like button listener ::-->
    public void configureLikeButton(String pPlaceId) {
        likeBtn.setOnClickListener(likeBtn -> {
        database = FirebaseFirestore.getInstance();
        CollectionReference userBookReference = database.collection("Users");
        userBookReference.get().addOnSuccessListener(pQueryDocumentSnapshots -> {
            for (QueryDocumentSnapshot varQueryDocumentSnapshot : pQueryDocumentSnapshots) {
                //-- :: Associate document with object POJO class :: --
                user = varQueryDocumentSnapshot.toObject(User.class);
                //-- :: Get connected current user :: --
                FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                //-- :: Define each user's attributes according to Firestore database  :: --
                assert currentUser != null;
                if (user.getName().equals(currentUser.getDisplayName())) {
                    restaurantLikedId = user.getRestaurantLikedId();
                        if(!restaurantLikedId.contains(pPlaceId)){
                            saveLikeBtn(pPlaceId);
                            likeBtn.setBackgroundColor(getResources().getColor(R.color.clear_orange_peach));
                        } else {
                            removeLikeBtn(pPlaceId);
                            likeBtn.setBackgroundColor(getResources().getColor(R.color.white));
                        }
                }
        }
    });
        });
}

    //--:: 2 -- Save like btn state in Firestore database  ::-->
    private void saveLikeBtn(String pPlaceId) {
        database = FirebaseFirestore.getInstance();
        CollectionReference userBookReference = database.collection("Users");
        userBookReference.get().addOnSuccessListener(pQueryDocumentSnapshots -> {
            for (QueryDocumentSnapshot varQueryDocumentSnapshot : pQueryDocumentSnapshots) {
                //-- :: Associate document with object POJO class :: --
                user = varQueryDocumentSnapshot.toObject(User.class);
                //-- :: Get connected current user :: --
                FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                //-- :: Define each user's attributes according to Firestore database  :: --
                assert currentUser != null;
                if (user.getName().equals(currentUser.getDisplayName())) {
                    userBookReference.document(user.getName()).update("restaurantLikedId", FieldValue.arrayUnion(pPlaceId));
                }
            }
        });
    }

    //--:: 2 -- Save like btn state in Firestore database  ::-->
    private void removeLikeBtn(String pPlaceId) {
        database = FirebaseFirestore.getInstance();
        CollectionReference userBookReference = database.collection("Users");
        userBookReference.get().addOnSuccessListener(pQueryDocumentSnapshots -> {
            for (QueryDocumentSnapshot varQueryDocumentSnapshot : pQueryDocumentSnapshots) {
                //-- :: Associate document with object POJO class :: --
                user = varQueryDocumentSnapshot.toObject(User.class);
                //-- :: Get connected current user :: --
                FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                //-- :: Define each user's attributes according to Firestore database  :: --
                assert currentUser != null;
                if (user.getName().equals(currentUser.getDisplayName())) {
                    userBookReference.document(user.getName()).update("restaurantLikedId", FieldValue.arrayRemove(pPlaceId));
                }
            }
        });
    }

    //--:: 3 -- Get state of like button store in Firestore database ::-->
    private void retrieveLikeBtn(String pPlaceId) {
        database = FirebaseFirestore.getInstance();
        CollectionReference userBookReference = database.collection("Users");
        userBookReference.get().addOnSuccessListener(pQueryDocumentSnapshots -> {
            for (QueryDocumentSnapshot varQueryDocumentSnapshot : pQueryDocumentSnapshots) {
                //-- :: Associate document with object POJO class :: --
                user = varQueryDocumentSnapshot.toObject(User.class);
                //-- :: Get connected current user :: --
                FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                //-- :: Define each user's attributes according to Firestore database  :: --
                restaurantLikedId = user.getRestaurantLikedId();
                assert currentUser != null;
                if (user.getName().equals(currentUser.getDisplayName())) {
                    if (restaurantLikedId != null && restaurantLikedId.contains(pPlaceId)) {
                        likeBtn.setBackgroundColor(getResources().getColor(R.color.clear_orange_peach));
                    }
                }
            }
        });
    }
}



