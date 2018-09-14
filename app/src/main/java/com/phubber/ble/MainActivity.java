package com.phubber.ble;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.util.Log;

import com.phubber.ble.common.BaseActivity;
import com.phubber.ble.common.BaseFragment;
import com.phubber.ble.fragment.DeviceFragment;
import com.phubber.ble.service.BleService;
import com.phubber.ble.utils.ContainerManager;

import java.util.List;


public class MainActivity extends BaseActivity {
    private static String TAG = MainActivity.class.getSimpleName();
    private Intent mServiceIntent = new Intent("android.intent.service.BleService");
    protected BleService mBleService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        setContentView(R.layout.activity_main);
//        getMainLooper().setMessageLogging((String x)->
//                Log.d(TAG, x)
//        );
        startBleService();
    }

    /**
     * 导常时才调用，一般不需要主动调用此方法
     */
    public void startBleService() {
        Log.d(TAG, "connectBleService");
        mServiceIntent.setPackage(getPackageName());
        bindService(mServiceIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
        startService(mServiceIntent);
        Log.d(TAG, "connectBleService over");
    }

    public final void stopBleService() {
        unbindService(mServiceConnection);
        stopService(mServiceIntent);
    }

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG, "onServiceConnected");
            mBleService = ((BleService.LocalBinder) service).getService();
            DeviceFragment deviceFragment = new DeviceFragment();
            deviceFragment.setBleService(mBleService);
            ContainerManager.getInstance(getSupportFragmentManager()).add(deviceFragment, false);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(TAG, "onServiceDisconnected");
            mBleService = null;
            finish();
        }

        @Override
        public void onBindingDied(ComponentName name) {
            Log.e(TAG, "onBindingDied name:" + name);
            mBleService = null;
            finish();
        }
    };

    @Override
    public void onBackPressed() {
        int count = getSupportFragmentManager().getFragments().size();
        Log.d(TAG, "onBackPressed count:" + count);
        if (count == 0)
            super.onBackPressed();
        else {
            List<Fragment> fragments = getSupportFragmentManager().getFragments();
            for (Fragment fragment : fragments) {
                if (fragment.isVisible() && fragment instanceof BaseFragment) {
                    BaseFragment baseFragment = (BaseFragment) fragment;
                    baseFragment.onBackPressed();
                }
            }
        }
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        stopBleService();
        ContainerManager.getInstance().release();
        super.onDestroy();

    }
}
