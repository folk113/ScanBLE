package com.phubber.ble.common;

import android.Manifest;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class BaseActivity extends AppCompatActivity implements PermissionInterface {

    private PermissionHelper mPermissionHelper;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //初始化并发起权限申请
        mPermissionHelper = new PermissionHelper(this, this);
        mPermissionHelper.requestPermissions();
    }
    /**
     * 可设置请求权限请求码
     */
    @Override
    public int getPermissionsRequestCode() {
        return 0;
    }

    /**
     * 设置需要请求的权限
     */
    @Override
    public String[] getPermissions() {
        return new String[]{
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.ACCESS_COARSE_LOCATION
        };
    }

    /**
     * 请求权限成功回调
     */
    @Override
    public void requestPermissionsSuccess() {
    }

    /**
     * 请求权限失败回调
     */
    @Override
    public void requestPermissionsFail() {
        finish();
    }
}
