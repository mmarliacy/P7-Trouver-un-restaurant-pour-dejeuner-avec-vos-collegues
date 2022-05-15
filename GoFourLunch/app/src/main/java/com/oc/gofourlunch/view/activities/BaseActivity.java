package com.oc.gofourlunch.view.activities;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewbinding.ViewBinding;

abstract class BaseActivity<T extends ViewBinding> extends AppCompatActivity {

    //----------
    // FOR DATA
    //----------
    abstract T getViewBinding();
    protected T binding;

    //---------------------------
    // ON-CREATE : BASE ACTIVITY
    //---------------------------
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initBinding();
    }

    //--:: Initialize the binding object and the layout of the activity ::--
    private void initBinding(){
        binding = getViewBinding();
        View view = binding.getRoot();
        setContentView(view);
    }
}
