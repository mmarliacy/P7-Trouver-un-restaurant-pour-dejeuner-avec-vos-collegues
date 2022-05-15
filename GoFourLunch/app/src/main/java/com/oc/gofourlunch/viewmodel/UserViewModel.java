package com.oc.gofourlunch.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;


import com.oc.gofourlunch.model.User.User;
import com.oc.gofourlunch.viewmodel.repository.UserRepository;

import java.util.List;

public class UserViewModel extends ViewModel {

    //----------
    // FOR DATA
    //----------
    private final UserRepository userRepository;

    //--------------
    // CONSTRUCTOR
    //--------------
    public UserViewModel() {
        userRepository = UserRepository.getInstance();
    }

    //--:: Get LiveData list of items "users" ::--
    public LiveData<List<User>> getAllUsers() {
        return userRepository.getUsers();
    }

}
