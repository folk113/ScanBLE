package com.phubber.ble.service;

import android.bluetooth.le.ScanResult;

import java.util.List;

public interface IDiscoveryResultListener {
    /**
     * @param ret 0,成功; -1，失败
     * @param results 扫描列表
     */
    void onDiscoveryResult(int ret,List<BluetoothRssi> results);
    void onDiscoveryResult(int ret,BluetoothRssi result);
}
