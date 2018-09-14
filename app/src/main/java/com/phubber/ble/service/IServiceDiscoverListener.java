package com.phubber.ble.service;

import android.bluetooth.BluetoothGatt;

public interface IServiceDiscoverListener {
    void onServiceDiscover(BluetoothGatt gatt);
}
