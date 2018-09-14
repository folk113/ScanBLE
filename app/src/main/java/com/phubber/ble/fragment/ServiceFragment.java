package com.phubber.ble.fragment;

import android.app.FragmentTransaction;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattService;
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
import com.phubber.ble.service.IConnectStateListener;
import com.phubber.ble.service.IServiceDiscoverListener;
import com.phubber.ble.utils.GattInfo;
import com.phubber.ble.widget.RecycleViewDivider;

import java.util.List;

public class ServiceFragment extends BaseFragment implements IConnectStateListener, IServiceDiscoverListener {
    private final String TAG = ServiceFragment.class.getSimpleName();
    private BleServiceAdapter mBleServiceAdapter;
    private BluetoothRssi mScanResult;
    private BluetoothGatt mBluetoothGatt;

    public ServiceFragment() {
        super();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate savedInstanceState:" + savedInstanceState);
        mBleService.addConnectStateListener(this);
        mBleService.addServiceDiscoverListener(this);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView inflater:" + inflater + " container:" + container + " savedInstanceState:" + savedInstanceState);
        return inflater.inflate(R.layout.service_layout, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onViewCreated view:" + view + " savedInstanceState:" + savedInstanceState);
        super.onViewCreated(view, savedInstanceState);
        RecyclerView recyclerView = view.findViewById(R.id.service_layout_recyclerView);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.addItemDecoration(new RecycleViewDivider(getContext(), LinearLayoutManager.VERTICAL));
        if(mBleServiceAdapter == null)
            mBleServiceAdapter = new BleServiceAdapter(getContext());
        if(mBluetoothGatt != null)
            mBleServiceAdapter.setGatt(mBluetoothGatt);
        recyclerView.setAdapter(mBleServiceAdapter);
        connectGatt();
    }
    private void connectGatt()
    {
        if(!mBleService.isConnectGatt() || mBluetoothGatt == null)
            mBleService.connectGatt(mScanResult.mBluetoothDevice.getAddress());
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
        mBleService.removeServiceDiscoverListener(this);
        super.onDestroy();
    }

    public void setBluetoothRssi(BluetoothRssi result) {
        Log.d(TAG, "setBluetoothRssi result:" + result);
        mScanResult = result;
        if(mBleService != null)
            mBleService.disconnectGatt();
        if(mBleServiceAdapter != null)
            mBleServiceAdapter.setGatt(null);
        connectGatt();
    }

    @Override
    public void onConnect(final BluetoothGatt gatt) {
        Log.d(TAG, "onConnect gatt:" + gatt);
        if (gatt == null)
            return;
        runOnUiThread(() -> {
            mBluetoothGatt = gatt;
            mBleServiceAdapter.setGatt(gatt);
        });
    }

    @Override
    public void onServiceDiscover(BluetoothGatt gatt) {
        Log.d(TAG, "onServiceDiscover gatt:" + gatt);
        runOnUiThread(() -> {
            mBluetoothGatt = gatt;
            mBleServiceAdapter.notifyDataSetChanged();
        });
    }

    @Override
    public void onDisconnect(final BluetoothGatt gatt) {
        Log.d(TAG, "onDisconnect gatt:" + gatt);
//        ContainerManager.getInstance().remove(ServiceFragment.this);
        runOnUiThread(() ->{
                mBleServiceAdapter.setGatt(null);
                backToParent();});
    }

    //    private CharacteristicFragment mCharacteristicFragment = new CharacteristicFragment();
    public void onItemClickListener(BluetoothGattService service) {
        Log.d(TAG, "onItemClickListener");
        CharacteristicFragment mCharacteristicFragment = null;

        List<Fragment> fragments = getActivity().getSupportFragmentManager().getFragments();
        for(Fragment fragment:fragments)
        {
            if(fragment instanceof CharacteristicFragment)
            {
                mCharacteristicFragment = (CharacteristicFragment)fragment;
                break;
            }
        }
        if(mCharacteristicFragment == null)
            mCharacteristicFragment = new CharacteristicFragment();

        mCharacteristicFragment.setBluetoothGattService(service);
        mCharacteristicFragment.setBleService(mBleService);

        goToChild(mCharacteristicFragment);
    }


    class BleServiceHolder extends RecyclerView.ViewHolder {
        public TextView mTvName;
        public TextView mTvUuid;
        public View mViewHolder;

        public BleServiceHolder(View itemView) {
            super(itemView);
            mViewHolder = itemView;
            mTvName = itemView.findViewById(R.id.service_list_item_name);
            mTvUuid = itemView.findViewById(R.id.service_list_item_uuid);
        }
    }

    private class BleServiceAdapter extends RecyclerView.Adapter<BleServiceHolder> implements View.OnClickListener {
        private Context mContext;
        private BluetoothGatt mGatt;

        public BleServiceAdapter(Context context) {
            mContext = context;
        }

        public synchronized void setGatt(BluetoothGatt gatt) {
            Log.d(TAG, "setGatt:" + gatt);
            mGatt = gatt;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public BleServiceHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(mContext).inflate(R.layout.service_list_item, parent, false);
            return new BleServiceHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull BleServiceHolder holder, int position) {
            BluetoothGattService service = mGatt.getServices().get(position);
            holder.mTvName.setText(GattInfo.uuidToName(service.getUuid()));
            holder.mTvUuid.setText(service.getUuid().toString());
            holder.mViewHolder.setOnClickListener(this);
            holder.mViewHolder.setTag(service);
        }

        @Override
        public synchronized int getItemCount() {
            if (mGatt == null)
                return 0;
            int size = mGatt.getServices().size();
            Log.d(TAG, "getItemCount size:" + size);
            return size;
        }

        @Override
        public void onClick(View v) {
            ServiceFragment.this.onItemClickListener((BluetoothGattService) v.getTag());
        }
    }
}
