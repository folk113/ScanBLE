package com.phubber.ble;

import android.os.Bundle;

import com.phubber.ble.common.BaseActivity;
import com.phubber.ble.fragment.DeviceFragment;
import com.phubber.ble.utils.ContainerManager;


public class MainActivity extends BaseActivity {
    private static String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ContainerManager.getInstance(getSupportFragmentManager()).add(new DeviceFragment(),false);
    }
}
