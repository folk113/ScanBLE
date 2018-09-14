package com.phubber.ble.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.phubber.ble.R;
import com.phubber.ble.common.BaseFragment;
import com.phubber.ble.service.BluetoothRssi;
import com.phubber.ble.service.IDiscoveryResultListener;
import com.phubber.ble.widget.RecycleViewDivider;

import java.util.ArrayList;
import java.util.List;

public class DeviceFragment extends BaseFragment implements IDiscoveryResultListener {
    private final String TAG = DeviceFragment.class.getSimpleName();
    private BleDeviceAdapter mBleDeviceAdapter;

    public DeviceFragment() {
        super();
    }



    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate savedInstanceState:" + savedInstanceState);
        mBleService.addBleScanResultListener(this);
        mBleService.startDiscovery();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView inflater:" + inflater + " container:" + container + " savedInstanceState:" + savedInstanceState);
        return inflater.inflate(R.layout.device_layout, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onViewCreated view:" + view + " savedInstanceState:" + savedInstanceState);
        super.onViewCreated(view, savedInstanceState);
        RecyclerView recyclerView = view.findViewById(R.id.device_layout_recyclerView);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.addItemDecoration(new RecycleViewDivider(getContext(), LinearLayoutManager.VERTICAL));
        if(mBleDeviceAdapter == null)
            mBleDeviceAdapter = new BleDeviceAdapter(getContext());
        if(mBluetoothRssis.size() > 0)
            mBleDeviceAdapter.updateScanResults(mBluetoothRssis);
        recyclerView.setAdapter(mBleDeviceAdapter);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.d(TAG, "onActivityCreated savedInstanceState:" + savedInstanceState);
    }

    //    private ServiceFragment mServiceFragment = new ServiceFragment();
    public void onItemClickListener(BluetoothRssi result) {
        Log.d(TAG, "onItemClickListener");
        ServiceFragment mServiceFragment = null;
        List<Fragment> fragments = getActivity().getSupportFragmentManager().getFragments();
        for(Fragment fragment:fragments)
        {
            if(fragment instanceof ServiceFragment)
            {
                mServiceFragment = (ServiceFragment)fragment;
                break;
            }
        }
        if(mServiceFragment == null)
            mServiceFragment = new ServiceFragment();
        mServiceFragment.setBleService(mBleService);
        mServiceFragment.setBluetoothRssi(result);
        goToChild(mServiceFragment);
    }


    @Override
    public void onDestroyView() {
        Log.d(TAG, "onDestroyView");
        mBleService.removeBleScanResultListener(this);
        super.onDestroyView();
    }
    private ArrayList<BluetoothRssi> mBluetoothRssis = new ArrayList<BluetoothRssi>();
    @Override
    public void onDiscoveryResult(int ret, List<BluetoothRssi> results) {
        Log.d(TAG, "onDiscoveryResult ret:" + ret + " results:" + results);
        if (ret == 0) {
            mBleDeviceAdapter.updateScanResults(results);
            mBluetoothRssis.addAll(results);
        }
    }

    @Override
    public void onDiscoveryResult(int ret, BluetoothRssi result) {
        Log.d(TAG, "onDiscoveryResult ret:" + ret + " results:" + result);
        if (ret == 0) {
            mBleDeviceAdapter.updateScanResult(result);
            mBluetoothRssis.add(result);
        }
    }

    class BleDeviceHolder extends RecyclerView.ViewHolder {
        public TextView mTvDeviceName;
        public TextView mTvDeviceAddr;
        public TextView mTvRssi;
        public View mViewHolder;

        public BleDeviceHolder(View itemView) {
            super(itemView);
            mViewHolder = itemView;
            mTvDeviceName = itemView.findViewById(R.id.device_list_item_device_name);
            mTvDeviceAddr = itemView.findViewById(R.id.device_list_item_device_address);
            mTvRssi = itemView.findViewById(R.id.device_list_item_device_rssi);
        }
    }

    private class BleDeviceAdapter extends RecyclerView.Adapter<BleDeviceHolder> implements View.OnClickListener {
        private Context mContext;
        private ArrayList<BluetoothRssi> mScanResults = new ArrayList<>();

        public BleDeviceAdapter(Context context) {
            mContext = context;
        }

        public void updateScanResults(List<BluetoothRssi> results) {
            for (BluetoothRssi newIn : results) {
                boolean isExist = false;
                for (BluetoothRssi old : mScanResults) {
                    if (newIn.mBluetoothDevice.getAddress().equals(old.mBluetoothDevice.getAddress())) {
                        int index = mScanResults.indexOf(old);
                        mScanResults.remove(old);
                        mScanResults.add(index, newIn);
                        isExist = true;
                        break;
                    }
                }
                if (!isExist)
                    mScanResults.add(newIn);
            }
            if(DeviceFragment.this.isVisible())
                notifyDataSetChanged();
        }

        public void updateScanResult(BluetoothRssi result) {
            boolean isExist = false;
            for (BluetoothRssi old : mScanResults) {
                if (result.mBluetoothDevice.getAddress().equals(old.mBluetoothDevice.getAddress())) {
                    int index = mScanResults.indexOf(old);
                    mScanResults.remove(old);
                    mScanResults.add(index, result);
                    isExist = true;
                    break;
                }
            }
            if (!isExist)
                mScanResults.add(result);

            if(DeviceFragment.this.isVisible())
                notifyDataSetChanged();
        }

        @NonNull
        @Override
        public BleDeviceHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(mContext).inflate(R.layout.device_list_item, parent, false);
            return new BleDeviceHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull BleDeviceHolder holder, int position) {
            BluetoothRssi result = mScanResults.get(position);
            holder.mTvDeviceName.setText(result.mBluetoothDevice.getName());
            holder.mTvDeviceAddr.setText(result.mBluetoothDevice.getAddress());
            holder.mTvRssi.setText(result.mRssi + "");
            holder.mViewHolder.setOnClickListener(this);
            holder.mViewHolder.setTag(result);
        }

        @Override
        public int getItemCount() {
            return mScanResults.size();
        }

        @Override
        public void onClick(View v) {
            DeviceFragment.this.onItemClickListener((BluetoothRssi) v.getTag());
        }
    }
}
