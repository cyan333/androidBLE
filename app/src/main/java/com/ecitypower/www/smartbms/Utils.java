package com.ecitypower.www.smartbms;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashMap;

/**
 * Created by Fangming on 11/23/16.
 */

public class Utils{

    public static final String PREFS_NAME = "preferences";
    public static final String PREF_BLEADDRESS = "Address";
    public static final String PREF_BLENAME = "Name";

//    private static final String DefaultAdress = "";
//
//    private static final String DefaultName = "";

    public static void savePreferences(String deviceAddress, String deviceName, Activity activity) {
        SharedPreferences settings = activity.getSharedPreferences(PREFS_NAME,
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();

        // Edit and commit

        editor.putString(PREF_BLEADDRESS, deviceAddress);
        editor.putString(PREF_BLENAME, deviceName);

        editor.commit();
    }

    public static HashMap<String,String> loadPreferences(Activity activity) {

        SharedPreferences settings = activity.getSharedPreferences(PREFS_NAME,
                Context.MODE_PRIVATE);

        HashMap<String,String> savedAddressName = new HashMap<>();
        // Get value

        savedAddressName.put(PREF_BLEADDRESS, settings.getString(PREF_BLEADDRESS, ""));
        savedAddressName.put(PREF_BLENAME, settings.getString(PREF_BLENAME, ""));

        return savedAddressName;
    }
}
