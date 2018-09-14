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
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import com.phubber.ble.utils.GattInfo;

import java.util.ArrayList;
import java.util.HashMap;
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
    private boolean mIsConnectGatt;
    private Handler mHandler;
    private HandlerThread mHandlerThread = new HandlerThread("BleServiceThread");
    private final int MSG_BLE_STOP_SCAN_DEVICE = 1001;
    private final int MSG_BLE_PARCEL_GATTINFO = 1002;
    private final int MSG_BLE_CONNECT_STATE = 1006;
    private final int MSG_BLE_DISCOVERY = 1004;
    private final int MSG_BLE_READ_RSSI = 1005;
    private final int MSG_BLE_ON_DISCOVERY_RESULT = 1007;
    private final int MSG_BLE_ON_SERVICE_DISCOVERED = 1008;
    private final int MSG_BLE_ON_CHARACTERISTIC_READ = 1009;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate");
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mHandlerThread.start();
        mHandler = new Handler(mHandlerThread.getLooper(), this);
        mHandler.sendEmptyMessage(MSG_BLE_PARCEL_GATTINFO);

        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mReceiver, filter);
    }

    public void startScanBleDevice() {
        Log.d(TAG, "_startScanBleDevice mBluetoothAdapter:" + mBluetoothAdapter);
        if (mBluetoothAdapter == null)
            return;
        BluetoothLeScanner scanner = mBluetoothAdapter.getBluetoothLeScanner();
        scanner.startScan(new ScanCallback() {
            public void onScanResult(int callbackType, ScanResult result) {
                Log.d(TAG, "onScanResult callbackType:" + callbackType + " result:" + result);
                BluetoothRssi ble = new BluetoothRssi(result.getDevice(), result.getRssi());
                for (IDiscoveryResultListener listener : mBleScanResultListeners) {
                    listener.onDiscoveryResult(0, ble);
                }
            }

            public void onBatchScanResults(List<ScanResult> results) {
                Log.d(TAG, "onScanResult results:" + results);
                ArrayList<BluetoothRssi> _results = new ArrayList<>();
                for (ScanResult result : results) {
                    _results.add(new BluetoothRssi(result.getDevice(), result.getRssi()));
                }
                for (IDiscoveryResultListener listener : mBleScanResultListeners) {
                    listener.onDiscoveryResult(0, _results);
                }
            }

            public void onScanFailed(int errorCode) {
                Log.d(TAG, "onScanFailed errorCode:" + errorCode);
            }
        });
    }


    public void startDiscovery() {
        Log.d(TAG, "startScanBleDevice mBluetoothAdapter:" + mBluetoothAdapter);
        if (mBluetoothAdapter == null)
            return;
        mBluetoothAdapter.startDiscovery();
    }

    public void stopScanBleDevice() {
        mHandler.sendEmptyMessage(MSG_BLE_STOP_SCAN_DEVICE);
    }

    public void connectGatt(final String address) {
        Log.d(TAG,"connect address:"+address);
        if (mBluetoothAdapter == null || address == null) {
            Log.w(TAG, "BluetoothAdapter not initialized or unspecified address.");
            return;
        }

        // Previously connected device. Try to reconnect.
        if (mBluetoothDeviceAddress != null && address.equals(mBluetoothDeviceAddress) && mBluetoothGatt != null) {
            Log.d(TAG, "Trying to use an existing mBluetoothGatt for connection.");
            if (mBluetoothGatt.connect()) {
                mIsConnectGatt = true;
                for(IConnectStateListener listener:mConnectStateListeners)
                {
                    listener.onConnect(mBluetoothGatt);
                }
                return;
            } else {
                return;
            }
        }

        final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        if (device == null) {
            Log.w(TAG, "Device not found.  Unable to connect.");
            return;
        }
        // We want to directly connect to the device, so we are setting the
        // autoConnect
        // parameter to false.
        mBluetoothGatt = device.connectGatt(this, false, mGattCallback);
        Log.d(TAG, "Trying to create a new connection.");
        mBluetoothDeviceAddress = address;
    }
    public boolean isConnectGatt()
    {
        return mIsConnectGatt;
    }
    public void disconnectGatt()
    {
        Log.d(TAG,"disconnect");
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.disconnect();
        mBluetoothGatt = null;
    }

    /**
     * Request a read on a given {@code BluetoothGattCharacteristic}. The read
     * result is reported asynchronously through the
     * {@code BluetoothGattCallback#onCharacteristicRead(android.bluetooth.BluetoothGatt, android.bluetooth.BluetoothGattCharacteristic, int)}
     * callback.
     *
     * @param characteristic The characteristic to read from.
     */
    public void readCharacteristic(BluetoothGattCharacteristic characteristic) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        Log.d(TAG, "readCharacteristic characteristic:"+characteristic);
        mBluetoothGatt.readCharacteristic(characteristic);
    }

    /**
     * Enables or disables notification on a give characteristic.
     *
     * @param characteristic Characteristic to act on.
     * @param enabled        If true, enable notification. False otherwise.
     */
    public void setCharacteristicNotification(BluetoothGattCharacteristic characteristic, boolean enabled) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        if (enabled) {
            Log.i(TAG, "Enable Notification");
            mBluetoothGatt.setCharacteristicNotification(characteristic, true);
            BluetoothGattDescriptor descriptor = characteristic
                    .getDescriptor(CLIENT_CHARACTERISTIC_CONFIG_DESCRIPTOR_UUID);
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            mBluetoothGatt.writeDescriptor(descriptor);
        } else {
            Log.i(TAG, "Disable Notification");
            mBluetoothGatt.setCharacteristicNotification(characteristic, false);
            BluetoothGattDescriptor descriptor = characteristic
                    .getDescriptor(CLIENT_CHARACTERISTIC_CONFIG_DESCRIPTOR_UUID);
            descriptor.setValue(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
            mBluetoothGatt.writeDescriptor(descriptor);
        }

        // mBluetoothGatt.setCharacteristicNotification(characteristic,
        // enabled);
    }

    public void writeCharacteristic(BluetoothGattCharacteristic characteristic) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.writeCharacteristic(characteristic);
    }

    private BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            Log.d(TAG, "onConnectionStateChange gatt:"+gatt +" status:"+status+" newState:"+newState);
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.d(TAG, "Connected to GATT server.");
                mIsConnectGatt = true;
                if(gatt == null)
                    return;
                Message msg = mHandler.obtainMessage();
                msg.what = MSG_BLE_CONNECT_STATE;
                msg.arg1 = 1;
                msg.obj = gatt;
                mHandler.sendMessage(msg);
                // 发现设备端服务
                mBluetoothGatt.discoverServices();
                // 增加读rssi 的定时器
//                mHandler.removeMessages(MSG_BLE_READ_RSSI);
//                mHandler.sendEmptyMessage(MSG_BLE_READ_RSSI);
            }else if(newState == BluetoothProfile.STATE_CONNECTING){}
            else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.d(TAG, "Disconnected from GATT server.");
                mIsConnectGatt = false;
                mHandler.removeMessages(MSG_BLE_READ_RSSI);
                mHandler.sendEmptyMessage(MSG_BLE_CONNECT_STATE);
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
//                Message msg = mHandler.obtainMessage(MSG_BLE_ON_SERVICE_DISCOVERED, gatt);
//                mHandler.sendMessage(msg);
                mIsConnectGatt = true;
                for (IServiceDiscoverListener listener : mServiceDiscoverListeners) {
                    listener.onServiceDiscover(gatt);
                }
                // // 增加读rssi 的定时器
//                mHandler.removeMessages(MSG_BLE_READ_RSSI);
//                mHandler.sendEmptyMessage(MSG_BLE_READ_RSSI);

//                Message msg = mHandler.obtainMessage();
//                msg.what = MSG_BLE_CONNECT_STATE;
//                msg.arg1 = 1;
//                msg.obj = gatt;
//                mHandler.sendMessage(msg);
            } else {
                Log.d(TAG, "onServicesDiscovered received: " + status);
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            Log.d(TAG, "onCharacteristicRead status:" + status + ":gatt:" + gatt.toString() + " characteristic:" + characteristic.toString());
//            Message msg = mHandler.obtainMessage(MSG_BLE_ON_CHARACTERISTIC_READ);
//            HashMap<String,Object> mData = new HashMap<>();
//            mData.put("gatt", gatt);
//            mData.put("characteristic",characteristic);
//            msg.obj = mData;
//            msg.arg1 = status;
//            mHandler.sendMessage(msg);
            for (IDataAvailableListener listener : mDataAvailableListeners) {
                listener.onCharacteristicRead(gatt, characteristic,status);
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            Log.d(TAG, "onCharacteristicChanged gatt:" + gatt.toString() + " characteristic:" + characteristic.toString());
            for (IDataAvailableListener listener : mDataAvailableListeners) {
                listener.onCharacteristicWrite(gatt, characteristic);
            }
        }

        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            Log.d(TAG, "onReadRemoteRssi rssi:" + rssi + " status:" + status + " gatt:" + gatt.toString());
            if (mIsConnectGatt && status == BluetoothGatt.GATT_SUCCESS) {
                for (IRemoteRssiListener listener : mRemoteRssiListeners) {
                    listener.onReadRemoteRssi(gatt, rssi);
                }
            }
        }
    };

    @Override
    public boolean handleMessage(Message msg) {
        Log.d(TAG, "handleMessage msg:" + msg);
        switch (msg.what) {
            case MSG_BLE_PARCEL_GATTINFO:
                GattInfo.getInstance(getApplicationContext());
                break;
            case MSG_BLE_STOP_SCAN_DEVICE:
                stopScanBleDevice();
                break;
            case MSG_BLE_READ_RSSI:
                if (mBluetoothGatt != null) {
                    mBluetoothGatt.readRemoteRssi();
                }
                mHandler.sendEmptyMessage(MSG_BLE_READ_RSSI);
                break;
            case MSG_BLE_CONNECT_STATE:
                int status = msg.arg1;
                synchronized (this) {
                    for (IConnectStateListener listener : mConnectStateListeners) {
                        if (status == 1)
                            listener.onConnect((BluetoothGatt) msg.obj);
                        else
                            listener.onDisconnect((BluetoothGatt) msg.obj);
                    }
                }
                break;
            case MSG_BLE_ON_DISCOVERY_RESULT:
                for (IDiscoveryResultListener listener : mBleScanResultListeners) {
                    listener.onDiscoveryResult(0, (BluetoothRssi) msg.obj);
                }
                break;
            case MSG_BLE_ON_SERVICE_DISCOVERED:
                for (IServiceDiscoverListener listener : mServiceDiscoverListeners) {
                    listener.onServiceDiscover((BluetoothGatt) msg.obj);
                }
                break;
            case MSG_BLE_ON_CHARACTERISTIC_READ:
                HashMap<String,Object> mData = (HashMap<String,Object>)msg.obj;
                BluetoothGatt gatt = (BluetoothGatt)mData.get("gatt");
                BluetoothGattCharacteristic characteristic = (BluetoothGattCharacteristic)mData.get("characteristic");
                for (IDataAvailableListener listener : mDataAvailableListeners) {
                    listener.onCharacteristicRead(gatt, characteristic, msg.arg1);
                }
                break;
        }
        return true;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
        mHandlerThread.quitSafely();
    }

    private LocalBinder mLocalBinder = new LocalBinder();

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind");
        return mLocalBinder;
    }

    public class LocalBinder extends Binder {
        public BleService getService() {
            return BleService.this;
        }
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d(TAG, "onReceive action:" + action);
            if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {

            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {

            } else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                int rssi = intent.getExtras().getShort(BluetoothDevice.EXTRA_RSSI);
                BluetoothRssi ble = new BluetoothRssi(device, rssi);
//                Message msg = mHandler.obtainMessage(MSG_BLE_ON_DISCOVERY_RESULT,ble);
//                mHandler.sendMessage(msg);
                for (IDiscoveryResultListener listener : mBleScanResultListeners) {
                    listener.onDiscoveryResult(0, ble);
                }
            }

        }
    };


    private ArrayList<IDiscoveryResultListener> mBleScanResultListeners = new ArrayList<>();

    public void addBleScanResultListener(IDiscoveryResultListener listener) {
        if (!mBleScanResultListeners.contains(listener))
            mBleScanResultListeners.add(listener);
    }

    public void removeBleScanResultListener(IDiscoveryResultListener listener) {
        mBleScanResultListeners.remove(listener);
    }

    private ArrayList<IConnectStateListener> mConnectStateListeners = new ArrayList<IConnectStateListener>();

    public synchronized void addConnectStateListener(IConnectStateListener listener) {
        if (!mConnectStateListeners.contains(listener))
            mConnectStateListeners.add(listener);
    }

    public synchronized void removeConnectStateListener(IConnectStateListener listener) {
        mConnectStateListeners.remove(listener);
    }

    private ArrayList<IRemoteRssiListener> mRemoteRssiListeners = new ArrayList<IRemoteRssiListener>();

    public void addRemoteRssiListener(IRemoteRssiListener listener) {
        if (mRemoteRssiListeners.contains(listener))
            mRemoteRssiListeners.add(listener);
    }
    public void removeRemoteRssiListener(IRemoteRssiListener listener) {
        mRemoteRssiListeners.remove(listener);
    }

    private ArrayList<IServiceDiscoverListener> mServiceDiscoverListeners = new ArrayList<IServiceDiscoverListener>();

    public void addServiceDiscoverListener(IServiceDiscoverListener listener) {
        if (!mServiceDiscoverListeners.contains(listener))
            mServiceDiscoverListeners.add(listener);
    }

    public void removeServiceDiscoverListener(IServiceDiscoverListener listener) {
        mServiceDiscoverListeners.remove(listener);
    }

    private ArrayList<IDataAvailableListener> mDataAvailableListeners = new ArrayList<IDataAvailableListener>();

    public void addDataAvailableListener(IDataAvailableListener listener) {
        if (!mDataAvailableListeners.contains(listener))
            mDataAvailableListeners.add(listener);
    }

    public void removeDataAvailableListener(IDataAvailableListener listener) {
        mDataAvailableListeners.remove(listener);
    }

}
