package com.phubber.ble.service;

import android.bluetooth.le.ScanResult;

import java.util.List;

public interface IBleScanResultListener {
    /**
     * @param ret 0,成功; -1，失败
     * @param results 扫描列表
     */
    void onScanResult(int ret,List<ScanResult> results);
}
