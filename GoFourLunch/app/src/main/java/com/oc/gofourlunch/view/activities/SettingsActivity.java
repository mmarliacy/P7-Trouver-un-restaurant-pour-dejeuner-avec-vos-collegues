package com.oc.gofourlunch.view.activities;

import static android.app.PendingIntent.FLAG_UPDATE_CURRENT;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.switchmaterial.SwitchMaterial;
import com.oc.gofourlunch.R;
import com.oc.gofourlunch.controller.notifications.ReminderBroadcast;

import java.util.Calendar;

public class SettingsActivity extends AppCompatActivity {

    SwitchMaterial notificationToggleBtn;
    private SharedPreferences toggleBtnSharesPref;
    private SharedPreferences.Editor fEditor;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        notificationToggleBtn = findViewById(R.id.notification_btn);
        retrieveToggleStateButton();
        notificationToggleBtn.setOnClickListener(v -> {
            if(notificationToggleBtn.isChecked()){
                saveToggleButtonState();
                createNotificationChannel();
                setNotifications();
                notificationToggleBtn.setText(R.string.Notifications_enabled);
                Toast.makeText(SettingsActivity.this, "Notifications On", Toast.LENGTH_SHORT).show();
            } else {
                noSaveToggleButtonState();
                Toast.makeText(SettingsActivity.this, "Notifications Off", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveToggleButtonState(){
        toggleBtnSharesPref = getSharedPreferences("togglePref", MODE_PRIVATE);
        fEditor = toggleBtnSharesPref.edit();
        fEditor.putBoolean("value", true);
        fEditor.commit();
        notificationToggleBtn.setChecked(true);
    }

    private void noSaveToggleButtonState(){
        toggleBtnSharesPref = getSharedPreferences("togglePref", MODE_PRIVATE);
        fEditor = toggleBtnSharesPref.edit();
        fEditor.putBoolean("value", false);
        fEditor.commit();
        notificationToggleBtn.setChecked(false);
    }
    private void retrieveToggleStateButton(){
        toggleBtnSharesPref = getSharedPreferences("togglePref", MODE_PRIVATE);
        notificationToggleBtn.setChecked(toggleBtnSharesPref.getBoolean("value", false));

    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("LunchChannel", name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void setNotifications() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 12);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        Intent intent = new Intent(SettingsActivity.this, ReminderBroadcast.class);
        @SuppressLint("UnspecifiedImmutableFlag")
        PendingIntent pendingIntent = PendingIntent.getBroadcast(SettingsActivity.this, 0, intent, FLAG_UPDATE_CURRENT);

        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 5000, pendingIntent);
      //  alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);
    }

}