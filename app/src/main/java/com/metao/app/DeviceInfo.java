package com.metao.app;

import com.polidea.rxandroidble2.RxBleConnection;

import java.util.UUID;

import io.reactivex.Single;

public class DeviceInfo {

    private final String deviceId = UUID.randomUUID().toString().replaceAll("-", "");
    private final String deviceAddress;
    private final String deviceName;
    private RxBleConnection.RxBleConnectionState status;
    private Single<Integer> rssi;
    private String message;
    private String dataInString;

    public DeviceInfo(String deviceName, String deviceAddress, int rssi, RxBleConnection.RxBleConnectionState status) {
        this.deviceName = deviceName;
        this.rssi = Single.just(rssi);
        this.deviceAddress = deviceAddress;
        this.status = status;
    }

    public Single<Integer> getRssi() {
        return rssi;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public String getDeviceAddress() {
        return deviceAddress;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public DeviceInfo setRssi(int newRssi) {
        this.rssi = Single.just(newRssi);
        return this;
    }

    public RxBleConnection.RxBleConnectionState getStatus() {
        return status;
    }

    public String getDataInString() {
        return dataInString;
    }

    public String getMessage() {
        return message;
    }

    public DeviceInfo setNewState(RxBleConnection.RxBleConnectionState newState) {
        this.status = newState;
        return this;
    }

    public DeviceInfo setReadData(String dataInString) {
        this.dataInString = dataInString;
        return this;
    }

    public DeviceInfo setMessage(String message) {
        this.message = message;
        return this;
    }
}
