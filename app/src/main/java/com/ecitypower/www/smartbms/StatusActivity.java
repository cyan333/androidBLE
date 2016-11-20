package com.ecitypower.www.smartbms;

import android.app.Activity;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

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
    private Button scanButton;
    private int toggle = 0;
    /**
     * Called when the activity is first created.
     */

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status);

        characteristic = gatt.getService(BLE_SERVICE_UUID).getCharacteristic(BLE_CHAR_UUID);

        scanButton = (Button) findViewById(R.id.button);
        scanButton.setOnClickListener(new View.OnClickListener() {
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
//        gatt.readCharacteristic(characteristic);

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
