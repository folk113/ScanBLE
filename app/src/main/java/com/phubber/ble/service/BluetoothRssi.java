package com.phubber.ble.service;

import android.bluetooth.BluetoothDevice;

public class BluetoothRssi{
    public BluetoothDevice mBluetoothDevice;
    public int mRssi;
    public BluetoothRssi(BluetoothDevice device,int rssi)
    {
        mBluetoothDevice = device;
        mRssi = rssi;
    }
}
