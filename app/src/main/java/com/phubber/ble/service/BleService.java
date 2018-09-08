/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.phubber.ble.service;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Service for managing connection and data communication with a GATT server
 * hosted on a given Bluetooth LE device.
 */
public class BleService extends Service implements Handler.Callback {
    private final static String TAG = BleService.class.getSimpleName();
    public static final UUID CLIENT_CHARACTERISTIC_CONFIG_DESCRIPTOR_UUID = UUID
            .fromString("00002902-0000-1000-8000-00805f9b34fb");
    private BluetoothAdapter mBluetoothAdapter;
    public String mBluetoothDeviceAddress;
    public BluetoothGatt mBluetoothGatt;
    private Handler mHandler;
    private HandlerThread mHandlerThread = new HandlerThread("BleServiceThread");
    private final int MSG_BLE_START_SCAN_DEVICE = 1000;
    private final int MSG_BLE_STOP_SCAN_DEVICE = 1001;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG,"onCreate");
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mHandlerThread.start();
        mHandler = new Handler(mHandlerThread.getLooper(), this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG,"onStartCommand");
        return super.onStartCommand(intent, flags, startId);
    }

    public void startScanBleDevice() {
        Log.d(TAG,"startScanBleDevice");
        mHandler.sendEmptyMessage(MSG_BLE_START_SCAN_DEVICE);
    }

    public void stopScanBleDevice() {
        mHandler.sendEmptyMessage(MSG_BLE_STOP_SCAN_DEVICE);
    }

    private void _startScanBleDevice() {
        Log.d(TAG,"_startScanBleDevice mBluetoothAdapter:"+mBluetoothAdapter);
        if (mBluetoothAdapter == null)
            return;
        BluetoothLeScanner scanner = mBluetoothAdapter.getBluetoothLeScanner();
        scanner.startScan(new ScanCallback() {
            public void onScanResult(int callbackType, ScanResult result) {
                Log.d(TAG,"onScanResult callbackType:"+callbackType+" result:"+result);
                ArrayList<ScanResult> results = new ArrayList<>();
                results.add(result);
                for(IBleScanResultListener listener:mBleScanResultListeners)
                {
                    listener.onScanResult(0,results);
                }
            }

            public void onBatchScanResults(List<ScanResult> results) {
                Log.d(TAG,"onScanResult results:"+results);
                for(IBleScanResultListener listener:mBleScanResultListeners)
                {
                    listener.onScanResult(0,results);
                }
            }

            public void onScanFailed(int errorCode) {
                Log.d(TAG,"onScanFailed errorCode:"+errorCode);
            }
        });
    }

    private ArrayList<IBleScanResultListener> mBleScanResultListeners = new ArrayList<>();
    public void addBleScanResultListener(IBleScanResultListener listener)
    {
        if(!mBleScanResultListeners.contains(listener))
            mBleScanResultListeners.add(listener);
    }
    public void removeBleScanResultListener(IBleScanResultListener listener)
    {
        mBleScanResultListeners.remove(listener);
    }

    @Override
    public boolean handleMessage(Message msg) {
        Log.d(TAG,"handleMessage msg:"+msg);
        switch (msg.what) {
            case MSG_BLE_START_SCAN_DEVICE:
                _startScanBleDevice();
                break;
            case MSG_BLE_STOP_SCAN_DEVICE:
                stopScanBleDevice();
                break;
        }
        return true;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mHandlerThread.quitSafely();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public void onRebind(Intent intent) {
        super.onRebind(intent);
    }


    private LocalBinder mLocalBinder = new LocalBinder();
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG,"onBind");
        return mLocalBinder;
    }

    public class LocalBinder extends Binder
    {
        public BleService getService(){
            return BleService.this;
        }
    }

}
