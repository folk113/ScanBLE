package com.phubber.ble.service;

import android.bluetooth.BluetoothGatt;

public interface IConnectStateListener {
    void onConnect(BluetoothGatt gatt);
    void onDisconnect(BluetoothGatt gatt);
}
