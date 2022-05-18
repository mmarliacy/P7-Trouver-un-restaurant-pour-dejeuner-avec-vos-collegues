package com.oc.gofourlunch.view.fragmentCoworkersList;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.oc.gofourlunch.R;
import com.oc.gofourlunch.controller.adapter.CoworkersListAdapter;
import com.oc.gofourlunch.controller.adapter.RestaurantListAdapter;
import com.oc.gofourlunch.model.GooglePlaces.PlacesModel;
import com.oc.gofourlunch.model.User.User;

import java.util.ArrayList;
import java.util.List;


public class CoworkersListFragment extends Fragment {

    //--:: Data for settings ::--
    ListView fListView;
    public List<User> fUserList ;


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
        getUsersInFirestoreDatabase();
        return view;
    }

    //-------------------------------
    // GET SUBSCRIBED COWORKERS LIST
    //-------------------------------
    private void getUsersInFirestoreDatabase() {
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection("Users").get().addOnSuccessListener(pQueryDocumentSnapshots -> {
            for (QueryDocumentSnapshot varQueryDocumentSnapshot : pQueryDocumentSnapshots) {
                User user = varQueryDocumentSnapshot.toObject(User.class);
                String name = user.getName();
                String mail = user.getMail();
                String photo = user.getPhotoURL();
                String restaurantId = user.getRestaurantId();

                if (restaurantId != null){
                    fUserList.add(user);
                    FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                    assert currentUser != null;
                    if (name.equals(currentUser.getDisplayName())) {
                        fUserList.remove(user);
                    }
                } else {
                    User localUser = new User(name, mail, photo);
                    fUserList.add(localUser);
                }
                fListView.setAdapter(new CoworkersListAdapter(getContext(), fUserList));
            }
        });
    }
}