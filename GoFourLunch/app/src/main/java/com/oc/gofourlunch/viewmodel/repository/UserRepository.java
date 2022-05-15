package com.oc.gofourlunch.viewmodel.repository;

import android.util.Log;

import androidx.lifecycle.MutableLiveData;

import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.oc.gofourlunch.model.User.User;
import com.oc.gofourlunch.controller.utils.FireStore.FireStoreHelper;

import java.util.ArrayList;
import java.util.List;

public class UserRepository {

    //----------
    // FOR DATA
    //----------
    private final MutableLiveData<List<User>> usersList = new MutableLiveData<>();
    private static UserRepository userRepository;
    private final FireStoreHelper mFireStoreHelper;

    //--:: Singleton pattern, to unique instantiation ::--
    public static UserRepository getInstance() {
        if (userRepository == null) {
            userRepository = new UserRepository();
        }
        return userRepository;
    }

    //--------------
    // CONSTRUCTOR
    //--------------
    public UserRepository() {
        mFireStoreHelper = FireStoreHelper.getInstance();
       // initData();
    }

    //--------------
    // GET DATA
    //--------------
    //--:: Get LiveData userList ::--
    public MutableLiveData<List<User>> getUsers() {
        mFireStoreHelper.getAllUsers().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                ArrayList<User> users = new ArrayList<>();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    users.add(document.toObject(User.class));
                }
                usersList.postValue(users);
            } else {
                Log.d("Error", "Error getting documents: ", task.getException());
            }
        }).addOnFailureListener(e -> {
            //--:: Handling error ::--
            usersList.postValue(null);
        });
        return usersList;
    }

    /*
    //--:: Init database with values when CoworkersList Fragment has been instantiated ::--
    private void initData() {
        FireStoreHelper.getInstance().userRef.add(
                new User(
                        "Sarah",
                        "sarah.ui@gmail.com",
                        "https://images.boardriders.com/global/billabong-products/all/default/hi-res/s3st68bip0_billabong,wg_1220_frt1.jpg",
                        0));
        FireStoreHelper.getInstance().userRef.add(
        new User(
                "Marco",
                "marc-THY@gmail.com",
                "https://file1.topsante.com/var/topsante/storage/images/couple-et-sexualite/amour-et-couple/" +
                        "vie-de-couple/amour-comment-reconnaitre-un-homme-amoureux/102208-2-fre-FR/Amour-comment-reconnaitre-un-homme-amoureux.jpg?alias=original",
                0));
        FireStoreHelper.getInstance().userRef.add(
                new User(
                        "MJean-Marc",
                        "JM.hill@gmail.com",
                        "https://les-comptoirs-du-soin.fr/wp-content/uploads/2019/07/peeling-visage-homme-dermo-peel.jpg",
                        0));

    }
     */
}
