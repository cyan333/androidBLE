package com.ecitypower.www.smartbms;

import android.app.ListActivity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Toast;

import java.util.ArrayList;

public class ScanActivity extends ListActivity {
    private BluetoothAdapter mBluetoothAdapter;

    ArrayList<BLENameAddress> BLEDevicesList=new ArrayList<BLENameAddress>();

    //DEFINING A STRING ADAPTER WHICH WILL HANDLE THE DATA OF THE LISTVIEW
    ArrayAdapter<BLENameAddress> listAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);

        Button button= (Button) findViewById(R.id.scanDevicesButton);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.i("debug", "Scan button Clicked");
//                new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION}, 1; 
                mBluetoothAdapter.startLeScan(mLeScanCallback);
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
