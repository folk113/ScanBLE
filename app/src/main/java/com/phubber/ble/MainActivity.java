package com.phubber.ble;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.os.Bundle;
import android.util.Log;

import com.phubber.ble.common.BaseActivity;
import com.phubber.ble.fragment.ServiceFragment;
import com.phubber.ble.utils.ContainerManager;

import java.util.List;

public class MainActivity extends BaseActivity {
    private static String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        new ContainerManager(getSupportFragmentManager()).add(new ServiceFragment());
    }

    private void startScan()
    {
        BluetoothAdapter mAdapter = BluetoothAdapter.getDefaultAdapter();
        BluetoothLeScanner scanner =  mAdapter.getBluetoothLeScanner();
        if(scanner != null)
        scanner.startScan(new ScanCallback(){
            @Override
            public void onScanResult(int callbackType, ScanResult result) {
                Log.d(TAG,"onScanResult callbackType:"+callbackType+" result:"+result.toString());
            }
            @Override
            public void onBatchScanResults(List<ScanResult> results) {
                Log.d(TAG,"onBatchScanResults results:"+results.toString());
            }
            @Override
            public void onScanFailed(int errorCode) {
                Log.d(TAG,"onScanFailed errorCode:"+errorCode);
            }


        });
    }
}
