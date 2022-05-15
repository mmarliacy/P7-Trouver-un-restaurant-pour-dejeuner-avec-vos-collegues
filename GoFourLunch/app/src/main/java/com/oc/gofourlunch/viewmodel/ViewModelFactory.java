package com.oc.gofourlunch.viewmodel;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

public class ViewModelFactory implements ViewModelProvider.Factory {

    //----------
    // FOR DATA
    //----------
    private static ViewModelFactory factory;


    //--------------
    // CONSTRUCTOR
    //--------------
    private ViewModelFactory() {
    }

    //--:: Singleton pattern, to unique instantiation ::--
    public static ViewModelFactory getInstance() {
        if (factory == null) {
            synchronized (ViewModelFactory.class) {
                if (factory == null) {
                    factory = new ViewModelFactory();
                }
            }
        }
        return factory;
    }

    //--:: Singleton pattern, to unique instantiation of ViewModel::--
    @NonNull
    @Override
    public <T extends ViewModel> T create(Class<T> modelClass) {
        if (modelClass.isAssignableFrom(UserViewModel.class)) {
            return (T) new UserViewModel();
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}
