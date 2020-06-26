package com.metao.app.listener;

import com.metao.app.model.BLEService;
import com.polidea.rxandroidble2.RxBleConnection;

import java.util.List;

import io.reactivex.disposables.Disposable;

public interface ConnectionListener {

    void onConnected(RxBleConnection connection);

    void onDisconnected(String eMsg);

    void onReadSuccess(byte[] bytes);

    void onDeviceStatusChanged(RxBleConnection.RxBleConnectionState newState);

    void onWriteSuccess();

    void onWriteFailed(Throwable throwable);

    void onReadFailure(Throwable e);

    void onFailure(Throwable e);

    void onSetup(Disposable disposable);

    void onResolveBleServices(List<BLEService> bleServiceList);
}
