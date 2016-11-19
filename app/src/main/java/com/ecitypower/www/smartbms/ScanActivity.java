package com.ecitypower.www.smartbms;

import android.app.ListActivity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

import static android.content.ContentValues.TAG;

public class ScanActivity extends ListActivity {
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeService mBluetoothLeService;
    private BLENameAddress connectedBLE;
    private Handler mHandler;
    private Button scanButton;
    private static final long SCAN_PERIOD = 10000;

    ArrayList<BLENameAddress> BLEDevicesList=new ArrayList<BLENameAddress>();
    ArrayAdapter<BLENameAddress> listAdapter;

    // Code to manage Service lifecycle.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }
            // Automatically connects to the device upon successful start-up initialization.
            mBluetoothLeService.connect(connectedBLE.getDeviceAddress());
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };

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

        listAdapter = new ArrayAdapter<BLENameAddress>(this, android.R.layout.simple_list_item_1, BLEDevicesList);
        setListAdapter(listAdapter);



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
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
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
        connectedBLE = BLEDevicesList.get(position);
        Log.i("debug", "Connecting to " + connectedBLE.getDeviceName());

//        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
//        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
//        mBluetoothLeService.connect(connectedBLE.getDeviceAddress());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mServiceConnection);
        mBluetoothLeService = null;
    }

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
//                            boolean flag = true;
                            if (device.getName() != null) {
                                for (BLENameAddress BLE : BLEDevicesList){
                                    if (device.getAddress().equals(BLE.getDeviceAddress())){
                                        return;
                                    }
                                }
                                BLENameAddress newBLE = new BLENameAddress();
                                newBLE.setNameAddress(device.getName(),device.getAddress());
                                BLEDevicesList.add(newBLE);
                                Log.i("debug","Device Name" + newBLE.getDeviceName());


//                                BLEDevicesList.add(device.getName());
                                listAdapter.notifyDataSetChanged();
                            }
//                            BLEDevicesList.add(device.getName());
//                            listAdapter.notifyDataSetChanged();
                        }
                    });
                }
            };
}
