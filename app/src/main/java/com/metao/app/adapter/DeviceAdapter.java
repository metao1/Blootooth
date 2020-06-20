package com.metao.app.adapter;

import android.content.Context;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.metao.app.DeviceInfo;
import com.metao.app.R;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class DeviceAdapter extends RecyclerView.Adapter<DeviceAdapter.ViewHolder> {
    private final List<DeviceInfo> mList;
    private final ConcurrentHashMap<String, Integer> map;
    private LayoutInflater mInflater;

    public DeviceAdapter(Context context) {
        this.mInflater = LayoutInflater.from(context);
        this.mList = new ArrayList<>();
        this.map = new ConcurrentHashMap<>();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.list_device_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DeviceInfo item = (DeviceInfo) getItem(position);
        if (item == null) {
            return;
        }
        holder.state.setText(item.getStatus().toString());
        Integer integer = item.getRssi().blockingGet();
        holder.rssi.setText(String.valueOf(integer));
        holder.deviceName.setText(item.getDeviceName());
        holder.macAddress.setText(item.getDeviceAddress());
    }

    private DeviceInfo getItem(int position) {
        return mList.get(position);
    }

    /**
     * add or update BluetoothDevice
     */
    public void newDevice(DeviceInfo info) {
        this.mList.add(info);
        this.map.put(info.getDeviceId(), 0);
        notifyDataSetChanged();
    }

    public void updateDevice(String deviceId, DeviceInfo deviceInfo) {
        Integer itemPos = this.map.get(deviceId);
        if (itemPos != null) {
            new Handler().postDelayed(() -> notifyItemChanged(itemPos, deviceInfo), 1000);
        }
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView state, deviceName, macAddress, rssi;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            state = (TextView) itemView.findViewById(R.id.status);
            rssi = (TextView) itemView.findViewById(R.id.rssi);
            deviceName = (TextView) itemView.findViewById(R.id.device_name);
            macAddress = (TextView) itemView.findViewById(R.id.mac_address);
        }
    }
}
