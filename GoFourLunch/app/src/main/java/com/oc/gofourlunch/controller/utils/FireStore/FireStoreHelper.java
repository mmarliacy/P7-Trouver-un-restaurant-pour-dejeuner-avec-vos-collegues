package com.oc.gofourlunch.controller.utils.FireStore;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

public class FireStoreHelper {

    //----------
    // FOR DATA
    //----------
    private static FireStoreHelper mFireStoreHelper;

    //--:: Singleton pattern, to unique instantiation ::--
    public static FireStoreHelper getInstance() {
        if (mFireStoreHelper == null) {
            mFireStoreHelper = new FireStoreHelper();
        }
        return mFireStoreHelper;
    }

    //--:: Create database by getting instance of FireStore, and make one collection "users" ::--
    private final FirebaseFirestore database = FirebaseFirestore.getInstance();
    public final CollectionReference userRef = database.collection("users");

    //--:: Get all users, by query FirebaseFirestore database ::--
    public Task<QuerySnapshot> getAllUsers(){
        return userRef.get();
    }
}
