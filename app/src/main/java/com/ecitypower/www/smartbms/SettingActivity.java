package com.ecitypower.www.smartbms;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

/**
 * Created by Fangming on 11/20/16.
 */

public class SettingActivity extends Fragment {
    private int toggle = 0;
    public static SettingActivity newInstance() {

        SettingActivity settingFragment = new SettingActivity();
//        Bundle b = new Bundle();
//        b.putString("msg", text);
//        f.setArguments(b);

        return settingFragment;
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View statusView = inflater.inflate(R.layout.activity_setting, container, false);

        Log.i("debug","tab: Setting");
//        Utils.loadPreferences(this);

        Button ledButton = (Button) statusView.findViewById(R.id.button);
        ledButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                if (toggle == 0){
                    ((TabBarActivity)getActivity()).LEDControl("1");
                    toggle = 1;
                }
                else {
                    ((TabBarActivity)getActivity()).LEDControl("0");
                    toggle = 0;
                }
            }
        });

        return statusView;
    }

}
