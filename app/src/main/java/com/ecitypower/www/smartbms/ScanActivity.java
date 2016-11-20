package com.ecitypower.www.smartbms;

import android.app.ListActivity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class ScanActivity extends ListActivity{
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeService mBluetoothLeService;
    private LeDeviceListAdapter mLeDeviceListAdapter;
//    private BLENameAddress connectedBLE;
    private Handler mHandler;
    private Button scanButton;
    private static final long SCAN_PERIOD = 10000;
//    private BluetoothDevice device2;
    private BluetoothGatt mConnectedGatt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);
        mHandler = new Handler();

        scanButton = (Button) findViewById(R.id.scanDevicesButton);
        scanButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.i("debug", "Scan button Clicked");
                scanButton.setText("扫描中...");
                scanButton.setEnabled(false);
//                new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION}, 1; 
                scanLeDevice();
            }
        });

        mLeDeviceListAdapter = new LeDeviceListAdapter();
        setListAdapter(mLeDeviceListAdapter);

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
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        mBluetoothAdapter.stopLeScan(mLeScanCallback);
        scanButton.setText("扫描蓝牙设备");
        scanButton.setEnabled(true);
        final BluetoothDevice connectedDevice = mLeDeviceListAdapter.getDevice(position);
        mConnectedGatt = connectedDevice.connectGatt(this, false, mGattCallback);

        Log.i("debug", "Connecting to " + connectedDevice.getName());

//        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
//        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
//        mBluetoothLeService.connect(connectedBLE.getDeviceAddress());
    }

    private BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                gatt.discoverServices();
//                Intent k = new Intent(ScanActivity.this, TabBarActivity.class);
//                startActivity(k);
            }

//            Log.d("debug", "Connection State Change: "+status+" -> " + newState);
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == 0){
                StatusActivity.gatt = gatt;
                Intent nextActivity = new Intent(ScanActivity.this, TabBarActivity.class);
                startActivity(nextActivity);
            }
        }
    };

    ////////////////////////////////////
    ///////////Scan LE Device///////////
    ////////////////////////////////////

    private void scanLeDevice() {
        // Stops scanning after a pre-defined scan period.
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mBluetoothAdapter.stopLeScan(mLeScanCallback);
                scanButton.setText("扫描蓝牙设备");
                scanButton.setEnabled(true);
            }
        }, SCAN_PERIOD);
        Log.i("debug","start scanning");
        mBluetoothAdapter.startLeScan(mLeScanCallback);
    }

    // Device scan callback.
    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {
                @Override
                public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (device.getName() != null) {
                                mLeDeviceListAdapter.addDevice(device);
                                mLeDeviceListAdapter.notifyDataSetChanged();
                            }
                        }
                    });
                }
            };

    //Custome Adaptor
    // Adapter for holding devices found through scanning.
    private class LeDeviceListAdapter extends BaseAdapter {
        private ArrayList<BluetoothDevice> mLeDevices;
        private LayoutInflater mInflator;

        public LeDeviceListAdapter() {
            super();
            mLeDevices = new ArrayList<BluetoothDevice>();
            mInflator = ScanActivity.this.getLayoutInflater();
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
