package com.phubber.ble.fragment;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
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
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.phubber.ble.R;
import com.phubber.ble.common.BaseFragment;
import com.phubber.ble.service.IConnectStateListener;
import com.phubber.ble.service.IDataAvailableListener;
import com.phubber.ble.utils.GattInfo;
import com.phubber.ble.widget.RecycleViewDivider;

import java.util.ArrayList;

public class CharacteristicFragment extends BaseFragment implements IConnectStateListener, IDataAvailableListener {
    private final String TAG = CharacteristicFragment.class.getSimpleName();
    private BleCharacteristicAdapter mBleServiceAdapter;
    private BluetoothGattService mBluetoothGattService;

    public CharacteristicFragment() {
        super();
    }

    public void setBluetoothGattService(BluetoothGattService service) {
        Log.d(TAG, "setBluetoothGattService service:" + service);
        mBluetoothGattService = service;
        if(mBleServiceAdapter != null)
            mBleServiceAdapter.setBluetoothGattService(mBluetoothGattService);
        mReadDataReq.clear();
        mReadDataReq.addAll(mBluetoothGattService.getCharacteristics());
        if (mReadDataReq.size() > 0 && mBleService != null) {
            mBleService.readCharacteristic(mReadDataReq.remove(0));
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate savedInstanceState:" + savedInstanceState);
        mBleServiceAdapter = new BleCharacteristicAdapter(getContext());
        mBleService.addConnectStateListener(this);
        mBleService.addDataAvailableListener(this);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView inflater:" + inflater + " container:" + container + " savedInstanceState:" + savedInstanceState);
        View view = inflater.inflate(R.layout.characteristic_layout, container, false);
        return view;
    }

    private RecyclerView mRecyclerView;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onViewCreated view:" + view + " savedInstanceState:" + savedInstanceState);
        super.onViewCreated(view, savedInstanceState);
        mRecyclerView = view.findViewById(R.id.characteristic_layout_recyclerView);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        mRecyclerView.addItemDecoration(new RecycleViewDivider(getContext(), LinearLayoutManager.VERTICAL));
        Log.d(TAG, "onServiceConnected mReadDataReq size:" + mReadDataReq.size());
        mBleServiceAdapter.setBluetoothGattService(mBluetoothGattService);
        if (mReadDataReq.size() > 0) {
            mBleService.readCharacteristic(mReadDataReq.remove(0));
        }
        mRecyclerView.setAdapter(mBleServiceAdapter);
        mRecyclerView.postInvalidate();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.d(TAG, "onActivityCreated savedInstanceState:" + savedInstanceState);
    }

    @Override
    public void onDestroyView() {
        Log.d(TAG, "onDestroyView");
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        mBleService.removeConnectStateListener(this);
        mBleService.removeDataAvailableListener(this);
        super.onDestroy();
    }

    @Override
    public void onConnect(final BluetoothGatt gatt) {
        Log.d(TAG, "onConnect gatt:" + gatt);
        if (gatt == null)
            return;
    }

    @Override
    public void onDisconnect(final BluetoothGatt gatt) {
        Log.d(TAG, "onDisconnect gatt:" + gatt);
//        ContainerManager.getInstance().remove(CharacteristicFragment.this);
    }

    ArrayList<BluetoothGattCharacteristic> mReadDataReq = new ArrayList<>();

    @Override
    public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        String value = characteristic.getStringValue(0);
        Log.d(TAG, "onCharacteristicRead gatt:" + gatt + " value:" + value + " characteristic:" + characteristic + " status:" + status);
        Log.d(TAG, "onCharacteristicRead mReadDataReq size:" + mReadDataReq.size());
        if (mReadDataReq.size() > 0) {
            mBleService.readCharacteristic(mReadDataReq.remove(0));
        }
        mBleServiceAdapter.addBluetoothGattCharacteristic(characteristic);
    }

    @Override
    public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        Log.d(TAG, "onCharacteristicWrite gatt:" + gatt + " characteristic:" + characteristic);
    }

    public void onItemClickListener(BluetoothGattCharacteristic characteristic) {

    }


    class BleCharacteristicHolder extends RecyclerView.ViewHolder {
        public TextView mTvDesc;
        public TextView mTvValue;
        public View mViewHolder;

        public BleCharacteristicHolder(View itemView) {
            super(itemView);
            mViewHolder = itemView;
            mTvDesc = itemView.findViewById(R.id.characteristic_list_item_desc);
            mTvValue = itemView.findViewById(R.id.characteristic_list_item_value);
        }
    }


    private class BleCharacteristicAdapter extends RecyclerView.Adapter<BleCharacteristicHolder> implements View.OnClickListener {
        private Context mContext;
        private ArrayList<BluetoothGattCharacteristic> mBluetoothGattCharacteristics = new ArrayList<>();

        public BleCharacteristicAdapter(Context context) {
            mContext = context;
        }

        public synchronized void addBluetoothGattCharacteristic(BluetoothGattCharacteristic bluetoothGattCharacteristic) {
            Log.d(TAG, "addBluetoothGattCharacteristic");
            for (BluetoothGattCharacteristic characteristic : mBluetoothGattCharacteristics) {
                if (characteristic.getUuid().equals(bluetoothGattCharacteristic.getUuid())) {
                    int index = mBluetoothGattCharacteristics.indexOf(characteristic);
                    mBluetoothGattCharacteristics.remove(index);
                    mBluetoothGattCharacteristics.add(index, bluetoothGattCharacteristic);
                    notifyItemChanged(index);
                    break;
                }
            }
            Log.d(TAG, "addBluetoothGattCharacteristic finish");
        }

        public synchronized void setBluetoothGattService(BluetoothGattService gattService) {
            Log.d(TAG, "setBluetoothGattService:" + gattService);
            mBluetoothGattCharacteristics.clear();
            notifyDataSetChanged();

            for (BluetoothGattCharacteristic characteristic : gattService.getCharacteristics()) {
                mBluetoothGattCharacteristics.add(characteristic);
            }
            notifyDataSetChanged();
            Log.d(TAG, "setBluetoothGattService finish");
        }

        @NonNull
        @Override
        public BleCharacteristicHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(mContext).inflate(R.layout.characteristic_list_item, parent, false);
            return new BleCharacteristicHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull BleCharacteristicHolder holder, int position) {
            BluetoothGattCharacteristic characteristic = mBluetoothGattCharacteristics.get(position);
            holder.mTvDesc.setText(GattInfo.uuidToName(characteristic.getUuid()) + "\r\n" + characteristic.getProperties());
            String value = characteristic.getStringValue(0);//Arrays.toString(characteristic.getValue());
            holder.mTvValue.setText(value);//characteristic.getUuid().toString()
            int writeType = characteristic.getWriteType();
            Log.d(TAG, "onBindViewHolder writeType:" + writeType);
            if (writeType <= BluetoothGattCharacteristic.PERMISSION_READ_ENCRYPTED_MITM)
                holder.mTvValue.setEnabled(false);
            holder.mViewHolder.setOnClickListener(this);
            holder.mViewHolder.setTag(characteristic);
        }

        @Override
        public synchronized int getItemCount() {
            Log.d(TAG, "getItemCount");
            int size = mBluetoothGattCharacteristics.size();
            Log.d(TAG, "getItemCount size:" + size);
            return size;
        }

        @Override
        public void onClick(View v) {
            CharacteristicFragment.this.onItemClickListener((BluetoothGattCharacteristic) v.getTag());
        }
    }
}
