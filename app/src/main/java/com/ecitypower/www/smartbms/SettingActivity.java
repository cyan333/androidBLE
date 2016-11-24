package com.ecitypower.www.smartbms;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

/**
 * Created by Fangming on 11/20/16.
 */

public class SettingActivity extends Activity {
    /** Called when the activity is first created. */

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        Log.i("debug","tab: Setting");
        Utils.loadPreferences(this);
    }
}
