package com.metao.app.activity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.metao.app.R;
import com.metao.app.adapter.DeviceAdapter;
import com.metao.app.handler.ActivityCallback;
import com.metao.app.handler.BleScannerHandler;
import com.metao.app.model.BLEService;
import com.metao.app.model.Constants;
import com.metao.app.model.DeviceInfo;
import com.polidea.rxandroidble2.RxBleClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import permissions.dispatcher.PermissionUtils;

public class MainActivity extends AppCompatActivity {
    private DeviceAdapter devicesAdapter;
    private BluetoothAdapter mBTAdapter;
    private RxBleClient rxBleClient;
    private static final int REQUEST_START_SCAN = 0;
    private static final String[] PERMISSION_START_SCAN = new String[]{"android.permission.ACCESS_COARSE_LOCATION"};
    private Map<String, BLEService> globalBleServiceList = new HashMap<>();
    private BluetoothLeScanner bluetoothLeScanner = null;
    private BleScannerHandler bleScannerHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_devices_activity);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        this.rxBleClient = RxBleClient.create(getApplicationContext());
        RecyclerView lv_scan = findViewById(R.id.devices_list);
        this.devicesAdapter = new DeviceAdapter(this, macAddress -> {
            if (this.globalBleServiceList.containsKey(macAddress)) {
                BLEService bleService = this.globalBleServiceList.get(macAddress);
                openDetailsActivity(bleService);
            }
        });
        lv_scan.setAdapter(this.devicesAdapter);
        lv_scan.setLayoutManager(layoutManager);
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (!PermissionUtils.hasSelfPermissions(this, MainActivity.PERMISSION_START_SCAN)) {
            ActivityCompat.requestPermissions(this, MainActivity.PERMISSION_START_SCAN, MainActivity.REQUEST_START_SCAN);
        }
        BluetoothManager btManager = (BluetoothManager) this.getSystemService(Context.BLUETOOTH_SERVICE);
        if (btManager != null) {
            mBTAdapter = btManager.getAdapter();
            bluetoothLeScanner = this.mBTAdapter.getBluetoothLeScanner();
            if (bleScannerHandler == null) {
                bleScannerHandler = new BleScannerHandler(new ActivityCallback() {
                    @Override
                    public void newDevice(final DeviceInfo deviceInfo) {
                        devicesAdapter.newDevice(MainActivity.this, deviceInfo);
                    }

                    @Override
                    @RequiresApi(api = Build.VERSION_CODES.N)
                    public void updateDevice(final DeviceInfo deviceInfo) {
                        devicesAdapter.updateDevice(MainActivity.this, deviceInfo);
                    }

                    @RequiresApi(api = Build.VERSION_CODES.N)
                    @Override
                    public void retrieveBleServices(List<BLEService> bleServiceList) {
                        bleServiceList.forEach(bleService -> globalBleServiceList.put(bleService.getMacAddress(), bleService));
                    }
                }, rxBleClient);
            }
            startScan();
        } else {
            Toast.makeText(this, "no adapter for bluetooth found on this device", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        stopScan();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopScan();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopScan();
    }

    @Override
    protected void onStart() {
        super.onStart();
        startScan();
    }

    private void openDetailsActivity(BLEService bleService) {
        Intent intent = new Intent(this, DetailsActivity.class);
        intent.putExtra("parcelable_extra", bleService);
        startActivity(intent);
    }

    void startScan() {
        if (this.mBTAdapter != null) {
            RxBleClient client = this.rxBleClient;
            RxBleClient.State state = client.getState();
            if (state == RxBleClient.State.READY) {
                bluetoothLeScanner.startScan(this.bleScannerHandler);
            } else {
                Toast.makeText(this, "Enable bluetooth", Toast.LENGTH_LONG).show();
                stopScan();
            }

        }
    }

    private void stopScan() {
        if (this.bleScannerHandler != null) {
            this.bleScannerHandler.clear();
            this.bleScannerHandler.dispose();
        }
        if (this.bluetoothLeScanner != null) {
            this.bluetoothLeScanner.stopScan(this.bleScannerHandler);
        }
        Log.e(Constants.TAG, "Scan stopped");
    }
}
