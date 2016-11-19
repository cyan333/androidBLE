package com.ecitypower.www.smartbms;

/**
 * Created by Fangming on 11/19/16.
 */

public class BLENameAddress {
    private String deviceName;
    private String deviceAddress;

    public void setNameAddress (String deviceName, String deviceAddress) {
        this.deviceName = deviceName;
        this.deviceAddress = deviceAddress;
    }

    public String getDeviceName (){
        return this.deviceName;
    }

    public String getDeviceAddress () {
        return this.deviceAddress;
    }

    @Override
    public String toString (){
        return this.deviceName;
    }

}
