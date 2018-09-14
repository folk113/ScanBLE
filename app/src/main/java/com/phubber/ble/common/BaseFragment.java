package com.phubber.ble.common;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.View;

import com.phubber.ble.service.BleService;
import com.phubber.ble.utils.ContainerManager;

public class BaseFragment extends Fragment implements Handler.Callback{
    private static final String TAG = BaseFragment.class.getSimpleName();
    protected BleService mBleService;
    private Handler mUiHandler;
    private BaseFragment mParent;
    public BaseFragment()
    {
        super();
        mUiHandler = new Handler(Looper.getMainLooper(),this);
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
    }

    public void onBackPressed()
    {
        Log.d(TAG, "onBackPressed");
        backToParent();
    }

    private void setParent(BaseFragment parent)
    {
        mParent = parent;
    }

    public void goToChild(BaseFragment fragment)
    {
        fragment.setParent(this);
        ContainerManager.getInstance().add(fragment,false);
        ContainerManager.getInstance().hide(this);
        ContainerManager.getInstance().show(fragment);
    }

    public void backToParent()
    {
        if(mParent != null)
        {
//            ContainerManager.getInstance().replace(this, mParent);
            ContainerManager.getInstance().hide(this);
            ContainerManager.getInstance().show(mParent);
        }
    }

    public void setBleService(BleService bleService)
    {
        mBleService = bleService;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Log.d(TAG,"onAttach");
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG,"onViewCreated");
    }

    @Override
    public void onDestroyView() {
        Log.d(TAG,"onDestroyView");
        super.onDestroyView();
    }

    private final int MSG_UI_EVEMT = 1000;
    protected void runOnUiThread(Runnable run)
    {
        Log.d(TAG ,"runOnUiThread");
        Message msg = mUiHandler.obtainMessage(MSG_UI_EVEMT,run);
        mUiHandler.sendMessage(msg);
    }

    @Override
    public boolean handleMessage(Message msg) {
        switch(msg.what)
        {
            case MSG_UI_EVEMT:
                ((Runnable)msg.obj).run();
                Log.d(TAG ,"runOnUiThread finish");
                break;
            default:
                break;
        }
        return false;
    }


    @Override
    public void onDetach() {
        Log.d(TAG,"onDetach");
        super.onDetach();
    }
}
