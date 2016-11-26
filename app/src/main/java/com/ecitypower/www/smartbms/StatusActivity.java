package com.ecitypower.www.smartbms;

import android.app.Activity;
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
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.ybq.android.spinkit.SpinKitView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import static android.content.ContentValues.TAG;

/**
 * Created by Fangming on 11/20/16.
 */

public class StatusActivity extends Activity {
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
    private Button LEDButton;

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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status);

        //Utils.savePreferences("hi","name1",this);
        Utils.loadPreferences(this);

        /* Initialize */
        mHandler = new Handler();
        mLeDeviceListAdapter = new LeDeviceListAdapter();

        loading = (SpinKitView) findViewById(R.id.loading);

        //Bluetooth permission request.
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] { android.Manifest.permission.ACCESS_COARSE_LOCATION }, 1);
        }

        // Use this check to determine whether BLE is supported on the device.  Then you can
        // selectively disable BLE-related features.
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "BLE not supported.", Toast.LENGTH_SHORT).show();
            finish();
        }
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        // Checks if Bluetooth is supported on the device.
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth not supported", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        //////////////////////////
        ///////Alert Dialog///////
        //////////////////////////
        AlertDialog.Builder BLEAScanAlertDialogBuilder = new AlertDialog.Builder(StatusActivity.this);

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
                        Log.i("debug","CLicked!!!");
                    }
                });
            }
        });
        ////////////////////////////////////////////////////////
        ///////////Save and Load Saved Device Address///////////
        ////////////////////////////////////////////////////////

        HashMap<String, String> savedAddressName = Utils.loadPreferences(this);
        savedAddress = savedAddressName.get(Utils.PREF_BLEADDRESS);
        if (savedAddress.equals("")){
            BLEAScanAlertDialog.show();
        }
        else {
            loading.setVisibility(loading.VISIBLE);
            scanLeDevice();
        }


        LEDButton = (Button) findViewById(R.id.button);
        LEDButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (toggle == 0){
                    characteristic.setValue("1");
                    gatt.writeCharacteristic(characteristic);
                    toggle = 1;
                }
                else {
                    characteristic.setValue("0");
                    gatt.writeCharacteristic(characteristic);
                    toggle = 0;
                }
            }
        });

    }
    //Oncreate End

    //////////////////////////////////////////////////
    ///////////Scan BLE Alert Dialog Layout///////////
    //////////////////////////////////////////////////
    private LinearLayout makeAlertDialog () {
        LinearLayout layout = new LinearLayout(this);
        LinearLayout.LayoutParams parms = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setLayoutParams(parms);

        connectionFail = new TextView(this);
        connectionFail.setText("");
        connectionFail.setTextColor(ContextCompat.getColor(this, R.color.colorWarningRed));
        connectionFail.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        connectionFail.setTextSize(15);

        //Alert Dialog --> List - BLE Device List
        ListView BLEDevicesList = new ListView(this);
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
                mConnectedGatt = connectedDevice.connectGatt(StatusActivity.this, false, mGattCallback);
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
                    runOnUiThread(new Runnable() {
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
                                    mConnectedGatt = device.connectGatt(StatusActivity.this, false, mGattCallback);
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

                runOnUiThread(new Runnable() {
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
                Utils.savePreferences(connectedDevice.getAddress(),connectedDevice.getName(),StatusActivity.this);
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
            Log.i("debug","changed");
            int hi = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0);
            Log.i("debug", Integer.toString(hi));
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



    ////////////////////////////////////////////////////////
    /////////////////////Custome Adaptor////////////////////
    ///Adapter for holding devices found through scanning///
    ////////////////////////////////////////////////////////
    private class LeDeviceListAdapter extends BaseAdapter {
        private ArrayList<BluetoothDevice> mLeDevices;
        private LayoutInflater mInflator;

        public LeDeviceListAdapter() {
            super();
            mLeDevices = new ArrayList<BluetoothDevice>();
            mInflator = StatusActivity.this.getLayoutInflater();
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
        public View getView(int i, View view, ViewGroup viewGroup) {
            TextView deviceName;
            // General ListView optimization code.
            if (view == null) {
                view = mInflator.inflate(R.layout.listitem_device, viewGroup, false);
                deviceName = (TextView) view.findViewById(R.id.device_name);
                view.setTag(deviceName);
            } else {
                deviceName = (TextView) view.getTag();
            }

            BluetoothDevice device = mLeDevices.get(i);
            final String deviceNameStr = device.getName();
            if (deviceNameStr != null && deviceNameStr.length() > 0)
                deviceName.setText(deviceNameStr);
            else
                deviceName.setText("Unknown");
            return view;
        }
    }





}
