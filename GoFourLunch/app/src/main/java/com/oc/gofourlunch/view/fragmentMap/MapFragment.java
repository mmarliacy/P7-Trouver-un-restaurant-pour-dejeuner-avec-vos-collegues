package com.oc.gofourlunch.view.fragmentMap;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
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

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
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
import com.google.android.gms.tasks.OnSuccessListener;
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

public class MapFragment extends Fragment implements GoogleMap.OnInfoWindowClickListener{

    FragmentMapBinding binding;
    private String api_key;
    //--:: Map default parameters ::--
    SupportMapFragment supportMapFragment;
    LatLng defaultLocation = new LatLng(48.707795, 2.440960);
    private static final int DEFAULT_ZOOM = 18;
    public static double latLngLatitude;
    public static double latLngLongitude;

    //--:: Request location permission ::--
    int requestCode;
    int[] grantResults;
    private final String TAG = "Alert";
    private boolean locationPermissionGranted;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 44;

    //--:: The entry point to the Fused Location Provider::--
    private FusedLocationProviderClient fusedLocationProviderClient;

    //--:: Last-know location retrieved by the Fused Location Provider ::--
    private Location lastKnownLocation;

    //--:: For Design ::--
    FloatingActionButton userLocationBtn;

    //--:: Used for selecting the current place ::--
    private static final String KEY_CAMERA_POSITION = "camera_position";
    private static final String KEY_LOCATION = "location";

    //--:: Finding places nearly ::--
    private int radius = 5000;
    public static RetrofitService retrofitService;
    List<PlacesModel> responsesList;
    private final List<PlacesModel> googlePlacesList = new ArrayList<>();
    PlacesModel selectedRestaurant;
    MarkerOptions markerOptions;
    PlacesClient placesClient;

    //--:: Restaurant List Adapter ::--
    RestaurantListAdapter fRestaurantListAdapter = new RestaurantListAdapter(this.googlePlacesList);

    //--:: Autocomplete Places ::--
    private static final int AUTOCOMPLETE_REQUEST_CODE = 1;
    Toolbar toolbar;
    Place place;
    PhotoModel selectedImage;
    PhotoMetadata photoMetadata;
    Bitmap bitmap;
    Bitmap photo;
    PhotoMetadata placePhoto;
    GoogleMap map;


    //------------------------
    // DISPLAYING MAP & LAYOUT
    //------------------------
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map, container, false);
        //FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

        //--:: Set support Toolbar ::--
        toolbar = requireActivity().findViewById(R.id.toolbar);
        ((AppCompatActivity) requireActivity()).setSupportActionBar(toolbar);

        //--:: Get map API key ::--
        try {
            ApplicationInfo varInfo_key = getActivity().getPackageManager().getApplicationInfo(getActivity().getApplicationContext().getPackageName(), PackageManager.GET_META_DATA);
            api_key = varInfo_key.metaData.getString("com.google.android.geo.API_KEY");
        } catch (PackageManager.NameNotFoundException pE) {
            pE.printStackTrace();
        }

        //--:: Initialize a PlacesClient Object ::--
        Places.initialize(requireContext(), api_key);
        placesClient = Places.createClient(requireActivity());

        //--:: Initialize a FusedLocationProviderClient Object ::--
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        setHasOptionsMenu(true);
        //--:: Request location permission, so that we can get the location of the device ::--
        if (ActivityCompat.checkSelfPermission(requireActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(requireContext(), R.string.permission_granted, Toast.LENGTH_LONG).show();
            locationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(requireActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
            permissionResult.launch(Manifest.permission.ACCESS_FINE_LOCATION);

        }
        //--:: Initializing Retrofit for given fragment ::--
        retrofitService = RetrofitClient.getRetrofitClient().create(RetrofitService.class);

        //--:: Initializing support Map Fragment & Sync Map ::--
        supportMapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.google_maps_fragment);
        assert supportMapFragment != null;
        binding = FragmentMapBinding.inflate(inflater, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        userLocationBtn = requireView().findViewById(R.id.fab_my_location);
        //--:: Configure button to center camera on user location::--
        userLocationBtn.setOnClickListener(v -> getDeviceLocation());
    }

    //------------------------
    // INITIALIZING PLACES API
    //------------------------

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
                Log.i(TAG, "Place: " + place.getName() + ", " + place.getAddress());
                map.clear();
                map.addMarker(addNearMarker(place));
                //The user can click on Marker to go on Restaurant activity
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


    private MarkerOptions addNearMarker(Place place) {
        double newLat = Objects.requireNonNull(place.getLatLng()).latitude;
        double newLng = place.getLatLng().longitude;

        MarkerOptions nearMarkerOptions = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            nearMarkerOptions = new MarkerOptions()
                    .position(new LatLng(newLat, newLng))
                    .title(place.getName())
                    .snippet(place.getAddress())
                    .icon(getCustomIcon());
        }
        assert nearMarkerOptions != null;
       Objects.requireNonNull(map.addMarker(nearMarkerOptions)).setTag(place);
       map.setInfoWindowAdapter(infoWindowAdapter);
        return nearMarkerOptions;
    }

    //-------------------------------------------
    // CONFIGURING MAP WHEN GETTING DEVICE LOCATION
    //-------------------------------------------

    //--:: Gets the current location of the device, and positions the map's camera ::--
    private void getDeviceLocation() {
        @SuppressLint("MissingPermission")
        Task<Location> locationResult = fusedLocationProviderClient.getLastLocation();
        locationResult.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    supportMapFragment.getMapAsync(new OnMapReadyCallback() {
                        @Override
                        public void onMapReady(@NonNull GoogleMap pGoogleMap) {
                            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                            MarkerOptions marker =
                                    new MarkerOptions().
                                    position(latLng).
                                    title(String.valueOf(R.string.here)).
                                    snippet(String.valueOf(R.string.here));
                            pGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, pGoogleMap.getMinZoomLevel()));
                            pGoogleMap.addMarker(marker);
                            // Enable the zoom controls for the map
                            pGoogleMap.getUiSettings().setZoomControlsEnabled(true);
                            pGoogleMap.setOnInfoWindowClickListener(MapFragment.this);
                            getPlaces(location, pGoogleMap);
                            map = pGoogleMap;
                        }
                    });

                }
            }
        });
    }
    //-----------------------------------
    // GET ACCESS TO USER DEVICE LOCATION
    //-----------------------------------
    //--:: Handles the result of the request for location permissions ::--
    ActivityResultLauncher<String> permissionResult = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            new ActivityResultCallback<Boolean>() {
                @Override
                public void onActivityResult(Boolean result) {
                    if (result){
                        getDeviceLocation();
                        updateLocationUI();
                    }
                }

            });

    //------------------------------
    // GET PLACES : NEAR BY PLACES
    //------------------------------

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

    private void fetchImageAndData(int i, PlacesModel placesModel) {
        String placeId = responsesList.get(i).getPlaceId();
        // Specify fields. Requests for photos must always have the PHOTO_META_DATA field.
        final List<Place.Field> fields = Collections.singletonList(Place.Field.PHOTO_METADATAS);

// Get a Place object (this example uses fetchPlace(), but you can also use findCurrentPlace())
        final FetchPlaceRequest placeRequest = FetchPlaceRequest.newInstance(placeId, fields);

        placesClient.fetchPlace(placeRequest).addOnSuccessListener((response) -> {
            final Place place = response.getPlace();
            // Get the photo metadata.
            final List<PhotoMetadata> metadata = place.getPhotoMetadatas();
            if (metadata == null || metadata.isEmpty()) {
                Log.w(TAG, "No photo metadata.");
                return;
            }
            photoMetadata = metadata.get(0);

            // Create a FetchPhotoRequest.
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

    private MarkerOptions addMarker(PlacesModel placesModel, GoogleMap map) {
        double newLat = placesModel.getGeometry().getLocation().getLat();
        double newLng = placesModel.getGeometry().getLocation().getLng();
        LatLng latLng = new LatLng(newLat, newLng);
        MarkerOptions nearMarkerOptions = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            nearMarkerOptions = new MarkerOptions()
                    .position(latLng)
                    .title(placesModel.getName())
                    .snippet(placesModel.getVicinity())
                    .icon(getCustomIcon());
        }
        assert nearMarkerOptions != null;
       map.addMarker(nearMarkerOptions).setTag(placesModel);

        map.setInfoWindowAdapter(infoWindowAdapter);
        return nearMarkerOptions;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private BitmapDescriptor getCustomIcon() {
        Drawable pin = ContextCompat.getDrawable(requireContext(), R.drawable.google_marker_restaurant);
        assert pin != null;
        pin.setBounds(0, 0, pin.getIntrinsicWidth(), pin.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(pin.getIntrinsicWidth(), pin.getIntrinsicHeight(),
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        pin.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }


    //----------------------------
    // UPDATE MAP'S UI SETTINGS
    //----------------------------
    private void updateLocationUI() {
        /*
        if (map == null) {
            return;
        }
        try {
            //--:: If true, set location button on in device settings automatically::--
            if (locationPermissionGranted) {
                map.setMyLocationEnabled(true);
                map.getUiSettings().setMyLocationButtonEnabled(true);

                //--:: If false, let location button off in device settings ::--
            } else {
                map.setMyLocationEnabled(false);
                map.getUiSettings().setMyLocationButtonEnabled(false);
                lastKnownLocation = null;
            }
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage());
        }
         */
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
                    //Convert Bitmap to byte array
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    photo.compress(Bitmap.CompressFormat.PNG, 100, stream);
                    byte[] byteArray = stream.toByteArray();

                    assert selectedRestaurant != null;
                    Intent intent = new Intent(requireContext(), RestaurantActivity.class);
                    intent.putExtra("restaurant info", selectedRestaurant);
                    intent.putExtra("restaurant image", byteArray);
                    startActivity(intent);
                }
            }
        } else if (marker.getTag() instanceof Place) {
            if (marker.getTag() != null) {
                place = (Place) marker.getTag();
                // Put condition, and insert image in case of placePhoto == null
                if (place.getPhotoMetadatas() != null) {
                    placePhoto = Objects.requireNonNull(place.getPhotoMetadatas()).get(0);
                    Log.i("STEP : ", "Place selected has a photo");
                    Intent intent = new Intent(requireContext(), RestaurantActivity.class);
                    intent.putExtra("place info", place);
                    intent.putExtra("place image", placePhoto);
                    startActivity(intent);
                } else {
                    Log.i("STEP : ", "Place selected has no image found");
                    Toast.makeText(requireContext(), "Restaurant has been selected", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(requireContext(), RestaurantActivity.class);
                    intent.putExtra("place info", place);
                    Log.e("STEP : ", "Image : Not Found has been transferred");
                    startActivity(intent);
                }
            }
        }
    }
}


