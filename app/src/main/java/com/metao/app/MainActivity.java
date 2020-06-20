package com.metao.app;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.content.Context;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.metao.app.adapter.DeviceAdapter;
import com.metao.app.handler.ActivityCallback;
import com.metao.app.handler.BleScannerHandler;
import com.polidea.rxandroidble2.RxBleClient;

import permissions.dispatcher.PermissionUtils;

public class MainActivity extends AppCompatActivity {
    private DeviceAdapter devicesAdapter;
    private BluetoothAdapter mBTAdapter;
    private RxBleClient rxBleClient;
    private static final int REQUEST_START_SCAN = 0;
    private static final String[] PERMISSION_START_SCAN = new String[]{"android.permission.ACCESS_COARSE_LOCATION"};
    private BluetoothLeScanner bluetoothLeScanner = null;
    private BleScannerHandler bleScannerHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_devices_activity);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        this.rxBleClient = RxBleClient.create(getApplicationContext());
        RecyclerView lv_scan = findViewById(R.id.devices_list);
        this.devicesAdapter = new DeviceAdapter(this);
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
                        devicesAdapter.newDevice(deviceInfo);
                    }

                    @Override
                    public void updateDevice(final DeviceInfo deviceInfo) {
                        devicesAdapter.updateDevice(deviceInfo.getDeviceId(), deviceInfo);
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
        this.bleScannerHandler.clear();
        bluetoothLeScanner.stopScan(this.bleScannerHandler);
        this.bleScannerHandler.dispose();
    }

}
