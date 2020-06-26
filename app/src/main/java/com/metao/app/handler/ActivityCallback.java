package com.metao.app.handler;

import com.metao.app.model.DeviceInfo;
import com.metao.app.model.BLEService;

import java.util.List;

public interface ActivityCallback {

    void newDevice(final DeviceInfo deviceInfo);

    void updateDevice(final DeviceInfo deviceInfo);

    void retrieveBleServices(final List<BLEService> bleServiceList);
}
