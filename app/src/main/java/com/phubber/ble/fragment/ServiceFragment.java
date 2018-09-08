package com.phubber.ble.fragment;

import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.phubber.ble.MarqueeView;
import com.phubber.ble.R;
import com.phubber.ble.common.BaseFragment;
import com.phubber.ble.service.IBleScanResultListener;

import java.util.ArrayList;
import java.util.List;

public class ServiceFragment extends BaseFragment implements IBleScanResultListener {
    private final String TAG = ServiceFragment.class.getSimpleName();
    private BleServiceAdapter mBleServiceAdapter;
    public ServiceFragment() {
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
        return inflater.inflate(R.layout.service_layout,null);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG,"onViewCreated");
        RecyclerView recyclerView = view.findViewById(R.id.service_layout_recyclerView);
        mBleServiceAdapter = new BleServiceAdapter(getContext());
        recyclerView.setAdapter(mBleServiceAdapter);
        recyclerView.postDelayed(new Runnable(){
            @Override
            public void run() {
                getBleService().addBleScanResultListener(ServiceFragment.this);
                getBleService().startScanBleDevice();
            }
        },3000);
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
            mBleServiceAdapter.updateScanResult(results);
        }
    }


    class BleServiceHolder extends RecyclerView.ViewHolder{
        public TextView mTvDeviceName;
        public TextView mTvDeviceAddr;
        public MarqueeView mMvUuid;
        public TextView mTvRssi;
        public BleServiceHolder(View itemView)
        {
            super(itemView);
            mTvDeviceName = itemView.findViewById(R.id.device_name);
            mTvDeviceAddr = itemView.findViewById(R.id.device_address);
            mMvUuid = itemView.findViewById(R.id.device_beacon_uuid);
            mTvRssi = itemView.findViewById(R.id.device_txPower_rssi);
        }
    }
    private class BleServiceAdapter extends RecyclerView.Adapter{
        private Context mContext;
        private ArrayList<ScanResult> mScanResults = new ArrayList<ScanResult>();
        public BleServiceAdapter(Context context)
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
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(mContext).inflate(R.layout.listitem_device,parent);
            return new BleServiceHolder(itemView);
        }


        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            BleServiceHolder _holder = (BleServiceHolder)holder;
            ScanResult result = mScanResults.get(position);
            _holder.mTvDeviceName.setText(result.getDevice().getName());
        }

        @Override
        public int getItemCount() {
            return mScanResults.size();
        }

        @Override
        public long getItemId(int position) {
            return position;
        }
    }
}
