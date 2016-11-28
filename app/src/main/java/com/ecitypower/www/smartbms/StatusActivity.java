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

import com.github.ybq.android.spinkit.SpinKitView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import static android.content.ContentValues.TAG;
import static android.content.Context.BLUETOOTH_SERVICE;

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

    private BluetoothGattCharacteristic characteristic;

    private BluetoothAdapter mBluetoothAdapter;
    private Handler mHandler;
    private LeDeviceListAdapter mLeDeviceListAdapter;

    /* Buttons */
    private Button scanButton;
//    private Button LEDButton;

    /* GATT */
    private BluetoothGatt mConnectedGatt;

    /* Alert Diaglo */
    private AlertDialog BLEAScanAlertDialog;

    private int toggle = 0;

    //define Final Variable
    private static final long SCAN_PERIOD = 10000;

    private BluetoothDevice connectedDevice;

    private TextView connectionFail;

    private String savedAddress;

    private SpinKitView loading;

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
        loading.setVisibility(loading.INVISIBLE);

        //Bluetooth permission request.
        if (ContextCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[] { android.Manifest.permission.ACCESS_COARSE_LOCATION }, 1);
        }

        // Use this check to determine whether BLE is supported on the device.  Then you can
        // selectively disable BLE-related features.
        if (!getActivity().getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(getActivity(), "BLE not supported.", Toast.LENGTH_SHORT).show();
            getActivity().finish();
        }
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getActivity().getSystemService(BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        // Checks if Bluetooth is supported on the device.
        if (mBluetoothAdapter == null) {
            Toast.makeText(getActivity(), "Bluetooth not supported", Toast.LENGTH_SHORT).show();
            getActivity().finish();
//            return;
        }

        //////////////////////////
        ///////Alert Dialog///////
        //////////////////////////
        AlertDialog.Builder BLEAScanAlertDialogBuilder = new AlertDialog.Builder(getActivity());

        BLEAScanAlertDialog = BLEAScanAlertDialogBuilder
                .setView(makeAlertDialog())
                .setTitle("Select Bluetooth Device")
                .setPositiveButton("Scan", null) //Set to null. We override the onclick
                .setNegativeButton("Cancel", null)
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
                        scanButton.setText("Scanning");
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

        HashMap<String, String> savedAddressName = Utils.loadPreferences(getActivity());
        savedAddress = savedAddressName.get(Utils.PREF_BLEADDRESS);
        if (savedAddress.equals("")){
            BLEAScanAlertDialog.show();

        }
        else {
            loading.setVisibility(loading.VISIBLE);
            scanLeDevice();
        }

        mStatusListAdapter = new StatusListAdapter();
        ListView statusList = (ListView)statusView.findViewById(R.id.statusList);
        statusList.setAdapter(mStatusListAdapter);

        mStatusListAdapter.addData("VOLTAGE", "Loading");
        mStatusListAdapter.addData("CELL-1 VOLTAGE", "4.4V");
        mStatusListAdapter.addData("CELL-2 VOLTAGE", "3.4V");
        mStatusListAdapter.addData("CELL-3 VOLTAGE", "6.4V");
        mStatusListAdapter.addData("CELL-4 VOLTAGE", "7.4V");
        mStatusListAdapter.addData("CELL-5 VOLTAGE", "8.4V");
        mStatusListAdapter.addData("CELL-6 VOLTAGE", "9.4V");


        return statusView;
    }
    //Oncreate End

    //////////////////////////////////////////////////
    ///////////////////Write Data/////////////////////
    //////////////////////////////////////////////////
    public void writeData (String input){
        characteristic.setValue(input);
        gatt.writeCharacteristic(characteristic);
    }


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
                scanButton.setText("Scan");
                scanButton.setEnabled(true);
                connectedDevice = mLeDeviceListAdapter.getDevice(position);
                mConnectedGatt = connectedDevice.connectGatt(getActivity(), false, mGattCallback);
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
                    scanButton.setText("Scan");
                    scanButton.setEnabled(true);
                }
                else if (connectedDevice == null) {
                    loading.setVisibility(loading.INVISIBLE);
                    BLEAScanAlertDialog.show();
                    connectionFail.setText("Cannot find previous device.");
                }



            }
        }, SCAN_PERIOD);
        Log.i("debug","SSSSSSStart scanning");
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
                                    loading.setVisibility(loading.INVISIBLE);
                                    connectedDevice = device;
                                    mConnectedGatt = device.connectGatt(getActivity(), false, mGattCallback);
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
                        connectionFail.setText("Connection Failed");
                    }
                });
            }
            else {
                characteristic = gatt.getService(BLE_SERVICE_UUID).getCharacteristic(BLE_CHAR_UUID);
                gatt.readCharacteristic(characteristic);
                StatusActivity.this.gatt = gatt;
                Utils.savePreferences(connectedDevice.getAddress(),connectedDevice.getName(),getActivity());
                BLEAScanAlertDialog.dismiss();
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
            final int hi = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0);
//            Log.i("debug", Integer.toString(hi));
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mStatusListAdapter.addData("VOLTAGE", Integer.toString(hi)+"V");
                    mStatusListAdapter.notifyDataSetChanged();
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
                deviceName.setText("Unknown");
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

//        public void clear() {
//            mLeDevices.clear();
//        }

        public String position2Key (int position){
            switch (position){
                default: return "VOLTAGE";
                case 1: return "CELL-1 VOLTAGE";
                case 2: return "CELL-2 VOLTAGE";
                case 3: return "CELL-3 VOLTAGE";
                case 4: return "CELL-4 VOLTAGE";
                case 5: return "CELL-5 VOLTAGE";
                case 6: return "CELL-6 VOLTAGE";
                case 7: return "CELL-7 VOLTAGE";
                case 8: return "TEMPERATURE";
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
                    Log.i("debug","position:"+position);
                    switch (position){
                        case 0:
                            statusImage.setImageResource(R.drawable.batterysquare);
                            break;
                        case 1:
                            statusImage.setImageResource(R.drawable.num1);
                            break;
                        case 2:
                            statusImage.setImageResource(R.drawable.num2);
                            break;
                        case 3:
                            statusImage.setImageResource(R.drawable.num3);
                            break;
                        case 4:
                            statusImage.setImageResource(R.drawable.num4);
                            break;
                        case 5:
                            statusImage.setImageResource(R.drawable.num5);
                            break;
                        case 6:
                            statusImage.setImageResource(R.drawable.num6);
                            break;
                        case 7:
                            statusImage.setImageResource(R.drawable.num7);
                            break;
                        case 8:
                            statusImage.setImageResource(R.drawable.temperature);
                            break;
                    }

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
