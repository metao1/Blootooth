package com.metao.app.model;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.metao.app.handler.BLENameResolver;
import com.metao.app.listener.ConnectionListener;
import com.polidea.rxandroidble2.RxBleConnection;
import com.polidea.rxandroidble2.RxBleDevice;
import com.polidea.rxandroidble2.RxBleDeviceServices;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

import io.reactivex.disposables.Disposable;

import static com.metao.app.model.Constants.TAG;

public class DeviceConnection {
    private final RxBleDevice device;
    private final DeviceInfo deviceInfo;
    private RxBleConnection connection;
    private Disposable connectionDisposable;
    private ConcurrentLinkedQueue<IRequest> commandToExecute;
    private ConnectionListener connectionListener;

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

    @RequiresApi(api = Build.VERSION_CODES.O)
    @SuppressLint("CheckResult")
    public void readServices(RxBleConnection connection) {
        if (connection != null && this.device.getConnectionState() == RxBleConnection.RxBleConnectionState.CONNECTED) {
            connection.discoverServices(10, TimeUnit.SECONDS)
                    .subscribe((services) -> {
                        Log.d(TAG, "Retrieving services and characteristics from connection");
                        connectionListener.onResolveBleServices(retrieveServices(services));
                    });
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private List<BLEService> retrieveServices(RxBleDeviceServices services) {
        List<BLEService> bleServices = new ArrayList<>();
        for (BluetoothGattService bleSrv : services.getBluetoothGattServices()) {
            if (BLENameResolver.isService(bleSrv.getUuid().toString())) {
                BLEService service = BLEService.Builder();

                service.addName(BLENameResolver.resolveUuid(bleSrv.getUuid().toString()));
                service.addUuid(bleSrv.getUuid().toString());
                service.addMacAddress(device.getMacAddress());
                List<BluetoothGattCharacteristic> bluetoothGattServiceCharacteristics = bleSrv.getCharacteristics();
                bluetoothGattServiceCharacteristics.stream()
                        .map(bluetoothGattCharacteristic -> bluetoothGattCharacteristic.getUuid().toString())
                        .filter(BLENameResolver::isCharacteristic)
                        .filter(Objects::nonNull)
                        .forEach(val -> {
                            ServiceCharacteristic sc = ServiceCharacteristic.Builder();
                            Log.d(TAG, "characteristic：" + val);
                            sc.addName(BLENameResolver.resolveCharacteristicName(val));
                            sc.addCharacteristicId(val);
                            service.addServiceCharacteristic(sc);
                        });

                bleServices.add(service);
            }
        }

        Log.d(TAG, "Service：" + Arrays.toString(bleServices.toArray()));
        return bleServices;
    }

    public RxBleConnection.RxBleConnectionState getState() {
        return this.device.getConnectionState();
    }

    public void setupConnection(ConnectionListener connectionListener) {
        this.connectionListener = connectionListener;
        this.connectionDisposable = this.device.establishConnection(true).doFinally(this::dispose)
                .subscribe(this::onConnectReceived,
                        this::onConnectionFailure);

        Disposable disposable = this.device.observeConnectionStateChanges().subscribe(this::onObserveState,
                this::onObserveStateFailure);
        this.connectionListener.onSetup(disposable);
    }

    private void onObserveState(RxBleConnection.RxBleConnectionState newState) {
        this.updateDeviceStatus(newState);
    }

    private void onObserveStateFailure(Throwable e) {
        Log.e(TAG, "ObserveFailed", e);
        connectionListener.onDisconnected(e.getMessage());
        this.connection = null;
    }

    private void onConnectReceived(RxBleConnection connection) {
        Log.d(TAG, "Device connected! " + device.getMacAddress());
        connectionListener.onConnected(connection);
        this.connection = connection;
    }

    private void updateDeviceStatus(RxBleConnection.RxBleConnectionState newState) {
        String address = this.device.getMacAddress();
        Log.d(TAG, address + " : " + newState);
        this.connectionListener.onDeviceStatusChanged(newState);
    }

    private void onConnectionFailure(Throwable e) {
        Log.e(TAG, "Connection failure", e);
        this.connectionListener.onDisconnected(e.getMessage());
        this.connectionListener.onFailure(e);
        this.connection = null;
    }

    public void dispose() {
        if (this.connectionDisposable != null) {
            this.connectionDisposable.dispose();
            this.connectionDisposable = null;
        }
    }

    private void onWriteSuccess(DeviceInfo deviceInfo) {
        this.connectionListener.onWriteSuccess();
    }

    private void onWriteFailure(DeviceInfo deviceInfo, Throwable throwable) {
        Log.e(TAG, "Error writing", throwable);
        this.connectionListener.onWriteFailed(throwable);
    }

    private void sendCommand(IRequest request) {
        Log.i(TAG, "Sending command request:" + request.getRequestType().toString());
        if (device.getConnectionState() != RxBleConnection.RxBleConnectionState.CONNECTED) {
            return;
        }

        String command = request.getRequestString();
        Disposable subscribe = this.connection.writeCharacteristic(UUID.fromString(request.getUUID()), HexString.hexToBytes(command)).subscribe((byte[] bytes) ->
                this.onWriteSuccess(deviceInfo), (e) -> this.onWriteFailure(deviceInfo, e));
        Disposable subscribe1 = this.connection.readCharacteristic(UUID.fromString(request.getUUID())).subscribe(this::onReadSuccess, this::onReadFailure);
        Log.i(TAG, "Command:" + command);
    }

    private void onReadFailure(Throwable throwable) {
        Log.e(TAG, String.format("Read failure: %s)", throwable.getMessage()));
        this.connectionListener.onReadFailure(throwable);
    }

    private void onReadSuccess(byte[] bytes) {
        Log.d(TAG, String.format("Read success: %d bytes)", bytes.length));
        this.connectionListener.onReadSuccess(bytes);
    }

    public String getDeviceAddress() {
        return deviceInfo.getDeviceAddress();
    }

    public DeviceInfo getDeviceInfo() {
        return deviceInfo;
    }
}
