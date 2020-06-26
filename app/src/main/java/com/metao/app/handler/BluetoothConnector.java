package com.metao.app.handler;

import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.metao.app.listener.ConnectionListener;
import com.metao.app.model.DeviceConnection;
import com.metao.app.model.BLEService;
import com.polidea.rxandroidble2.RxBleConnection;

import java.util.Base64;
import java.util.List;

import io.reactivex.disposables.Disposable;

import static com.metao.app.model.Constants.TAG;

public final class BluetoothConnector implements Runnable {

    private final ConnectionListenerSetup connectionInterfaceSetup;
    private final DeviceConnection deviceConnection;

    public BluetoothConnector(ActivityCallback ac, DeviceConnection deviceConnection) {
        this.deviceConnection = deviceConnection;
        connectionInterfaceSetup = new ConnectionListenerSetup(ac, deviceConnection);
    }

    @Override
    public void run() {
        Log.d(TAG, "Started to setup connection to " + deviceConnection.getDeviceAddress());
        this.deviceConnection.setupConnection(connectionInterfaceSetup);
    }

    private class ConnectionListenerSetup implements ConnectionListener {

        private final DeviceConnection deviceConnection;
        private final ActivityCallback callback;

        public ConnectionListenerSetup(ActivityCallback callback, DeviceConnection dc) {
            this.deviceConnection = dc;
            this.callback = callback;
        }

        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        public void onConnected(RxBleConnection rxBleConnection) {
            callback.updateDevice(deviceConnection.getDeviceInfo());
            establishConnection(deviceConnection, rxBleConnection);
        }

        @Override
        public void onDeviceStatusChanged(RxBleConnection.RxBleConnectionState newState) {
            callback.updateDevice(deviceConnection.getDeviceInfo().setNewState(newState));
        }

        @Override
        public void onDisconnected(String eMsg) {
            callback.updateDevice(deviceConnection.getDeviceInfo().setNewState(RxBleConnection.RxBleConnectionState.DISCONNECTED));
        }

        @Override
        public void onWriteSuccess() {
            callback.updateDevice(deviceConnection.getDeviceInfo());
        }

        @Override
        public void onWriteFailed(Throwable error) {
            callback.updateDevice(deviceConnection.getDeviceInfo());
        }

        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        public void onReadSuccess(byte[] bytes) {
            if (bytes != null && bytes.length > 0) {
                callback.updateDevice(deviceConnection.getDeviceInfo().setReadData(Base64.getEncoder().encodeToString(bytes)));
            }
        }

        @Override
        public void onReadFailure(Throwable e) {
            callback.updateDevice(deviceConnection.getDeviceInfo().setMessage(e.getMessage()));
        }

        @Override
        public void onFailure(Throwable e) {
            callback.updateDevice(deviceConnection.getDeviceInfo().setMessage(e.getMessage()));
        }

        @Override
        public void onSetup(Disposable disposable) {

        }

        @Override
        public void onResolveBleServices(List<BLEService> bleServiceList) {
            callback.retrieveBleServices(bleServiceList);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void establishConnection(DeviceConnection deviceConnection, RxBleConnection rxBleConnection) {
        if (deviceConnection != null) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            deviceConnection.readServices(rxBleConnection);
            Log.i(TAG, "running commands ->" + deviceConnection.getDeviceAddress());
        }
    }
//
//    public void addAttackCommandToDevice() {
//        deviceConnection.addCommand(new LockOn());
//    }
//
//    public void addUnlockCommandToDevice() {
//        deviceConnection.addCommand(new LockOff());
//    }
}