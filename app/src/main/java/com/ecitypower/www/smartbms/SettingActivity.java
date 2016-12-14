package com.ecitypower.www.smartbms;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;


/**
 * Created by Fangming on 11/20/16.
 */

public class SettingActivity extends Fragment {
    private int toggle = 0;

    private SettingListAdapter mSettingListAdapter;

    private String deviceName;
    private String deviceAddress;

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

        deviceName = "Loading";
        deviceAddress = "Loading";
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
        ListView settingList = (ListView)statusView.findViewById(R.id.setting_list);
        settingList.setAdapter(mSettingListAdapter);

        settingList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                Log.i("debug", "position:" + position);

//                if (position == 12){
//                    Log.i("debug", "position12 clicked");
//                    Intent intent = new Intent();
//
//                    NotificationCompat.Builder mBuilder =
//                            new NotificationCompat.Builder(getActivity())
//                                    .setSmallIcon(R.drawable.full_battery)
//                                    .setContentTitle("My notification")
//                                    .setContentText("Hello World!");
//
//                    TaskStackBuilder stackBuilder = TaskStackBuilder.create(getActivity());
//                    stackBuilder.addParentStack(getActivity());
//                    stackBuilder.addNextIntent(intent);
//
//                    PendingIntent pIntent =
//                            stackBuilder.getPendingIntent(
//                                    0,
//                                    PendingIntent.FLAG_UPDATE_CURRENT
//                            );
//                    mBuilder.setContentIntent(pIntent);
//                    NotificationManager mNotificationManager =
//                            (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
//                    // mId allows you to update the notification later on.
//                    mNotificationManager.notify(0, mBuilder.build());
//                }
                if (position == 5){
                    String url = "http://www.ecitypower.com";
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse(url));
                    startActivity(i);
                }

                else if (position == 6) {
                    final Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
                    emailIntent.setType("text/plain");
                    emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[] { "sxie@wpi.edu" });
                    emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "[Smart BMS Feedback]");

                    emailIntent.setType("message/rfc822");

                    try {
                        startActivity(Intent.createChooser(emailIntent,
                                "Send email using..."));
                    } catch (android.content.ActivityNotFoundException ex) {
                        Toast.makeText(getActivity(),
                                "No email clients installed.",
                                Toast.LENGTH_SHORT).show();
                    }
                }
                else if (position == 7){
                    String url = "http://www.ecitypower.com";
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse(url));
                    startActivity(i);
                }
            }
        });

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
            return 13;
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
                    TextView settingTitle_DeviceName;
                    TextView settingData_DeviceName;
                    if (view == null){
                        view = mInflator.inflate(R.layout.listitem_text_text, viewGroup, false);
                    }
                    settingTitle_DeviceName = (TextView) view.findViewById(R.id.settingTitle);
                    settingTitle_DeviceName.setText("Device Name");

                    settingData_DeviceName = (TextView) view.findViewById(R.id.settingData);
                    settingData_DeviceName.setText(deviceName);
                    return view;

                case 2:
                    TextView settingTitle_DeviceAddress;
                    TextView settingData_DeviceAddress;
                    if (view == null){
                        view = mInflator.inflate(R.layout.listitem_text_text, viewGroup, false);
                    }
                    settingTitle_DeviceAddress = (TextView) view.findViewById(R.id.settingTitle);
                    settingTitle_DeviceAddress.setText("Device Address");

                    settingData_DeviceAddress = (TextView) view.findViewById(R.id.settingData);
                    settingData_DeviceAddress.setText(deviceAddress);

                    return view;

                case 3:
                    TextView settingTitle_notification;
                    Switch notificationSW;
                    if (view == null){
                        view = mInflator.inflate(R.layout.listitem_text_switch, viewGroup, false);
                    }
                    settingTitle_notification = (TextView) view.findViewById(R.id.settingTitle);
                    settingTitle_notification.setText("Notification");

                    notificationSW = (Switch) view.findViewById(R.id.settingSwitch);

                    //load notification status
                    notificationSW.setChecked(Utils.loadNotificationStatus(getActivity()));

                    //save notification status
                    notificationSW.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                            if (isChecked){
                                Utils.saveNotificationStatus(true, getActivity());
                            }
                            else {
                                Utils.saveNotificationStatus(false, getActivity());
                            }
                        }
                    });

                    return view;

                case 5:
                    TextView settingTitle_LikeUsOnFB;
                    if (view == null){
                        view = mInflator.inflate(R.layout.listitem_text_arrow, viewGroup, false);
                    }
                    settingTitle_LikeUsOnFB = (TextView) view.findViewById(R.id.settingTitle);
                    settingTitle_LikeUsOnFB.setText("Like us on facebook");
                    return view;

                case 6:
                    TextView settingTitle_HelpAndFeedback;
                    if (view == null){
                        view = mInflator.inflate(R.layout.listitem_text_arrow, viewGroup, false);
                    }
                    settingTitle_HelpAndFeedback = (TextView) view.findViewById(R.id.settingTitle);
                    settingTitle_HelpAndFeedback.setText("Help & Feedback");
                    return view;

                case 7:
                    TextView settingTitle_about;
                    if (view == null){
                        view = mInflator.inflate(R.layout.listitem_text_arrow, viewGroup, false);
                    }
                    settingTitle_about = (TextView) view.findViewById(R.id.settingTitle);
                    settingTitle_about.setText("About us");
                    return view;

                case 9:
                    TextView settingTitle_disconnect;
                    if (view == null){
                        view = mInflator.inflate(R.layout.listitem_center_text, viewGroup, false);
                    }
                    settingTitle_disconnect = (TextView) view.findViewById(R.id.settingTitle);
                    settingTitle_disconnect.setText("Disconnect Bluetooth Device");
                    return view;

                case 11: //led
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

                case 12:
                    TextView settingTitle_notification_test;
                    if (view == null){
                        view = mInflator.inflate(R.layout.listitem_text_arrow, viewGroup, false);
                    }
                    settingTitle_notification_test = (TextView) view.findViewById(R.id.settingTitle);
                    settingTitle_notification_test.setText("Test Notification");


                    return view;

            }


        }
    }

    ////////////////////////////////////////////////////////////////////
    ///////////////////////save address and name////////////////////////
    ////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////
    public void saveDeviceAddressandName (String connectedDeviceName, String connectedDeviceAddress){
        deviceAddress = connectedDeviceAddress;
        deviceName = connectedDeviceName;
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mSettingListAdapter.notifyDataSetChanged();
                Log.i("debug", "notify change device address" + deviceAddress);
            }
        });


    }
}
