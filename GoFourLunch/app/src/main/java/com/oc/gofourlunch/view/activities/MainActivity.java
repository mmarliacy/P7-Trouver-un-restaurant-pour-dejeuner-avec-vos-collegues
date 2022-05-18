package com.oc.gofourlunch.view.activities;


import static com.oc.gofourlunch.view.activities.RestaurantActivity.subscribedUsersPref;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.oc.gofourlunch.databinding.ActivityMainBinding;
import com.oc.gofourlunch.model.User.User;
import com.oc.gofourlunch.R;

import com.oc.gofourlunch.view.fragmentCoworkersList.CoworkersListFragment;
import com.oc.gofourlunch.view.fragmentMap.MapFragment;
import com.oc.gofourlunch.view.fragmentRestaurantList.RestaurantListFragment;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class MainActivity extends com.oc.gofourlunch.view.activities.BaseActivity<ActivityMainBinding> implements NavigationView.OnNavigationItemSelectedListener {

    //----------
    // FOR DATA
    //----------
    private static final String TAG = "Problem in : " + MainActivity.class.getName();

    //--:: Firebase/Firestore::--
    private FirebaseFirestore database = FirebaseFirestore.getInstance();
    private FirebaseUser currentUser;
    private Uri photoUser;
    private User user;

    //--:: Get data for daily notification ::--
    public static SharedPreferences fSharedPreferences;
    public static String name;
    public static String address;
    public static String id;
    public static Set usersList;

    //--:: Navigation View ::--
    ActionBarDrawerToggle varToggle;
    Toolbar varToolbar;

    //------------
    // FOR DESIGN
    //------------
    //--:: Navigation View ::--
    private NavigationView fNavigationView;

    //--:: View Binding ::--
    @Override
    ActivityMainBinding getViewBinding() {
        return ActivityMainBinding.inflate(getLayoutInflater());
    }

    //---------------------------
    // ON-CREATE : MAIN ACTIVITY
    //---------------------------
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(binding.getRoot());
        setUpBottomNavigation();
        setUpNavigationDrawer();
        setNavigationView();
        checkIfUserIsInDb();
        retrieveInfoForNotification();

    }

    //----------------------
    // NAVIGATION BOTTOM VIEW
    //----------------------
    //--:: 1 -- Connecting bottom Navigation View to fragment views ::-->
    private void setUpBottomNavigation() {
        BottomNavigationView bottomNavigationView = binding.bottomNavigation;
        bottomNavigationView.setSelectedItemId(R.id.map_view);
        bottomNavigationView.setOnItemSelectedListener(bottomNavListener);
       replaceFragment(new MapFragment());
    }

    //--:: 2 -- Handling position of bottom Navigation View and setting results ::-->
    private final BottomNavigationView.OnItemSelectedListener bottomNavListener =
            item -> {
                switch (item.getItemId()) {
                    case (R.id.map_view):
                       replaceFragment(new MapFragment());
                        break;

                    case (R.id.list_view):
                       replaceFragment(new RestaurantListFragment());
                        break;

                    case (R.id.workmates):
                     replaceFragment(new CoworkersListFragment());
                        varToolbar.setTitle(R.string.available_workmates);
                        break;
                }
                return true;
            };

    //--:: 3 -- Calling FragmentManager to handle transaction between main fragment and the others ::-->
    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.main_fragment, fragment);
        fragmentTransaction.commit();
    }

    //-------------------
    // NAVIGATION DRAWER
    //-------------------
    //--:: 1 -- Establish Navigation Drawer's behaviour ::-->
    private void setUpNavigationDrawer() {
        //--::> Navigation Drawer && Toolbar
        varToolbar = binding.toolbar;
        DrawerLayout varDrawerLayout = binding.drawerLayout;
        setSupportActionBar(varToolbar); //--::> Set Up Toolbar
        //--::> Define new Toggle for Drawer Layout and connect it to current Activity
        varToggle = new ActionBarDrawerToggle(
                this,
                varDrawerLayout,
                varToolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close
        );
        varDrawerLayout.addDrawerListener(varToggle);
        varToggle.syncState();
    }

    //--:: 2 -- Set Toggle activated if item selected ::-->
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (varToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    //-------------------
    // NAVIGATION VIEW
    //-------------------
    //--:: 1 -- Establish Navigation View's behaviour ::-->
    private void setNavigationView() {
        fNavigationView = binding.navView;
        fNavigationView.setNavigationItemSelectedListener(this);
        updateNavHeader();
    }

    //--:: 2 -- Update Nav Header Menu with current User information ::-->
    @SuppressLint("SetTextI18n")
    private void updateNavHeader() {
        View headerView = fNavigationView.getHeaderView(0); //--::> Get Header View
        //--::> Get graphics elements
        TextView userName = headerView.findViewById(R.id.profile_name);
        TextView userMail = headerView.findViewById(R.id.profile_mail);
        ImageView userPhoto = headerView.findViewById(R.id.profile_picture);
        //--::> Get current User from parcelable intent and bind info to graphics elements
            currentUser = getIntent().getParcelableExtra("user");
            photoUser = getIntent().getParcelableExtra("userPhoto");
            userName.setText(currentUser.getDisplayName());
            userMail.setText(currentUser.getEmail());
            Glide.with(this).load(photoUser).into(userPhoto);

        }

    //--:: 3 -- Handle click on Navigation view items and defining their behaviour ::-->
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case (R.id.lunch_item):
                startActivity(new Intent(MainActivity.this, YourLunch.class));
                break;
            case (R.id.settings_item):
               startActivity(new Intent(this, SettingsActivity.class));
                break;

            case (R.id.log_out_item):
                logOut();
                break;
        }
        return false;
    }

    //----------------------------------------------
    // CHECKING USER IN DATABASE : GET IT or ADD IT
    //----------------------------------------------
    private void checkIfUserIsInDb() {
            FirebaseFirestore rootRef = FirebaseFirestore.getInstance();
            DocumentReference docIdRef = rootRef.collection("Users").document(Objects.requireNonNull(currentUser.getDisplayName()));
            docIdRef.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        getUserInDatabase();
                    } else {
                        saveNewUserInDb();
                    }
                } else {
                    Log.d(TAG, "Failed with: ", task.getException());
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception pE) {
                    Log.i("FirestoreError",pE.getMessage());
                    Toast.makeText(MainActivity.this, pE.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

    }

    private void getUserInDatabase() {
        //--::> Init Firestore.
        database = FirebaseFirestore.getInstance();
        //--::> Create new collection if there is no collection with same name "User".
        CollectionReference userCollection = database.collection("Users");
        //--::> Get user when authentication is done.
        database = FirebaseFirestore.getInstance();
        userCollection.get().addOnSuccessListener(pQueryDocumentSnapshots -> {
            for (QueryDocumentSnapshot varQueryDocumentSnapshot : pQueryDocumentSnapshots) {
                User user = varQueryDocumentSnapshot.toObject(User.class);
                if (Objects.equals(varQueryDocumentSnapshot.get(user.getName()), currentUser.getDisplayName())) {
                    String name = user.getName();
                    String mail = user.getMail();
                    String photo = user.getPhotoURL();
                    String restaurantId = user.getRestaurantId();
                    String restaurantType = user.getRestaurantType();
                    String restaurantName = user.getRestaurantName();
                    User localUser = new User(name, mail, photo, restaurantId, restaurantType, restaurantName);

                    Toast.makeText(
                            MainActivity.this,
                            "You are authenticated with " + localUser.getName() + " account",
                            Toast.LENGTH_SHORT).
                            show();
                }
            }
        });
    }

    private void saveNewUserInDb() {
        //--::> Init Firestore
        database = FirebaseFirestore.getInstance();
        //--::> Get new user when log in is done.
        assert currentUser != null;
        if (photoUser != null) {
            user = new User(currentUser.getDisplayName(), currentUser.getEmail(), photoUser.toString());
        } else {
            user = new User(currentUser.getDisplayName(), currentUser.getEmail());
        }
        //--::> Create new collection if there is no collection with same name "User".
        CollectionReference userCollection = database.collection("Users");
        DocumentReference varDocumentReference = userCollection.document(Objects.requireNonNull(currentUser.getDisplayName()));
        //--::> Create new document with user information.
        varDocumentReference.set(user).addOnSuccessListener(pUnused -> Toast.makeText(
                MainActivity.this,
                "You are authenticated with " + user.getName() + " account",
                Toast.LENGTH_SHORT).
                show()).addOnFailureListener(pE -> Toast.makeText(
                MainActivity.this,
                "Authentication failed",
                Toast.LENGTH_SHORT).
                show());
    }

    //---------------
    // NOTIFICATIONS
    //---------------
    //-- :: Data stores in Restaurant Activity,  to retrieve and send to user in daily notification  :: -->
    private void retrieveInfoForNotification() {
        Set<String> defSet = new HashSet<>();
        fSharedPreferences = getSharedPreferences("lunchPref", MODE_PRIVATE);
        subscribedUsersPref = getSharedPreferences("usersList", MODE_PRIVATE);
        name = fSharedPreferences.getString("name", "no restaurant name");
        address = fSharedPreferences.getString("address", "no restaurant address");
        id = fSharedPreferences.getString("id", "no Id");
        usersList = subscribedUsersPref.getStringSet("list", defSet);
    }


    //---------------------------
    // LOG OUT & ON BACK PRESSED
    //---------------------------
    //--:: 1 -- Handle user's log out ::-->
    private void logOut() {
        FirebaseAuth firebase = FirebaseAuth.getInstance();
        firebase.signOut();
        startActivity(new Intent(this, LoginActivity.class));
        Toast.makeText(this, R.string.You_logged_out, Toast.LENGTH_SHORT).show();
    }
    //--:: 2 -- Handle "back" button action ::-->
    @Override
    public void onBackPressed() {
        int count = getSupportFragmentManager().getBackStackEntryCount();
        if (count == 0) {
            super.onBackPressed();
        } else {
            getSupportFragmentManager().popBackStack();
        }
    }

    /*
    -----------------
     USER VIEW MODEL
    -----------------
    --:: 1 -- Init View Model and get users list ::-->
     private void initViewModel() {
        UserViewModel varUserViewModel = new ViewModelProvider(this).get(UserViewModel.class);
        varUserViewModel.getAllUsers();
    }
    */
}