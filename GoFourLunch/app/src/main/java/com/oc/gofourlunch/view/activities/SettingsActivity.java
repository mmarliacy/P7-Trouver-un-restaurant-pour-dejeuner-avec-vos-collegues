package com.oc.gofourlunch.view.activities;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.SetOptions;
import com.oc.gofourlunch.R;
import com.oc.gofourlunch.controller.notifications.ReminderBroadcast;
import com.oc.gofourlunch.model.User.User;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;

public class SettingsActivity extends AppCompatActivity {

    //-------------
    // FOR DESIGN
    //-------------
    SwitchMaterial notificationToggleBtn;
    //----------
    // FOR DATA
    //----------
    private String currentUser_restaurantId;
    private SharedPreferences.Editor toggleEditor;
    public List<String> registeredCoworkers = new ArrayList<>();

    //-------------------------------
    // ON-CREATE : SETTINGS ACTIVITY
    //-------------------------------
    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        getToggleBtnState();
        onOrOffNotificationToggleButtonPressed();
    }

    //--------------------------------
    //  NOTIFICATIONS - TOGGLE BUTTON
    //--------------------------------
    //--:: 1 -- get Toggle button State ::-->
    private void getToggleBtnState() {
        notificationToggleBtn = findViewById(R.id.notification_btn);
        SharedPreferences toggleBtnSharesPref = getSharedPreferences("togglePref", MODE_PRIVATE);
        notificationToggleBtn.setChecked(toggleBtnSharesPref.getBoolean("toggle", false));
        if(toggleBtnSharesPref.getBoolean("toggle", false)) {
            notificationToggleBtn.setText(R.string.Notifications_enabled);
        } else {
            notificationToggleBtn.setText(R.string.notifications_disabled);
        }
    }

    //--:: 2 -- Handle if toggle button is pressed or not ::-->
    private void onOrOffNotificationToggleButtonPressed() {
        notificationToggleBtn.setOnClickListener(v -> {
            if (notificationToggleBtn.isChecked()) {
                toggleButtonChecked();
                getCurrentUserRestaurantId();
                createNotificationChannel();
                setNotificationAlarm();
            } else {
                toggleButtonNotChecked();
            }
        });
    }

    //--:: 3 -- Save toggle button state as "On" when user enter in app ::-->
    private void toggleButtonChecked() {
        toggleEditor = getSharedPreferences("togglePref", MODE_PRIVATE).edit();
        toggleEditor.putBoolean("toggle", true);
        toggleEditor.apply();
        notificationToggleBtn.setChecked(true);
        notificationToggleBtn.setText(R.string.Notifications_enabled);
    }

    //--:: 4 -- Save toggle button state as "Off" when user enter in app ::-->
    private void toggleButtonNotChecked() {
        toggleEditor = getSharedPreferences("togglePref", MODE_PRIVATE).edit();
        toggleEditor.putBoolean("toggle", false);
        toggleEditor.apply();
        notificationToggleBtn.setChecked(false);
        notificationToggleBtn.setText(R.string.notifications_disabled);
    }

    //-----------------------
    //  NOTIFICATIONS - DATA
    //-----------------------
    //--:: 1 -- Create followers coworkers list that will go on the same restaurant, to set in notification ::-->
    public void getCurrentUserRestaurantId() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference collectionReference = db.collection("Users");
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        assert currentUser != null;
        DocumentReference varDocumentReference = collectionReference.document(Objects.requireNonNull(currentUser.getDisplayName()));
        varDocumentReference.get().addOnSuccessListener(pDocumentSnapshot -> {
            if (pDocumentSnapshot.exists()) {
                currentUser_restaurantId = pDocumentSnapshot.getString("restaurantId");
                collectionReference.get().addOnSuccessListener(pQueryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot queryDocumentSnapshot : pQueryDocumentSnapshots) {
                        User user = queryDocumentSnapshot.toObject(User.class);
                        String restaurantId = user.getRestaurantId();
                        String userName = user.getName();
                        if (!Objects.requireNonNull(currentUser.getDisplayName()).equals(userName)) {
                            if (restaurantId.equals(currentUser_restaurantId)) {
                                registeredCoworkers.add(userName);
                            }
                        }
                        setFollowersToCurrentUser();
                    }
                });
            }
        });
    }

    //--:: 2 -- Attribute the coworkers list to the connected user, in Firestore Database ::-->
    public void setFollowersToCurrentUser() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference collectionReference = db.collection("Users");
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        collectionReference.get().addOnSuccessListener(pQueryDocumentSnapshots -> {
            for (QueryDocumentSnapshot queryDocumentSnapshot : pQueryDocumentSnapshots) {
                User user = queryDocumentSnapshot.toObject(User.class);
                assert currentUser != null;
                if (user.getName().equals(currentUser.getDisplayName())) {
                    user.setSubscribedCoworkers(registeredCoworkers);
                    collectionReference.document(user.getName()).set(user, SetOptions.merge());
                }
            }
        });
    }

    //-------------------------------
    //  NOTIFICATIONS - CONFIGURATION
    //--------------------------------
    //--:: 1 -- Create the NotificationChannel object ::-->
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel("LunchChannel", name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    //--:: 2 -- Set Notification Alarm repeatedly ::-->
    private void setNotificationAlarm() {
        Intent intent = new Intent(SettingsActivity.this, ReminderBroadcast.class);
        @SuppressLint("UnspecifiedImmutableFlag")
        PendingIntent pendingIntent = PendingIntent.getBroadcast(SettingsActivity.this, 0, intent, 0);
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 13);
        calendar.set(Calendar.MINUTE, 37);
        calendar.set(Calendar.SECOND, 0);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);
        //alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 10000, pendingIntent);
    }

}