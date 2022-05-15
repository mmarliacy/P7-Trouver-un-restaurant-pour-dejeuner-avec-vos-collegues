package com.oc.gofourlunch.view.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;

import com.facebook.FacebookSdk;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract;
import com.firebase.ui.auth.IdpResponse;
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.oc.gofourlunch.R;

import java.util.Arrays;
import java.util.List;

public class LoginActivity extends AppCompatActivity {

    //----------
    // FOR DATA
    //----------
    private static final String TAG = "Problem in : " + LoginActivity.class.getName();

    //---------------------------
    // ON-CREATE : LOGIN ACTIVITY
    //---------------------------
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.setClientToken(String.valueOf(R.string.facebook_app_id));
        FacebookSdk.sdkInitialize(this.getApplicationContext());
        loginProcess();
    }

    //-----------------------
    // AUTHENTICATION'S USER
    //-----------------------
    private void loginProcess() {
        //--:: 1 -- Choose authentication providers ::-->
        List<AuthUI.IdpConfig> varProviders = Arrays.asList(
                new AuthUI.IdpConfig.GoogleBuilder().build(),
                new AuthUI.IdpConfig.FacebookBuilder().build(),
                new AuthUI.IdpConfig.EmailBuilder().build(),
                new AuthUI.IdpConfig.TwitterBuilder().build());

        //--:: 2 -- Create and launch sign-in intent ::-->
        Intent signInIntent = AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setTheme(R.style.LoginTheme)
                .setAvailableProviders(varProviders)
                .setIsSmartLockEnabled(false, true)
                .setLogo(R.drawable.starter_logo)
                .build();
        signInLauncher.launch(signInIntent);
    }

    //--:: 3 -- Get the sign-in result ::-->
    private final ActivityResultLauncher<Intent> signInLauncher = registerForActivityResult(
            new FirebaseAuthUIActivityResultContract(),
            this::onSignInResult
    );

    //--:: 4 -- Handle the result ::-->
    private void onSignInResult(FirebaseAuthUIAuthenticationResult result) {
        IdpResponse response = result.getIdpResponse();
        if (result.getResultCode() == RESULT_OK) {
            //--::> Successfully signed in
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            assert FirebaseAuth.getInstance().getCurrentUser() != null;
            Uri userPhoto = FirebaseAuth.getInstance().getCurrentUser().getPhotoUrl();
            Intent loginIntent = new Intent(this, MainActivity.class);
            loginIntent.putExtra("user", user);
            loginIntent.putExtra("userPhoto", userPhoto);
            startActivity(loginIntent);
            //--::> Otherwise...
        } else {
            Toast.makeText(this, R.string.cancelled_authentication, Toast.LENGTH_SHORT).show();
            if (response != null && response.getError() != null) {
                Log.e(TAG, getString(R.string.cancelled_authentication_because_of) + result.toString());
            }
        }
    }
}