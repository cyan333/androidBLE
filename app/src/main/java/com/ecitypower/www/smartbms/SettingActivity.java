package com.ecitypower.www.smartbms;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;

import java.util.HashMap;

import static com.ecitypower.www.smartbms.R.id.setting_list;

/**
 * Created by Fangming on 11/20/16.
 */

public class SettingActivity extends Fragment {
    private int toggle = 0;

    private SettingListAdapter mSettingListAdapter;

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

//        Button ledButton = (Button) statusView.findViewById(R.id.button);
//        ledButton.setOnClickListener(new View.OnClickListener() {
//            public void onClick(View v) {
//
//                if (toggle == 0){
//                    ((TabBarActivity)getActivity()).LEDControl("1");
//                    toggle = 1;
//                }
//                else {
//                    ((TabBarActivity)getActivity()).LEDControl("0");
//                    toggle = 0;
//                }
//            }
//        });

        mSettingListAdapter = new SettingListAdapter();
        ListView settingList = (ListView)statusView.findViewById(setting_list);
        settingList.setAdapter(mSettingListAdapter);

        return statusView;
    }


    ////////////////////////////////////////////////////////////////////
    ///////////////////////Status List - List Device////////////////////
    /////////////////////////////List all the data /////////////////////
    ////////////////////////////////////////////////////////////////////
    private class SettingListAdapter extends BaseAdapter {
        private HashMap<String, String> deviceData;
        private LayoutInflater mInflator;

        public SettingListAdapter() {
            super();
            deviceData = new HashMap<>();
            mInflator = getActivity().getLayoutInflater();
        }

        @Override
        public int getCount() {
            return 11;
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int position, View view, ViewGroup viewGroup) {
            switch (position) {
                default:
                    if (view == null){
                        view = mInflator.inflate(R.layout.listitem_section, viewGroup, false);
                    }
                    return view;
                case 1:
                    TextView settingTitle_currentDevice;
                    TextView settingData;
                    if (view == null){
                        view = mInflator.inflate(R.layout.listitem_text_text, viewGroup, false);
                    }
                    settingTitle_currentDevice = (TextView) view.findViewById(R.id.settingTitle);
                    settingTitle_currentDevice.setText("Current device");

                    settingData = (TextView) view.findViewById(R.id.settingData);
                    settingData.setText("device name");
                    return view;
                case 2:
                    TextView settingTitle_notification;
                    if (view == null){
                        view = mInflator.inflate(R.layout.listitem_text_switch, viewGroup, false);
                    }
                    settingTitle_notification = (TextView) view.findViewById(R.id.settingTitle);
                    settingTitle_notification.setText("Notification");
                    return view;

                case 4:
                    TextView settingTitle_language;
                    if (view == null){
                        view = mInflator.inflate(R.layout.listitem_text_arrow, viewGroup, false);
                    }
                    settingTitle_language = (TextView) view.findViewById(R.id.settingTitle);
                    settingTitle_language.setText("Language");
                    return view;
                case 6:
                    TextView settingTitle_LikeUsOnFB;
                    if (view == null){
                        view = mInflator.inflate(R.layout.listitem_text_arrow, viewGroup, false);
                    }
                    settingTitle_language = (TextView) view.findViewById(R.id.settingTitle);
                    settingTitle_language.setText("Like us on facebook");
                    return view;
                case 7:
                    TextView settingTitle_HelpAndFeedback;
                    if (view == null){
                        view = mInflator.inflate(R.layout.listitem_text_arrow, viewGroup, false);
                    }
                    settingTitle_language = (TextView) view.findViewById(R.id.settingTitle);
                    settingTitle_language.setText("Help & Feedback");
                    return view;
                case 8:
                    TextView settingTitle_about;
                    if (view == null){
                        view = mInflator.inflate(R.layout.listitem_text_arrow, viewGroup, false);
                    }
                    settingTitle_language = (TextView) view.findViewById(R.id.settingTitle);
                    settingTitle_language.setText("About us");
                    return view;

                case 10: //led
                    TextView settingTitle;
                    Switch ledSW;
                    if (view == null){
                        view = mInflator.inflate(R.layout.listitem_text_switch, viewGroup, false);
                    }

                    settingTitle = (TextView) view.findViewById(R.id.settingTitle);
                    settingTitle.setText("LED On/Off");

                    ledSW = (Switch) view.findViewById(R.id.settingSwitch);
                    ledSW.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                            if (isChecked){
                                ((TabBarActivity)getActivity()).LEDControl("1");
                            }
                            else {
                                ((TabBarActivity)getActivity()).LEDControl("0");
                            }
                        }
                    });

                    return view;
            }


        }
    }
}
