package com.metao.app.handler;

import com.metao.app.DeviceInfo;

public interface ActivityCallback {

    void newDevice(final DeviceInfo deviceInfo);

    void updateDevice(final DeviceInfo deviceInfo);
}
