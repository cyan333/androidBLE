package com.ecitypower.www.smartbms;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.lzyzsd.circleprogress.ArcProgress;
import com.github.ybq.android.spinkit.SpinKitView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import static android.content.ContentValues.TAG;
import static android.content.Context.BLUETOOTH_SERVICE;

//import com.github.lzyzsd.circleprogress.ArcProgress;

//import com.akexorcist.roundcornerprogressbar.RoundCornerProgressBar;

/**
 * Created by Fangming on 11/20/16.
 */

public class StatusActivity extends Fragment {
//    public static BluetoothGatt gatt;

    /* GATT */
    private BluetoothGatt gatt;

    /* Service UUID */
    private static final UUID BLE_SERVICE_UUID = UUID.fromString("0000ffe0-0000-1000-8000-00805f9b34fb");

    /* Characteristic UUID */
    private static final UUID BLE_CHAR_UUID = UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb");

    /* Descriptor */
    private static final UUID BLE_DESCRIPTOR_1_UUID = UUID.fromString("00002901-0000-1000-8000-00805f9b34fb");
    private static final UUID BLE_DESCRIPTOR_2_UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

    /* Fully Charged Voltage */
    private static final int FULLY_CHARGED_VOLTAGE = 1;

    private BluetoothGattCharacteristic characteristic;

    private BluetoothAdapter mBluetoothAdapter;
    private Handler mHandler;
    private LeDeviceListAdapter mLeDeviceListAdapter;

    /* Buttons */
    private Button scanButton;
//    private Button LEDButton;

    /* GATT */
//    private BluetoothGatt mConnectedGatt;

    /* Alert Diaglo */
    private AlertDialog BLEAScanAlertDialog;

    private int toggle = 0;

    //define Final Variable
    private static final long SCAN_PERIOD = 10000;

    private BluetoothDevice connectedDevice;

    private TextView connectionFail;

    private String savedAddress;

    private SpinKitView loading;
    private View loadingBg;

//    private RoundCornerProgressBar voltageProgressBar;
    private ArcProgress voltageProgressBar;

    private float maxVoltage;
    private float minVoltage;

    private TextView voltagePercentage;

    /* Data */
    private StatusListAdapter mStatusListAdapter;

    public static StatusActivity newInstance() {

        StatusActivity f = new StatusActivity();
//        Bundle b = new Bundle();
//        b.putString("msg", text);
//        f.setArguments(b);

        return f;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
//    @Override
//    public void onCreate(Bundle savedInstanceState) {
        View statusView = inflater.inflate(R.layout.activity_status, container, false);

        /* Initialize */
        mHandler = new Handler();
        mLeDeviceListAdapter = new LeDeviceListAdapter();

        loading = (SpinKitView) statusView.findViewById(R.id.loading);
        loading.setVisibility(View.INVISIBLE);

        loadingBg = (View) statusView.findViewById(R.id.loadingBg);
        loadingBg.setVisibility(View.INVISIBLE);
        loadingBg.setOnClickListener(new View.OnClickListener() {
                                         @Override
                                         public void onClick(View v) {
                                         }
                                     }
        );

        //Bluetooth permission request.
        if (ContextCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[] { android.Manifest.permission.ACCESS_COARSE_LOCATION }, 1);
        }

        // Use this check to determine whether BLE is supported on the device.  Then you can
        // selectively disable BLE-related features.
        if (!getActivity().getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(getActivity(), R.string.BLE_not_supported, Toast.LENGTH_SHORT).show();
            getActivity().finish();
        }
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getActivity().getSystemService(BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        // Checks if Bluetooth is supported on the device.
        if (mBluetoothAdapter == null) {
            Toast.makeText(getActivity(), R.string.BLE_not_supported, Toast.LENGTH_SHORT).show();
            getActivity().finish();
//            return;
        }

        //////////////////////////
        ///////Alert Dialog///////
        //////////////////////////
        AlertDialog.Builder BLEAScanAlertDialogBuilder = new AlertDialog.Builder(getActivity());

        BLEAScanAlertDialog = BLEAScanAlertDialogBuilder
                .setView(makeAlertDialog())
                .setTitle(R.string.Select_Bluetooth_Device)
                .setPositiveButton(R.string.Scan, null) //Set to null. We override the onclick
                .setNegativeButton(R.string.Cancel, null)
                .setCancelable(false)
                .create();

        //Alert Dialog --> Scan Button
        BLEAScanAlertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {

                scanButton = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
                scanButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        scanButton.setText(R.string.Scanning);
                        scanButton.setEnabled(false);
                        scanLeDevice();
//                        d.dismiss();
                        savedAddress = "";

                        Log.i("debug","CLicked!!!");

                    }
                });
            }
        });
        ////////////////////////////////////////////////////////
        ///////////Save and Load Saved Device Address///////////
        ////////////////////////////////////////////////////////

        HashMap<String, String> savedAddressName = Utils.loadConnectedDevice(getActivity());
        savedAddress = savedAddressName.get(Utils.PREF_BLEADDRESS);
        if (savedAddress.equals("")){
            BLEAScanAlertDialog.show();

        }
        else {
            loading.setVisibility(View.VISIBLE);
            loadingBg.setVisibility(View.VISIBLE);
            scanLeDevice();
        }

        mStatusListAdapter = new StatusListAdapter();
        ListView statusList = (ListView)statusView.findViewById(R.id.statusList);
        statusList.setAdapter(mStatusListAdapter);

        mStatusListAdapter.addData(getResources().getString(R.string.Voltage), getResources().getString(R.string.Loading));
        mStatusListAdapter.addData(getResources().getString(R.string.Cell_1_Voltage), "4.4V");
        mStatusListAdapter.addData(getResources().getString(R.string.Cell_2_Voltage), "3.4V");
        mStatusListAdapter.addData(getResources().getString(R.string.Cell_3_Voltage), "6.4V");
        mStatusListAdapter.addData(getResources().getString(R.string.Cell_4_Voltage), "7.4V");
        mStatusListAdapter.addData(getResources().getString(R.string.Cell_5_Voltage), "8.4V");
        mStatusListAdapter.addData(getResources().getString(R.string.Cell_6_Voltage), "9.4V");
        mStatusListAdapter.addData(getResources().getString(R.string.Cell_7_Voltage), "9.4V");
        mStatusListAdapter.addData(getResources().getString(R.string.Temperature), "9.4V");

//        voltageProgressBar = (RoundCornerProgressBar) statusView.findViewById(voltageProgressBar);
//        voltageProgressBar.setProgress(25);

        voltageProgressBar = (ArcProgress) statusView.findViewById(R.id.voltageProgressBar);
        voltageProgressBar.setProgress(25);

//        voltagePercentage = (TextView) statusView.findViewById((R.id.voltagePercentage));

        return statusView;
    }

    /////////Oncreate End////////////


    //////////////////////////////////////////////////
    ///////////Scan BLE Alert Dialog Layout///////////
    //////////////////////////////////////////////////
    private LinearLayout makeAlertDialog () {
        LinearLayout layout = new LinearLayout(getActivity());
        LinearLayout.LayoutParams parms = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setLayoutParams(parms);

        connectionFail = new TextView(getActivity());
        connectionFail.setText("");
        connectionFail.setTextColor(ContextCompat.getColor(getActivity(), R.color.colorWarningRed));
        connectionFail.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        connectionFail.setTextSize(15);

        //Alert Dialog --> List - BLE Device List
        ListView BLEDevicesList = new ListView(getActivity());
        BLEDevicesList.setAdapter(mLeDeviceListAdapter);

        //Device List Click Listener
        BLEDevicesList.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                mBluetoothAdapter.stopLeScan(mLeScanCallback);
                scanButton.setText(R.string.Scan);
                scanButton.setEnabled(true);
                connectedDevice = mLeDeviceListAdapter.getDevice(position);
                connectedDevice.connectGatt(getActivity(), false, mGattCallback);
//                gatt.disconnect();
            }
        });

        LinearLayout.LayoutParams connectionFailParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        connectionFailParams.topMargin = 10;
        connectionFailParams.bottomMargin = 5;
        layout.addView(connectionFail,connectionFailParams);
        layout.addView(BLEDevicesList);

        return layout;
    }

    ////////////////////////////////////
    ///////////Scan LE Device///////////
    ////////////////////////////////////

    private void scanLeDevice() {
        // Stops scanning after a pre-defined scan period.
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mBluetoothAdapter.stopLeScan(mLeScanCallback);
                if (savedAddress.equals("")){
                    scanButton.setText(R.string.Scan);
                    scanButton.setEnabled(true);
                }
                else if (connectedDevice == null) {
                    loading.setVisibility(View.INVISIBLE);
                    loadingBg.setVisibility(View.INVISIBLE);
                    BLEAScanAlertDialog.show();
                    connectionFail.setText(R.string.Cannot_find_previous_device);
//                    voltageProgressBar.setProgress(100);
                }

            }
        }, SCAN_PERIOD);

        Log.i("debug","SSSSSSStart scanning");
        mLeDeviceListAdapter.clear();
        mLeDeviceListAdapter.notifyDataSetChanged();
        mBluetoothAdapter.startLeScan(mLeScanCallback);
    }

    ///////////////////////////////
    ///// Device scan callback.////
    ///////////////////////////////
    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {
                @Override
                public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (device.getName() != null && savedAddress.equals("")) {
                                mLeDeviceListAdapter.addDevice(device);
                                mLeDeviceListAdapter.notifyDataSetChanged();
                            }
                            else if (device.getName() != null){
                                if (savedAddress.equals(device.getAddress())){
                                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
                                    loading.setVisibility(View.INVISIBLE);
                                    loadingBg.setVisibility(View.INVISIBLE);
                                    connectedDevice = device;
                                    device.connectGatt(getActivity(), false, mGattCallback);
                                }

                            }
                        }
                    });
                }
            };

    //////////////////////////////////
    ///////// GATT Call Back//////////
    //////////////////////////////////
    private BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                gatt.discoverServices();

                Log.i("debug", "Did connect device");

            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {

            Log.i("debug", "Connection Service State : "+ status);

            if (status != 0){
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        connectionFail.setText(R.string.Connection_Failed);
                    }
                });
            }
            else {
                characteristic = gatt.getService(BLE_SERVICE_UUID).getCharacteristic(BLE_CHAR_UUID);
                gatt.readCharacteristic(characteristic);
                StatusActivity.this.gatt = gatt;
                //save connected device for user
                Utils.saveConnectedDevice(connectedDevice.getAddress(),connectedDevice.getName(),getActivity());
                BLEAScanAlertDialog.dismiss();
                ((TabBarActivity)getActivity()).saveBLENameAddress(connectedDevice.getName(),connectedDevice.getAddress());
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {

            //Enable local notifications
            gatt.setCharacteristicNotification(characteristic, true);
            //Enabled remote notifications
            BluetoothGattDescriptor desc = characteristic.getDescriptor(BLE_DESCRIPTOR_1_UUID);
            desc.setValue(BluetoothGattDescriptor.ENABLE_INDICATION_VALUE);
            gatt.writeDescriptor(desc);
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
//            Log.i("debug","changed");
            final int voltage = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0);
//            Log.i("debug", Integer.toString(hi));
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mStatusListAdapter.addData(getResources().getString(R.string.Voltage), Integer.toString(voltage)+"V");
//                    voltageProgressBar.setProgress(voltageProgressBar.getProgress() + 10);
                    mStatusListAdapter.notifyDataSetChanged();

                    Log.i("debug", "Voltage" + voltage);
                    if (voltage == FULLY_CHARGED_VOLTAGE && Utils.loadNotificationStatus(getActivity())){
                        Utils.sendNotification(
                                getActivity(),
                                getResources().getString(R.string.Battery_Fully_Charged),
                                getResources().getString(R.string.Battery_Fully_Charged_Content),
                                R.drawable.full_battery);
                    }
                }
            });
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.i(TAG, "Callback: Wrote GATT Descriptor successfully.");
            }
            else{
                Log.i(TAG, "Callback: Error writing GATT Descriptor: "+ status);
            }
        };
    };

    //////////////////////////////////////////////////
    ///////////////////Write Data/////////////////////
    //////////////////////////////////////////////////
    public void writeData (String input){
        characteristic.setValue(input);
        gatt.writeCharacteristic(characteristic);
    }

    ///////////////////////////////////////////////////////
    ///////////////////Disconnect GATT/////////////////////
    ///////////////////////////////////////////////////////
    public void disconnectGATT (){

        final AlertDialog disconnectAlertDialog;
        AlertDialog.Builder disconnectAlertDialogBuilder = new AlertDialog.Builder(getActivity());

        disconnectAlertDialog = disconnectAlertDialogBuilder
                .setTitle(R.string.Are_you_sure)
                .setMessage(R.string.disconnect_warning)
                .setPositiveButton(R.string.OK, null)
                .setNegativeButton(R.string.Cancel, null)
                .setCancelable(false)
                .create();

        if (mBluetoothAdapter == null || gatt == null || connectedDevice == null){
            disconnectAlertDialog.show();
            return;
        }
        else {
            disconnectAlertDialog.setButton(DialogInterface.BUTTON_POSITIVE,
                    getResources().getString(R.string.Yes),
                    new DialogInterface.OnClickListener(){
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Utils.saveConnectedDevice("" , "" , getActivity());
                            gatt.disconnect();
                            connectedDevice = null;
                            disconnectAlertDialog.dismiss();
                            mLeDeviceListAdapter.clear();
                            mLeDeviceListAdapter.notifyDataSetChanged();
                            BLEAScanAlertDialog.show();
                        }
                    });

            disconnectAlertDialog.show();
        }

    }

    ////////////////////////////////////////////////////////////////////
    //////////////////////List Adaptor - List Device////////////////////
    //////////Adapter for holding devices found through scanning////////
    ////////////////////////////////////////////////////////////////////
    private class LeDeviceListAdapter extends BaseAdapter {
        private ArrayList<BluetoothDevice> mLeDevices;
        private LayoutInflater mInflator;

        public LeDeviceListAdapter() {
            super();
            mLeDevices = new ArrayList<BluetoothDevice>();
            mInflator = getActivity().getLayoutInflater();
        }

        public void addDevice(BluetoothDevice device) {
            if(!mLeDevices.contains(device)) {
                mLeDevices.add(device);
            }
        }

        public BluetoothDevice getDevice(int position) {
            return mLeDevices.get(position);
        }

        public void clear() {
            mLeDevices.clear();
        }

        @Override
        public int getCount() {
            return mLeDevices.size();
        }

        @Override
        public Object getItem(int i) {
            return mLeDevices.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int position, View view, ViewGroup viewGroup) {
            TextView deviceName;
            // General ListView optimization code.
            if (view == null) {
                view = mInflator.inflate(R.layout.listitem_device, viewGroup, false);
                deviceName = (TextView) view.findViewById(R.id.device_name);
                view.setTag(deviceName);
            } else {
                deviceName = (TextView) view.getTag();
            }

            BluetoothDevice device = mLeDevices.get(position);
            final String deviceNameStr = device.getName();
            if (deviceNameStr != null && deviceNameStr.length() > 0)
                deviceName.setText(deviceNameStr);
            else
                deviceName.setText(R.string.Unknown);
            return view;
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////Code Above: Configure BLE///////////////////////////////////////////////
    /////////////////////////////////////////Code Below: Handle Data////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    ////////////////////////////////////////////////////////////////////
    ///////////////////////Status List - List Device////////////////////
    /////////////////////////////List all the data /////////////////////
    ////////////////////////////////////////////////////////////////////
    private class StatusListAdapter extends BaseAdapter {
        private HashMap<String, String> deviceData;
        private LayoutInflater mInflator;

        public StatusListAdapter() {
            super();
            deviceData = new HashMap<>();
            mInflator = getActivity().getLayoutInflater();
        }

        public void addData(String key, String value) {
            deviceData.put(key,value);
        }

        public String getData(String key) {
            return deviceData.get(key);
        }

        public void clear() {
            deviceData.clear();
        }

        public String position2Key (int position){
            switch (position){
                default: return getResources().getString(R.string.Voltage);
                case 1: return getResources().getString(R.string.Cell_1_Voltage);
                case 2: return getResources().getString(R.string.Cell_2_Voltage);
                case 3: return getResources().getString(R.string.Cell_3_Voltage);
                case 4: return getResources().getString(R.string.Cell_4_Voltage);
                case 5: return getResources().getString(R.string.Cell_5_Voltage);
                case 6: return getResources().getString(R.string.Cell_6_Voltage);
                case 7: return getResources().getString(R.string.Cell_7_Voltage);
                case 8: return getResources().getString(R.string.Temperature);
            }
        }

        @Override
        public int getCount() {
            return deviceData.size();
        }

        @Override
        public Object getItem(int position) {
            return deviceData.get(position2Key(position));
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int position, View view, ViewGroup viewGroup) {
            switch (position){
                case 0:
                    TextView statusTitle1;
                    TextView statusValue1;
                    ImageView statusImage1;
//                    if (view == null){
                        view = mInflator.inflate(R.layout.listiem_data, viewGroup, false);
//                    }

                    statusTitle1 = (TextView) view.findViewById(R.id.statusTitle);
                    statusTitle1.setText(position2Key(position));

                    statusValue1 = (TextView) view.findViewById(R.id.statusValue);
                    statusValue1.setText(deviceData.get(position2Key(position)));

                    statusImage1 = (ImageView) view.findViewById(R.id.statusImage);
                    statusImage1.setImageResource(R.drawable.charged_battery    );

                    return view;

                default:
                    TextView statusTitle;
                    TextView statusValue;
                    ImageView statusImage;
                    if (view == null){
                        view = mInflator.inflate(R.layout.listiem_data, viewGroup, false);
                    }

                    statusTitle = (TextView) view.findViewById(R.id.statusTitle);
                    statusTitle.setText(position2Key(position));

                    statusValue = (TextView) view.findViewById(R.id.statusValue);
                    statusValue.setText(deviceData.get(position2Key(position)));

                    statusImage = (ImageView) view.findViewById(R.id.statusImage);

                    int[] statusIcon = {
                            R.drawable.num1,
                            R.drawable.num2,
                            R.drawable.num3,
                            R.drawable.num4,
                            R.drawable.num5,
                            R.drawable.num6,
                            R.drawable.num7,
                            R.drawable.temperature};
                    statusImage.setImageResource(statusIcon[position-1]);
                    return view;
            }

//            TextView deviceName;
//            // General ListView optimization code.
//            if (view == null) {
//                view = mInflator.inflate(R.layout.listitem_device, viewGroup, false);
//                deviceName = (TextView) view.findViewById(R.id.device_name);
//                view.setTag(deviceName);
//            } else {
//                deviceName = (TextView) view.getTag();
//            }
//
//            BluetoothDevice device = mLeDevices.get(position);
//            final String deviceNameStr = device.getName();
//            if (deviceNameStr != null && deviceNameStr.length() > 0)
//                deviceName.setText(deviceNameStr);
//            else
//                deviceName.setText("Unknown");
//            return view;
        }
    }




}
