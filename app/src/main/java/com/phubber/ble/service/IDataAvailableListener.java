package com.phubber.ble.service;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;

public interface IDataAvailableListener {
    void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status);
    void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic);
}
