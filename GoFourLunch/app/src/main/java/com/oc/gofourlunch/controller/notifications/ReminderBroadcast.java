package com.oc.gofourlunch.controller.notifications;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.facebook.login.Login;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.oc.gofourlunch.R;
import com.oc.gofourlunch.model.User.User;
import com.oc.gofourlunch.view.activities.LoginActivity;
import com.oc.gofourlunch.view.activities.YourLunch;

import java.util.List;

public class ReminderBroadcast extends BroadcastReceiver {

    //----------
    // FOR DATA
    //----------
    String textContent;
    public String restaurantName;
    public String restaurantAddress;
    List<String> registeredCoworkers;

    @Override
    public void onReceive(Context context, Intent intent) {

        //--:: 1 -- Get data about current User meeting lunch on Firestore database ::-->
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference collectionReference = db.collection("Users");
        FirebaseUser userConnected = FirebaseAuth.getInstance().getCurrentUser();
        assert userConnected != null;
        collectionReference.get().addOnSuccessListener(pQueryDocumentSnapshots -> {
            for (QueryDocumentSnapshot varQueryDocumentSnapshot : pQueryDocumentSnapshots) {
                User user = varQueryDocumentSnapshot.toObject(User.class);
                if (user.getName().equals(userConnected.getDisplayName())) {
                    restaurantName = user.getRestaurantName();
                    restaurantAddress = user.getRestaurantAddress();
                    registeredCoworkers = user.getSubscribedCoworkers();
                }
            }
            //--:: 2 -- Set up notifications content parameters ::-->
            textContent = context.getString(R.string.Meet_your_coworkers_at) + restaurantName + ", " + restaurantAddress + context.getString(R.string.with_notifications) + registeredCoworkers.toString().substring(1, registeredCoworkers.toString().length() - 1);
            String textTitle = "Your Lunch";
            String CHANNEL_ID = "LunchChannel";

            //--:: 3 -- Set up  notifications extras settings ::-->
            Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            long time = System.currentTimeMillis();

            //--:: 4 -- Configure pending Intent for handling notifications reception ::-->
            Intent notificationIntent = new Intent(context, LoginActivity.class);
            notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            @SuppressLint("UnspecifiedImmutableFlag")
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0,
                    notificationIntent, 0);

            //--:: 5 -- Build NotificationCompat.Builder object to create notification ::-->
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(R.drawable.notification_icon)
                    .setContentTitle(textTitle)
                    .setContentText(textContent)
                    .setContentIntent(pendingIntent)
                    .setSound(alarmSound)
                    .setAutoCancel(true).setWhen(time).setDefaults(NotificationCompat.DEFAULT_ALL)
                    .setStyle(new NotificationCompat.BigTextStyle()
                            .bigText(textContent))
                    .setPriority(NotificationCompat.PRIORITY_HIGH);
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
            notificationManager.notify(123, builder.build());
        });
    }
}


