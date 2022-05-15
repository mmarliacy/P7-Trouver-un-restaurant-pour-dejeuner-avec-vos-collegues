package com.oc.gofourlunch.view.fragmentCoworkersList;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.SetOptions;
import com.oc.gofourlunch.model.GooglePlaces.PlacesModel;
import com.oc.gofourlunch.model.User.User;
import com.oc.gofourlunch.R;
import com.oc.gofourlunch.controller.adapter.CoworkersListAdapter;
import com.oc.gofourlunch.controller.adapter.RestaurantListAdapter;


import java.util.ArrayList;
import java.util.List;


public class CoworkersListFragment extends Fragment {

    //--:: Data for settings ::--
    ListView fListView;
    public List<User> fUserList ;
    List<PlacesModel> fPlacesModels;
    String restaurantType;
    String restaurantName;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fUserList= new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_coworkers_list, container, false);
        fListView = view.findViewById(R.id.list_view_coworkers);
        fPlacesModels = RestaurantListAdapter.restaurantList;
        getUsersAccordingToRestaurantId();
        return view;
    }

    private void getUsersAccordingToRestaurantId() {
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        CollectionReference userBookReference = database.collection("Users");
        database.collection("Users").get().addOnSuccessListener(pQueryDocumentSnapshots -> {
            for (QueryDocumentSnapshot varQueryDocumentSnapshot : pQueryDocumentSnapshots) {
                User user = varQueryDocumentSnapshot.toObject(User.class);
                String name = user.getName();
                String mail = user.getMail();
                String photo = user.getPhotoURL();
                String restaurantId = user.getRestaurantId();
                List<PlacesModel> restaurantsList = new ArrayList<>();
                for (int i = 0; i < fPlacesModels.size(); i++) {
                    PlacesModel placesModel = fPlacesModels.get(i);
                    if (placesModel.getPlaceId().equals(restaurantId)) {
                        restaurantsList.add(placesModel);
                        for (int j = 0; j < restaurantsList.size(); j++) {
                            PlacesModel restaurantPlace = restaurantsList.get(j);
                            restaurantType = restaurantPlace.getTypes().get(0);
                            restaurantName = restaurantPlace.getName();
                        }
                        User localUser = new User(name, mail, photo, restaurantId, restaurantType, restaurantName);
                        userBookReference.document(user.getName()).set(localUser, SetOptions.merge());
                        fUserList.add(localUser);
                        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                        assert currentUser != null;
                        if (localUser.getName().equals(currentUser.getDisplayName())) {
                            fUserList.remove(localUser);
                        }
                    }
                }
                fListView.setAdapter(new CoworkersListAdapter(getContext(), fUserList, fPlacesModels));
            }
        });
    }


    //-----------------------------
    // DISPLAYING RESTAURANT LIST
    //-----------------------------

}