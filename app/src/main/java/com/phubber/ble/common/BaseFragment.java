package com.phubber.ble.common;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.util.Log;

import com.phubber.ble.service.BleService;

public class BaseFragment extends Fragment {
    private static final String TAG = BaseFragment.class.getSimpleName();
    private Intent mServiceIntent = new Intent("android.intent.service.BleService");
    private BleService mBleService;
    private ServiceConnection mServiceConnection = new ServiceConnection(){
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG,"onServiceConnected");
            mBleService = ((BleService.LocalBinder)service).getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(TAG,"onServiceDisconnected");
            mBleService = null;
        }

        @Override
        public void onBindingDied(ComponentName name) {
            Log.e(TAG,"onBindingDied name:"+name);
            mBleService = null;
        }
    };

    public BleService getBleService()
    {
        return mBleService;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Log.d(TAG,"onAttach");
        connectBleService(context);
    }

    /**
     * 导常时才调用，一般不需要主动调用此方法
     * @param context
     */
    public void connectBleService(Context context)
    {
        mServiceIntent.setPackage(context.getPackageName());
        context.bindService(mServiceIntent,mServiceConnection, Context.BIND_AUTO_CREATE);
        context.startService(mServiceIntent);
    }

    @Override
    public void onDetach() {
        Log.d(TAG,"onDetach");
        getContext().unbindService(mServiceConnection);
        super.onDetach();
    }
}
