package com.metao.app;

import android.util.Log;

import com.polidea.rxandroidble2.RxBleConnection;
import com.polidea.rxandroidble2.RxBleDevice;

import java.util.Iterator;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

import io.reactivex.disposables.Disposable;

public class DeviceConnection {
    private final RxBleDevice device;
    private final DeviceInfo deviceInfo;
    private RxBleConnection connection;
    private Disposable connectionDisposable;
    private ConcurrentLinkedQueue<IRequest> commandToExecute;
    private ConnectionInterface connectionInterface;

    public DeviceConnection(RxBleDevice device) {
        this.device = device;
        this.deviceInfo = new DeviceInfo(device.getName(), device.getMacAddress(), 0, RxBleConnection.RxBleConnectionState.CONNECTING);
        this.commandToExecute = new ConcurrentLinkedQueue<>();
    }

    public void addCommand(IRequest request) {
        this.commandToExecute.add(request);
    }

    public Iterator<IRequest> getAllCommands() {
        return this.commandToExecute.iterator();
    }

    public void runNextCommand() {
        if (this.connection != null) {
            IRequest command = this.commandToExecute.poll();
            if (command != null) {
                this.sendCommand(command);
            }
        }
    }

    public RxBleConnection.RxBleConnectionState getState() {
        return this.device.getConnectionState();
    }

    public void setupConnection(ConnectionInterface connectionInterface) {
        this.connectionInterface = connectionInterface;
        this.connectionDisposable = this.device.establishConnection(true).doFinally(this::dispose)
                .subscribe(this::onConnectReceived,
                        this::onConnectionFailure);

        Disposable disposable = this.device.observeConnectionStateChanges().subscribe(this::onObserveState,
                this::onObserveStateFailure);
        this.connectionInterface.onSetup(disposable);
    }

    private void onObserveState(RxBleConnection.RxBleConnectionState newState) {
        this.connectionInterface.onDeviceStatusChanged(newState);
        this.updateDeviceStatus();
    }

    private void onObserveStateFailure(Throwable e) {
        Log.e(Constants.TAG, "ObserveFailed", e);
        connectionInterface.onDisconnected(e.getMessage());
        this.connection = null;
    }

    private void onConnectReceived(RxBleConnection connection) {
        connectionInterface.onConnected();
        this.connection = connection;
    }

    private void updateDeviceStatus() {
        String address = this.device.getMacAddress();
        RxBleConnection.RxBleConnectionState state = this.device.getConnectionState();

        Log.d(Constants.TAG, address + " : " + state);
    }

    private void onConnectionFailure( Throwable e) {
        Log.e(Constants.TAG, "Connection failure", e);
        this.connectionInterface.onDisconnected(e.getMessage());
        this.connectionInterface.onFailure(e);
        this.connection = null;
    }

    public void dispose() {
        if (this.connectionDisposable != null) {
            this.connectionDisposable.dispose();
            this.connectionDisposable = null;
        }
    }

    private void onWriteSuccess(DeviceInfo deviceInfo) {
        this.connectionInterface.onWriteSuccess();
    }

    private void onWriteFailure(DeviceInfo deviceInfo, Throwable throwable) {
        Log.e(Constants.TAG, "Error writing", throwable);
        this.connectionInterface.onWriteFailed(throwable);
    }

    private void sendCommand(IRequest request) {
        Log.i(Constants.TAG, "Sending command request:" + request.getRequestType().toString());
        if (device.getConnectionState() != RxBleConnection.RxBleConnectionState.CONNECTED) {
            return;
        }

        String command = request.getRequestString();
        Disposable subscribe = this.connection.writeCharacteristic(UUID.fromString(request.getUUID()), HexString.hexToBytes(command)).subscribe((byte[] bytes) ->
                this.onWriteSuccess(deviceInfo), (e) -> this.onWriteFailure(deviceInfo, e));
        Disposable subscribe1 = this.connection.readCharacteristic(UUID.fromString(request.getUUID())).subscribe(this::onReadSuccess, this::onReadFailure);
        Log.i(Constants.TAG, "Command:" + command);
    }

    private void onReadFailure(Throwable throwable) {
        Log.e(Constants.TAG, String.format("Read failure: %s)", throwable.getMessage()));
        this.connectionInterface.onReadFailure(throwable);
    }

    private void onReadSuccess(byte[] bytes) {
        Log.d(Constants.TAG, String.format("Read success: %d bytes)", bytes.length));
        this.connectionInterface.onReadSuccess(bytes);
    }

    public String getDeviceAddress() {
        return deviceInfo.getDeviceAddress();
    }

    public DeviceInfo getDeviceInfo() {
        return deviceInfo;
    }
}
