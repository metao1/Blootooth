package com.metao.app.handler;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;

import com.metao.app.model.DeviceConnection;
import com.polidea.rxandroidble2.RxBleClient;
import com.polidea.rxandroidble2.RxBleConnection;
import com.polidea.rxandroidble2.RxBleDevice;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class BleScannerHandler extends ScanCallback {

    private static final Object lock = new Object();
    private final RxBleClient rxBleClient;
    private final ExecutorService executorService = Executors.newFixedThreadPool(5);
    private final ConcurrentHashMap<String, DeviceConnection> devices = new ConcurrentHashMap<>();
    private final ActivityCallback callback;

    public BleScannerHandler(ActivityCallback callback, RxBleClient rxBleClient) {
        this.rxBleClient = rxBleClient;
        this.callback = callback;
    }

    @Override
    public void onScanFailed(int errorCode) {
        super.onScanFailed(errorCode);
    }

    @Override
    public void onScanResult(int callbackType, ScanResult result) {
        super.onScanResult(callbackType, result);
        synchronized (lock) {

            BluetoothDevice newDevice = result.getDevice();

            int newRssi = result.getRssi();
            String deviceName = newDevice.getName();
            String deviceAddress = newDevice.getAddress();
            if (deviceName == null) {
                return;
            }
            DeviceConnection dev = devices.get(deviceAddress);
            if (dev != null) {
                callback.updateDevice(dev.getDeviceInfo().setRssi(newRssi).setNewState(dev.getState()));
                if (dev.getState().equals(RxBleConnection.RxBleConnectionState.DISCONNECTED)) {
                    this.devices.remove(deviceAddress);
                }
            } else {
                addToFoundDeviceList(deviceAddress);
            }
        }

    }

    private void addToFoundDeviceList(String deviceAddress) {
        RxBleDevice bleDevice = rxBleClient.getBleDevice(deviceAddress);
        DeviceConnection deviceConnection = new DeviceConnection(bleDevice);
        createNewDevice(deviceConnection);
    }


    private void createNewDevice(DeviceConnection deviceConnection) {
        if (!devices.containsKey(deviceConnection.getDeviceAddress())) {
            this.devices.put(deviceConnection.getDeviceAddress(), deviceConnection);
            BluetoothConnector deviceConnectionFuture = new BluetoothConnector(callback, deviceConnection);
            if (!(executorService.isShutdown() || executorService.isTerminated())) {
                executorService.execute(deviceConnectionFuture);
                callback.newDevice(deviceConnection.getDeviceInfo());
            }
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
        if (executorService != null) {
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
}
