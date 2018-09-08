package com.phubber.ble.fragment;

import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.phubber.ble.R;
import com.phubber.ble.common.BaseFragment;
import com.phubber.ble.service.IBleScanResultListener;
import com.phubber.ble.utils.ContainerManager;

import java.util.ArrayList;
import java.util.List;

public class DeviceFragment extends BaseFragment implements IBleScanResultListener {
    private final String TAG = DeviceFragment.class.getSimpleName();
    private BleDeviceAdapter mBleDeviceAdapter;
    public DeviceFragment() {
        super();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG,"onCreateView");
        return inflater.inflate(R.layout.device_layout,null);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG,"onViewCreated");
        RecyclerView recyclerView = view.findViewById(R.id.device_layout_recyclerView);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);
        mBleDeviceAdapter = new BleDeviceAdapter(getContext());
        recyclerView.setAdapter(mBleDeviceAdapter);
        recyclerView.postDelayed(()->{
                getBleService().addBleScanResultListener(DeviceFragment.this);
                getBleService().startScanBleDevice();}
        ,3000);
    }

    public void onItemClickListener(ScanResult result)
    {
        ServiceFragment fragment = new ServiceFragment();
        fragment.setScanResult(result);
        ContainerManager.getInstance().add(fragment,true);
    }

    @Override
    public void onDestroyView() {
        getBleService().removeBleScanResultListener(this);
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onScanResult(int ret, List<ScanResult> results) {
        if(ret == 0)
        {
            mBleDeviceAdapter.updateScanResult(results);
        }
    }


    class BleDeviceHolder extends RecyclerView.ViewHolder{
        public TextView mTvDeviceName;
        public TextView mTvDeviceAddr;
        public TextView mTvRssi;
        public View mViewHolder;
        public BleDeviceHolder(View itemView)
        {
            super(itemView);
            mViewHolder = itemView;
            mTvDeviceName = itemView.findViewById(R.id.device_list_item_device_name);
            mTvDeviceAddr = itemView.findViewById(R.id.device_list_item_device_address);
            mTvRssi = itemView.findViewById(R.id.device_list_item_device_rssi);
        }
    }

    private class BleDeviceAdapter extends RecyclerView.Adapter<BleDeviceHolder> implements View.OnClickListener {
        private Context mContext;
        private ArrayList<ScanResult> mScanResults = new ArrayList<ScanResult>();
        public BleDeviceAdapter(Context context)
        {
            mContext = context;
        }

        public void updateScanResult(List<ScanResult> results)
        {
            for(ScanResult newIn :results)
            {
                boolean isExist = false;
                for(ScanResult old:mScanResults)
                {
                    if(newIn.getDevice().getAddress().equals(old.getDevice().getAddress()))
                    {
                       int index =  mScanResults.indexOf(old);
                       mScanResults.remove(old);
                       mScanResults.add(index,newIn);
                       isExist = true;
                       break;
                    }
                }
                if(!isExist)
                    mScanResults.add(newIn);
            }
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public BleDeviceHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(mContext).inflate(R.layout.device_list_item,parent,false);
            return new BleDeviceHolder(itemView);

        }

        @Override
        public void onBindViewHolder(@NonNull BleDeviceHolder holder, int position) {
            ScanResult result = mScanResults.get(position);
            holder.mTvDeviceName.setText(result.getDevice().getName());
            holder.mTvDeviceAddr.setText(result.getDevice().getAddress());
            holder.mTvRssi.setText(result.getRssi()+"");
            holder.mViewHolder.setOnClickListener(this);
            holder.mViewHolder.setTag(result);
        }

        @Override
        public int getItemCount() {
            return mScanResults.size();
        }

        @Override
        public void onClick(View v) {
            DeviceFragment.this.onItemClickListener((ScanResult)v.getTag());
        }
    }
}
