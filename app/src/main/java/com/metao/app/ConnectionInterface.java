package com.metao.app;

import com.polidea.rxandroidble2.RxBleConnection;

import io.reactivex.disposables.Disposable;

public interface ConnectionInterface {

    void onConnected();

    void onDisconnected(String eMsg);

    void onReadSuccess(byte[] bytes);

    void onDeviceStatusChanged(RxBleConnection.RxBleConnectionState newState);

    void onWriteSuccess();

    void onWriteFailed(Throwable throwable);

    void onReadFailure(Throwable e);

    void onFailure(Throwable e);

    void onSetup(Disposable disposable);
}
