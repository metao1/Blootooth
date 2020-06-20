package com.metao.app.handler;

import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.metao.app.ConnectionInterface;
import com.metao.app.Constants;
import com.metao.app.DeviceConnection;
import com.polidea.rxandroidble2.RxBleConnection;

import java.util.Base64;

import io.reactivex.disposables.Disposable;

public final class BluetoothConnector implements Runnable {

    private final ConnectionInterfaceSetup connectionInterfaceSetup;
    private final DeviceConnection deviceConnection;

    public BluetoothConnector(ActivityCallback ac, DeviceConnection deviceConnection) {
        this.deviceConnection = deviceConnection;
        connectionInterfaceSetup = new ConnectionInterfaceSetup(ac, deviceConnection);
    }

    @Override
    public void run() {
        this.deviceConnection.setupConnection(connectionInterfaceSetup);
    }

    private class ConnectionInterfaceSetup implements ConnectionInterface {

        private final DeviceConnection deviceConnection;
        private final ActivityCallback callback;

        public ConnectionInterfaceSetup(ActivityCallback callback, DeviceConnection dc) {
            this.deviceConnection = dc;
            this.callback = callback;
        }

        @Override
        public void onConnected() {
            callback.updateDevice(deviceConnection.getDeviceInfo());
            establishConnection(deviceConnection);
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
            callback.updateDevice(deviceConnection.getDeviceInfo().setReadData(Base64.getEncoder().encodeToString(bytes)));
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
    }

    private void establishConnection(DeviceConnection deviceConnection) {
        if (deviceConnection != null) {
            while (deviceConnection.getAllCommands().hasNext()) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Log.i(Constants.TAG, "running commands ->" + deviceConnection.getDeviceAddress());
                deviceConnection.runNextCommand();
            }
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