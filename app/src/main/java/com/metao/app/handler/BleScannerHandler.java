package com.metao.app.handler;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;

import com.metao.app.DeviceConnection;
import com.polidea.rxandroidble2.RxBleClient;
import com.polidea.rxandroidble2.RxBleDevice;

import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class BleScannerHandler extends ScanCallback {

    private static final Object lock = new Object();
    private final RxBleClient rxBleClient;
    private final ExecutorService executorService = Executors.newFixedThreadPool(100);
    private final HashMap<String, DeviceConnection> devices = new HashMap<>();
    private final ActivityCallback callback;

    public BleScannerHandler(ActivityCallback callback, RxBleClient rxBleClient) {
        this.rxBleClient = rxBleClient;
        this.callback = callback;
    }

    @Override
    public void onScanResult(int callbackType, ScanResult result) {
        synchronized (lock) {
            super.onScanResult(callbackType, result);

            BluetoothDevice newDevice = result.getDevice();

            int newRssi = result.getRssi();
            String deviceName = newDevice.getName();
            String deviceAddress = newDevice.getAddress();
            if (deviceName == null) {
                return;
            }

            DeviceConnection dev = devices.get(deviceAddress);
            if (dev != null) {
                callback.updateDevice(dev.getDeviceInfo().setRssi(newRssi));
            } else {
                addToFoundDeviceList(deviceAddress);
            }
        }
    }

    private void addToFoundDeviceList(String deviceAddress) {
        RxBleDevice bleDevice = rxBleClient.getBleDevice(deviceAddress);
        DeviceConnection deviceConnection = new DeviceConnection(bleDevice);
        createNewDevice(deviceConnection);
        callback.newDevice(deviceConnection.getDeviceInfo());
    }


    private void createNewDevice(DeviceConnection deviceConnection) {
        if (this.devices.get(deviceConnection.getDeviceAddress()) != null) {
            return;
        }
        if (!devices.containsKey(deviceConnection.getDeviceAddress())) {
            this.devices.put(deviceConnection.getDeviceAddress(), deviceConnection);
            BluetoothConnector deviceConnectionFuture = new BluetoothConnector(callback, deviceConnection);
            executorService.execute(deviceConnectionFuture);
        }
    }


    public void clear() {
        this.devices.clear();
    }

    public void dispose() {
        for (DeviceConnection value : devices.values()) {
            value.dispose();
        }
        stopExecutorService();
    }

    private void stopExecutorService() {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(800, TimeUnit.MILLISECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
        }
    }
}
