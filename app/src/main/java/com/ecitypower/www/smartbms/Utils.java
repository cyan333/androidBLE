package com.ecitypower.www.smartbms;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

import java.util.HashMap;

/**
 * Created by Fangming on 11/23/16.
 */

public class Utils{

    public static final String PREFS_NAME = "preferences";
    public static final String PREF_BLEADDRESS = "Address";
    public static final String PREF_BLENAME = "Name";
    public static final String PREF_NOTIFICATION_STATUS = "Notification";

//    private static final String DefaultAdress = "";
//
//    private static final String DefaultName = "";

    public static void saveConnectedDevice (String deviceAddress, String deviceName, Activity activity) {
        SharedPreferences settings = activity.getSharedPreferences(PREFS_NAME,
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();

        // Edit and commit

        editor.putString(PREF_BLEADDRESS, deviceAddress);
        editor.putString(PREF_BLENAME, deviceName);

        editor.commit();
    }


    public static HashMap<String,String> loadConnectedDevice(Activity activity) {

        SharedPreferences settings = activity.getSharedPreferences(PREFS_NAME,
                Context.MODE_PRIVATE);

        HashMap<String,String> savedAddressName = new HashMap<>();
        // Get value

        savedAddressName.put(PREF_BLEADDRESS, settings.getString(PREF_BLEADDRESS, ""));
        savedAddressName.put(PREF_BLENAME, settings.getString(PREF_BLENAME, ""));

        return savedAddressName;
    }

    /////////Notification//////////////

    public static void saveNotificationStatus (boolean notificationStatus, Activity activity){
        SharedPreferences settings = activity.getSharedPreferences(PREFS_NAME,
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();

        editor.putBoolean(PREF_NOTIFICATION_STATUS, notificationStatus);

        editor.commit();
    }

    public static boolean loadNotificationStatus (Activity activity ){
        SharedPreferences settings = activity.getSharedPreferences(PREFS_NAME,
                Context.MODE_PRIVATE);

        return settings.getBoolean(PREF_NOTIFICATION_STATUS, true);

        // Get value
    }

    public static void sendNotification (Activity activity, String contentTitle, String contentText, int iconImage){
        Intent intent = new Intent();
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(activity)
                        .setSmallIcon(iconImage)
                        .setContentTitle(contentTitle)
                        .setContentText(contentText);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(activity);
        stackBuilder.addParentStack(activity);
        stackBuilder.addNextIntent(intent);

        PendingIntent pIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        mBuilder.setContentIntent(pIntent);
        NotificationManager mNotificationManager =
                (NotificationManager) activity.getSystemService(Context.NOTIFICATION_SERVICE);
        // mId allows you to update the notification later on.
        mNotificationManager.notify(0, mBuilder.build());
    }

    /////////Parameter//////////////



}
