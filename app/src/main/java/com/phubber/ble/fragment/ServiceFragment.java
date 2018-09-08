package com.phubber.ble.fragment;

import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.os.Bundle;
import android.os.ParcelUuid;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
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
import com.phubber.ble.utils.ContainerManager;
import com.phubber.ble.utils.GattInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ServiceFragment extends BaseFragment{
    private final String TAG = ServiceFragment.class.getSimpleName();
    private BleServiceAdapter mBleServiceAdapter;
    private ScanResult mScanResult;
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
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);
        mBleServiceAdapter = new BleServiceAdapter(getContext(),mScanResult);
        recyclerView.setAdapter(mBleServiceAdapter);
    }

    public void setScanResult(ScanResult result)
    {
        mScanResult = result;
    }

    public void onItemClickListener(UUID uuid)
    {

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    class BleServiceHolder extends RecyclerView.ViewHolder{
        public TextView mTvName;
        public TextView mTvUuid;
        public View mViewHolder;
        public BleServiceHolder(View itemView)
        {
            super(itemView);
            mViewHolder = itemView;
            mTvName = itemView.findViewById(R.id.service_list_item_name);
            mTvUuid = itemView.findViewById(R.id.service_list_item_uuid);
        }
    }

    private class BleServiceAdapter extends RecyclerView.Adapter<BleServiceHolder> implements View.OnClickListener {
        private Context mContext;
        private ScanResult mScanResult;
        public BleServiceAdapter(Context context,ScanResult result)
        {
            mContext = context;
            mScanResult = result;
        }

        @NonNull
        @Override
        public BleServiceHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(mContext).inflate(R.layout.service_list_item,parent,false);
            return new BleServiceHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull BleServiceHolder holder, int position) {
            ParcelUuid uuid = mScanResult.getScanRecord().getServiceUuids().get(position);
            holder.mTvName.setText(GattInfo.uuidToName(uuid.getUuid()));
            holder.mTvUuid.setText(uuid.getUuid().toString());
            holder.mViewHolder.setOnClickListener(this);
            holder.mViewHolder.setTag(uuid.getUuid());
        }

        @Override
        public int getItemCount() {
            return mScanResult.getScanRecord().getServiceUuids().size();
        }

        @Override
        public void onClick(View v) {
            ServiceFragment.this.onItemClickListener((UUID)v.getTag());
        }
    }
}
