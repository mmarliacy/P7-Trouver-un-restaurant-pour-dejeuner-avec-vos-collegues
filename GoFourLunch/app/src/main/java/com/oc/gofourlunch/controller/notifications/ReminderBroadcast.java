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

import com.oc.gofourlunch.R;
import com.oc.gofourlunch.view.activities.MainActivity;
import com.oc.gofourlunch.view.activities.SettingsActivity;

public class ReminderBroadcast extends BroadcastReceiver {


    static String textContent;
    @Override
    public void onReceive(Context context, Intent intent) {
        long time = System.currentTimeMillis();
        if(MainActivity.usersList != null){
            textContent = context.getString(R.string.Meet_your_coworkers_at) + MainActivity.name +", " + MainActivity.address + context.getString(R.string.with) + MainActivity.usersList.toString().substring(1, MainActivity.usersList.toString().length() - 1);
        } else {
            textContent = context.getString(R.string.Meet_your_coworkers_at) + MainActivity.name +", " + MainActivity.address;
        }
        String textTitle = "Your Lunch";
        String CHANNEL_ID = "LunchChannel";


        Intent notificationIntent = new Intent(context, SettingsActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        @SuppressLint("UnspecifiedImmutableFlag") PendingIntent pendingIntent = PendingIntent.getActivity(context, 0,
                notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);


        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.notification_icon)
                .setContentTitle(textTitle)
                .setContentText(textContent)
                .setContentIntent(pendingIntent)
                .setSound(alarmSound)
                .setAutoCancel(true).setWhen(time)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(textContent))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(200, builder.build());
    }

}
