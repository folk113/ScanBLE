package com.phubber.ble.service;

import android.bluetooth.BluetoothGatt;

public interface IRemoteRssiListener {
    void onReadRemoteRssi(BluetoothGatt gatt, int rssi);
}
