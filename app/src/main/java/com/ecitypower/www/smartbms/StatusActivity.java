package com.ecitypower.www.smartbms;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
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
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.UUID;

/**
 * Created by Fangming on 11/20/16.
 */

public class StatusActivity extends Activity {
    public static BluetoothGatt gatt;

    /* Service UUID */
    private static final UUID BLE_SERVICE_UUID = UUID.fromString("0000ffe0-0000-1000-8000-00805f9b34fb");

    /* Characteristic UUID */
    private static final UUID BLE_CHAR_UUID = UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb");

    private BluetoothGattCharacteristic characteristic;

    private BluetoothAdapter mBluetoothAdapter;
    private Handler mHandler;
    private LeDeviceListAdapter mLeDeviceListAdapter;

    /* Button - Scan Button */
    private Button scanButton;

    /* GATT */
    private BluetoothGatt mConnectedGatt;

    /* Alert Diaglo */
    private AlertDialog BLEAScanAlertDialog;

    private int toggle = 0;

    //define Final Variable
    private static final long SCAN_PERIOD = 10000;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status);

        /* Initialize */
        mHandler = new Handler();
        mLeDeviceListAdapter = new LeDeviceListAdapter();

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
                final BluetoothDevice connectedDevice = mLeDeviceListAdapter.getDevice(position);
                mConnectedGatt = connectedDevice.connectGatt(StatusActivity.this, false, mGattCallback);
            }
        });

        BLEAScanAlertDialog = BLEAScanAlertDialogBuilder
                .setView(BLEDevicesList)
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
        BLEAScanAlertDialog.show();


//        characteristic = gatt.getService(BLE_SERVICE_UUID).getCharacteristic(BLE_CHAR_UUID);
//
//        scanButton = (Button) findViewById(R.id.button);
//        scanButton.setOnClickListener(new View.OnClickListener() {
//            public void onClick(View v) {
//                if (toggle == 0){
//                    characteristic.setValue("1");
//                    gatt.writeCharacteristic(characteristic);
//                    toggle = 1;
//                }
//                else {
//                    characteristic.setValue("0");
//                    gatt.writeCharacteristic(characteristic);
//                    toggle = 0;
//                }
//            }
//        });
//        gatt.readCharacteristic(characteristic);

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
                scanButton.setText("Scan");
                scanButton.setEnabled(true);
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
                            if (device.getName() != null) {
                                    Log.i("debug","Device Name: " + device.getName());
                                    mLeDeviceListAdapter.addDevice(device);
                                    mLeDeviceListAdapter.notifyDataSetChanged();
                                }
                        }
                    });
                }
            };

    //////////////////////////////////
    ///// Connect Device Call Back////
    //////////////////////////////////
    private BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                gatt.discoverServices();
                Log.i("debug", "Did connect device");
            }

//            Log.d("debug", "Connection State Change: "+status+" -> " + newState);
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            Log.i("debug", "Connection Service State : "+ status);
            if (status == 0){
                BLEAScanAlertDialog.dismiss();
            }
        }
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
//
//    @Override
//    public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
//
//        //Enable local notifications
//        gatt.setCharacteristicNotification(characteristic, true);
//        //Enabled remote notifications
//        BluetoothGattDescriptor desc = characteristic.getDescriptor(CONFIG_DESCRIPTOR);
//        desc.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
//        gatt.writeDescriptor(desc);
//
//    }

}
