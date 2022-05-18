package com.oc.gofourlunch.view.fragmentMap;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;
import static com.oc.gofourlunch.R.drawable.google_marker_restaurant;
import static com.oc.gofourlunch.R.drawable.google_marker_restaurant_green;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.PhotoMetadata;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPhotoRequest;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.oc.gofourlunch.R;
import com.oc.gofourlunch.controller.adapter.RestaurantListAdapter;
import com.oc.gofourlunch.controller.utils.WebService.RetrofitClient;
import com.oc.gofourlunch.controller.utils.WebService.RetrofitService;
import com.oc.gofourlunch.databinding.FragmentMapBinding;
import com.oc.gofourlunch.model.GooglePlaces.GooglePlacesModel;
import com.oc.gofourlunch.model.GooglePlaces.PhotoModel;
import com.oc.gofourlunch.model.GooglePlaces.PlacesModel;
import com.oc.gofourlunch.view.activities.RestaurantActivity;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MapFragment extends Fragment implements OnMapReadyCallback, GoogleMap.OnInfoWindowClickListener {

    //-------------
    // FOR DESIGN
    //-------------
    FloatingActionButton userLocationBtn;
    Toolbar toolbar;

    //----------
    // FOR DATA
    //----------
    FragmentMapBinding binding;
    private final String TAG = "Alert";
    private String api_key;
    GoogleMap map;

    //--:: Map default parameters ::--
    SupportMapFragment supportMapFragment;

    //--:: Last-know location retrieved by the Fused Location Provider ::--
    public static Location lastKnownLocation;
    //--:: Request location permission ::--
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 44;
    public boolean locationPermissionGranted;

    //--:: Finding places nearly ::--
    public static RetrofitService retrofitService;
    private int radius = 5000;
    List<PlacesModel> responsesList;
    private final List<PlacesModel> googlePlacesList = new ArrayList<>();
    PlacesModel selectedRestaurant;
    PlacesClient placesClient;
    MarkerOptions nearMarkerOptions;
    RestaurantListAdapter fRestaurantListAdapter = new RestaurantListAdapter(this.googlePlacesList);
    private String restaurantId;

    //--:: Autocomplete Places ::--
    private static final int AUTOCOMPLETE_REQUEST_CODE = 1;
    Place place;
    PhotoModel selectedImage;
    PhotoMetadata photoMetadata;
    Bitmap bitmap;
    PhotoMetadata placePhoto;


    //------------------------
    // DISPLAYING MAP & LAYOUT
    //------------------------
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map, container, false);
        //--:: Set support Toolbar ::--
        configureToolbar();
        //--:: Get map API key && Initialize PlacesClient Object ::--
        getMapApiKeyAndInitializePlacesClient();
        //--:: Initializing Retrofit for given fragment ::--
        initialize_retrofitClient();
        //--:: Get RestaurantId to define marker's icon color for the selected restaurant ::--
        getRestaurantIdOfCurrentUser();
        //--:: Get Device Location ::--
        getDeviceLocation();
        //--:: Bind and return fragment view ::--
        binding = FragmentMapBinding.inflate(inflater, container, false);
        return view;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        userLocationBtn = requireView().findViewById(R.id.fab_my_location);
        //--:: Configure button to center camera on user location::--
        userLocationBtn.setOnClickListener(v -> getDeviceLocation());
    }


    //----------------------------------------------------------
    // ON START - CONFIGURE ALL NEEDED MODULES FOR MAP FRAGMENT
    //----------------------------------------------------------
    //--:: 1 -- Configure toolbar ::-->
    public void configureToolbar() {
        toolbar = requireActivity().findViewById(R.id.toolbar);
        ((AppCompatActivity) requireActivity()).setSupportActionBar(toolbar);
        setHasOptionsMenu(true);
    }

    //--:: 2 -- Initialize Map key and Places Client ::-->
    public void getMapApiKeyAndInitializePlacesClient() {
        try {
            ApplicationInfo varInfo_key = Objects.requireNonNull(getActivity()).getPackageManager().getApplicationInfo(getActivity().getApplicationContext().getPackageName(), PackageManager.GET_META_DATA);
            api_key = varInfo_key.metaData.getString("com.google.android.geo.API_KEY");
        } catch (PackageManager.NameNotFoundException pE) {
            pE.printStackTrace();
        }
        //--:: Initialize a PlacesClient Object ::--
        Places.initialize(requireContext(), api_key);
        placesClient = Places.createClient(requireActivity());
    }

    //--:: 3 -- Initialize retrofit Client for request API ::-->
    public void initialize_retrofitClient() {
        retrofitService = RetrofitClient.getRetrofitClient().create(RetrofitService.class);
    }

    //------------------------------------
    // GET ACCESS TO USER DEVICE LOCATION
    //------------------------------------
    //--:: 1 -- Gets the current location of the device ::-->
    @RequiresApi(api = Build.VERSION_CODES.M)
    private void getDeviceLocation() {
        //--:: The entry point to the Fused Location Provider::--
        FusedLocationProviderClient varFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireActivity());
        //--:: Request location permission, so that we can get the location of the device ::--
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
            locationPermissionGranted = true;
        }
        //--:: Launch Task Location ::--
        Task<Location> locationResult = varFusedLocationProviderClient.getLastLocation();
        locationResult.addOnSuccessListener(location -> {
            if (location != null) {
                lastKnownLocation = location;
                //--:: Initializing support Map Fragment & Sync Map ::--
                supportMapFragment = (SupportMapFragment) getChildFragmentManager()
                        .findFragmentById(R.id.google_maps_fragment);
                assert supportMapFragment != null;
                supportMapFragment.getMapAsync(MapFragment.this);
            }
        });
    }

    //--:: 2 -- Handling result of getting permission and relaunch getDeviceLocation ::-->
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getDeviceLocation();
                locationPermissionGranted = true;
            }
        }
    }

    //-----------------
    // CONFIGURING MAP
    //-----------------
    //--:: 1 -- On Map ready animate camera, add markers... ::-->
    @Override
    public void onMapReady(@NonNull GoogleMap pGoogleMap) {
        locationPermissionGranted = true;
        LatLng latLngCurrentLocation = new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
        MarkerOptions marker =
                new MarkerOptions().
                        position(latLngCurrentLocation).
                        title(String.valueOf(R.string.here)).
                        snippet(String.valueOf(R.string.here));
        pGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLngCurrentLocation, 14));
        pGoogleMap.addMarker(marker);
        // Enable the zoom controls for the map
        pGoogleMap.getUiSettings().setZoomControlsEnabled(true);
        getPlaces(lastKnownLocation, pGoogleMap);
        map = pGoogleMap;
    }

    //------------------------------
    // GET PLACES : NEAR BY PLACES
    //------------------------------
    //--:: 1 -- Make API request to get all restaurants, add markers ::-->
    private void getPlaces(Location location, GoogleMap map) {
        if (locationPermissionGranted) {
            String url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location="
                    + location.getLatitude() + ","
                    + location.getLongitude()
                    + "&radius=" + radius
                    + "&type=restaurant"
                    + "&key=" + api_key;

            retrofitService.getNearByPlaces(url).enqueue(new Callback<GooglePlacesModel>() {
                @SuppressLint("NotifyDataSetChanged")
                @RequiresApi(api = Build.VERSION_CODES.M)
                @Override
                public void onResponse(@NonNull Call<GooglePlacesModel> call, @NonNull Response<GooglePlacesModel> response) {
                    if (response.errorBody() == null) {
                        if (response.body() != null) {
                            responsesList = response.body().getGooglePlaceList();
                            if (responsesList != null && responsesList.size() > 0) {
                                googlePlacesList.clear();
                                map.clear();
                                for (int i = 0; i < response.body().getGooglePlaceList().size(); i++) {
                                    googlePlacesList.add(responsesList.get(i));
                                    map.addMarker(addMarker(responsesList.get(i), map));
                                    selectedRestaurant = responsesList.get(i);
                                    fetchImageAndData(i, responsesList.get(i));
                                }
                                fRestaurantListAdapter.setGooglePlacesList(googlePlacesList);
                            } else {
                                map.clear();
                                googlePlacesList.clear();
                                radius += 1000;
                            }
                        }
                    } else {
                        Log.d("TAG", "onResponse: " + response.errorBody());
                        Toast.makeText(requireContext(), "Error : " + response.errorBody(), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<GooglePlacesModel> call, @NonNull Throwable t) {
                    Log.d("TAG", "onFailure: " + t);
                }
            });
        }
    }

    //--:: 2 -- Fetch place photo to each restaurant ::-->
    private void fetchImageAndData(int i, PlacesModel placesModel) {
        String placeId = responsesList.get(i).getPlaceId();
        //--:: Specify fields. Requests for photos must always have the PHOTO_META_DATA field ::--
        final List<Place.Field> fields = Collections.singletonList(Place.Field.PHOTO_METADATAS);
        //--:: Get a Place object (this example uses fetchPlace(), but you can also use findCurrentPlace()) ::--
        final FetchPlaceRequest placeRequest = FetchPlaceRequest.newInstance(placeId, fields);

        placesClient.fetchPlace(placeRequest).addOnSuccessListener((response) -> {
            final Place place = response.getPlace();
            //--:: Get the photo metadata ::--
            final List<PhotoMetadata> metadata = place.getPhotoMetadatas();
            if (metadata == null || metadata.isEmpty()) {
                Log.w(TAG, "No photo metadata.");
                return;
            }
            photoMetadata = metadata.get(0);
            //--:: Create a FetchPhotoRequest ::--
            final FetchPhotoRequest photoRequest = FetchPhotoRequest.builder(photoMetadata)
                    .setMaxWidth(500) // Optional.
                    .setMaxHeight(300) // Optional.
                    .build();
            placesClient.fetchPhoto(photoRequest).addOnSuccessListener((fetchPhotoResponse) -> {
                bitmap = fetchPhotoResponse.getBitmap();
                selectedImage = placesModel.getPhotos().get(0);
                selectedImage.setImage(bitmap);

            }).addOnFailureListener((exception) -> {
                if (exception instanceof ApiException) {
                    Log.e(TAG, "Place not found: " + exception.getMessage());
                    // TODO: Handle error with given status code.
                }
            });
        });
    }

    //--:: 3 -- Create marker for each restaurant localized on Map::-->
    private MarkerOptions addMarker(PlacesModel placesModel, GoogleMap map) {
        double newLat = placesModel.getGeometry().getLocation().getLat();
        double newLng = placesModel.getGeometry().getLocation().getLng();
        LatLng latLng = new LatLng(newLat, newLng);
        if (restaurantId != null && restaurantId.equals(placesModel.getPlaceId())) {
            Bitmap myLogo = BitmapFactory.decodeResource(requireContext().getResources(), google_marker_restaurant_green);
            BitmapDescriptor pin = BitmapDescriptorFactory.fromBitmap(myLogo);
            nearMarkerOptions = new MarkerOptions()
                    .position(latLng)
                    .title(placesModel.getName())
                    .snippet(placesModel.getVicinity())
                    .icon(pin);
        } else {
            Bitmap myLogo = BitmapFactory.decodeResource(requireContext().getResources(), google_marker_restaurant);
            BitmapDescriptor pin = BitmapDescriptorFactory.fromBitmap(myLogo);
            nearMarkerOptions = new MarkerOptions()
                    .position(latLng)
                    .title(placesModel.getName())
                    .snippet(placesModel.getVicinity())
                    .icon(pin);
        }
        Objects.requireNonNull(map.addMarker(nearMarkerOptions)).setTag(placesModel);
        map.setInfoWindowAdapter(infoWindowAdapter);
        map.setOnInfoWindowClickListener(MapFragment.this);
        return nearMarkerOptions;
    }

    //--:: 5 -- Get RestaurantId to define marker's icon color for the selected restaurant ::-->
    public void getRestaurantIdOfCurrentUser() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference varCollectionReference = db.collection("Users");
        FirebaseUser userConnected = FirebaseAuth.getInstance().getCurrentUser();
        assert userConnected != null;
        DocumentReference varDocumentReference = varCollectionReference.document(Objects.requireNonNull(userConnected.getDisplayName()));
        varDocumentReference.get().addOnSuccessListener(pDocumentSnapshot -> {
            if (pDocumentSnapshot.exists()) {
                restaurantId = pDocumentSnapshot.getString("restaurantId");
            }
        });
    }

    //-----------------------------------
    // AUTOCOMPLETE : GETTING PREDICTIONS
    //-----------------------------------
    //--:: 1 -- Create search icon by implemented a menu ::-->
    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater
            inflater) {
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
    public void onActivityResult(int requestCode, int resultCode,
                                 @Nullable Intent data) {
        if (requestCode == AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                assert data != null;
                place = Autocomplete.getPlaceFromIntent(data);
                Log.i(TAG, "Place: " + place.getName() + ", " + place.getAddress());
                map.clear();
                map.addMarker(addNearMarker(place));
                //--:: The user can click on Marker to go on Restaurant activity ::--
                map.moveCamera(CameraUpdateFactory.newLatLng(Objects.requireNonNull(place.getLatLng())));
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

    //--:: 5 -- Get place from intent, and define a marker to locate the place position ::-->
    private MarkerOptions addNearMarker(Place place) {
        double newLat = Objects.requireNonNull(place.getLatLng()).latitude;
        double newLng = place.getLatLng().longitude;
        MarkerOptions nearMarkerOptions = null;
        Bitmap myLogo = BitmapFactory.decodeResource(requireContext().getResources(), R.drawable.google_marker_restaurant_blue);
        BitmapDescriptor pin = BitmapDescriptorFactory.fromBitmap(myLogo);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            nearMarkerOptions = new MarkerOptions()
                    .position(new LatLng(newLat, newLng))
                    .title(place.getName())
                    .snippet(place.getAddress())
                    .icon(pin);
        }
        assert nearMarkerOptions != null;
        //--:: Set Place tag to marker, to get it in intent ::--
        Objects.requireNonNull(map.addMarker(nearMarkerOptions)).setTag(place);
        map.setInfoWindowAdapter(infoWindowAdapter);
        return nearMarkerOptions;
    }

    //---------------------------
    // MAP MARKER : DISPLAY INFO
    //---------------------------
    GoogleMap.InfoWindowAdapter infoWindowAdapter = new GoogleMap.InfoWindowAdapter() {
        @NonNull
        @Override
        public View getInfoContents(@NonNull Marker marker) {
            @SuppressLint("InflateParams")
            View view = requireActivity().getLayoutInflater().inflate(R.layout.info_windows_popup, null);
            TextView restNamePopUp = view.findViewById(R.id.restaurant_name_popup);
            TextView restAddressPopUp = view.findViewById(R.id.restaurant_address_popup);
            restNamePopUp.setText(marker.getTitle());
            restAddressPopUp.setText(marker.getSnippet());
            return view;
        }

        @Nullable
        @Override
        public View getInfoWindow(@NonNull Marker marker) {
            return null;
        }
    };

    //-----------------------------
    // MAP MARKER : HANDLING CLICK
    //-----------------------------
    @Override
    public void onInfoWindowClick(@NonNull Marker marker) {
        if (marker.getTag() instanceof PlacesModel) {
            if (marker.getTag() != null) {
                if (marker.getTag() != null) {
                    selectedRestaurant = (PlacesModel) marker.getTag();
                    Bitmap photo = selectedRestaurant.getPhotos().get(0).getImage();
                    //--:: Convert Bitmap to byte array to transfer by intent ::--
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    photo.compress(Bitmap.CompressFormat.PNG, 100, stream);
                    byte[] byteArray = stream.toByteArray();
                    Intent intent = new Intent(requireContext(), RestaurantActivity.class);
                    intent.putExtra("restaurant info", selectedRestaurant);
                    intent.putExtra("restaurant image", byteArray);
                    startActivity(intent);
                }
            }
        } else if (marker.getTag() instanceof Place) {
            if (marker.getTag() != null) {
                place = (Place) marker.getTag();
                if (place.getPhotoMetadatas() != null) {
                    placePhoto = Objects.requireNonNull(place.getPhotoMetadatas()).get(0);
                    Log.i("STEP : ", "Place selected has a photo");
                    Intent intent = new Intent(requireContext(), RestaurantActivity.class);
                    intent.putExtra("place info", place);
                    intent.putExtra("place image", placePhoto);
                    startActivity(intent);
                } else {
                    Log.i("STEP : ", "Place selected has no image found");
                    Intent intent = new Intent(requireContext(), RestaurantActivity.class);
                    intent.putExtra("place info", place);
                    startActivity(intent);
                }
            }
        }
    }
}