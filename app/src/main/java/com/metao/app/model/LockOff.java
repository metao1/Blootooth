package com.metao.app.model;

import java.util.Arrays;

public class LockOff implements IRequest {
    private static int delay = 100;
    private final String requestBit = "71";
    private final RequestType requestType = RequestType.NOCOUNT;
    private final String uuid;
    private long startTime;

    public LockOff(String uuid) {
        this.startTime = System.currentTimeMillis() + delay;
        this.uuid = uuid;
    }

    @Override
    public int getDelay() {
        return delay;
    }

    @Override
    public String getRequestString() {
        return new NbMessage()
                .setDirection(NbCommands.MASTER_TO_M365)
                .setRW(NbCommands.WRITE)
                .setPosition(0x71)
                .setPayload(0x0001)
                .build();
    }

    @Override
    public String getUUID() {
        return uuid;
    }

    @Override
    public String getRequestBit() {
        return requestBit;
    }

    @Override
    public String handleResponse(String[] request) {
        return Arrays.toString(request);
    }

    @Override
    public RequestType getRequestType() {
        return requestType;
    }
}