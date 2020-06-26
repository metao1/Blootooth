package com.metao.app.adapter;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.metao.app.R;
import com.metao.app.activity.MainActivity;
import com.metao.app.listener.AdapterListener;
import com.metao.app.model.DeviceInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import static com.metao.app.model.Constants.TAG;

public class DeviceAdapter extends RecyclerView.Adapter<DeviceAdapter.ViewHolder> {
    private final AdapterListener adapterListener;
    private final List<DeviceInfo> mList;
    private final ConcurrentHashMap<String, Integer> map;
    private LayoutInflater mInflater;

    public DeviceAdapter(Context context, AdapterListener adapterListener) {
        this.mInflater = LayoutInflater.from(context);
        this.adapterListener = adapterListener;
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
        DeviceInfo item = getItem(position);
        if (item == null) {
            return;
        }
        holder.state.setText(item.getStatus().toString());
        Integer integer = item.getRssi().blockingGet();
        holder.rssi.setText(String.valueOf(integer));
        holder.deviceName.setText(item.getDeviceName());
        holder.macAddress.setText(item.getDeviceAddress());
        holder.item.setOnClickListener((v) -> adapterListener.onClick(item.getDeviceAddress()));
    }

    private DeviceInfo getItem(int position) {
        return mList.get(position);
    }

    /**
     * add or update BluetoothDevice
     */
    public void newDevice(MainActivity mainActivity, DeviceInfo info) {
        Integer pos = this.map.get(info.getDeviceAddress());
        if (pos != null) {
            mainActivity.runOnUiThread(() -> notifyItemChanged(pos, info));
        } else {
            this.mList.add(info);
            this.map.put(info.getDeviceAddress(), getItemCount() - 1);
            mainActivity.runOnUiThread(() -> {
                notifyItemChanged(getItemCount() - 1);
            });
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void updateDevice(Activity activity, DeviceInfo deviceInfo) {
        Integer pos = this.map.get(deviceInfo.getDeviceAddress());
        if (pos != null) {
            activity.runOnUiThread(() -> notifyItemChanged(pos, deviceInfo));
            Log.d(TAG, deviceInfo.getDeviceAddress() + ",," + pos + ",," + deviceInfo.getStatus());
        }
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView state, deviceName, macAddress, rssi;
        public LinearLayout item;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            state = itemView.findViewById(R.id.status);
            rssi = itemView.findViewById(R.id.rssi);
            item = itemView.findViewById(R.id.list_device_item);
            deviceName = itemView.findViewById(R.id.device_name);
            macAddress = itemView.findViewById(R.id.mac_address);
        }
    }
}
