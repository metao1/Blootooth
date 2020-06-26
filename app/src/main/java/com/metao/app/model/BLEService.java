package com.metao.app.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

public class BLEService implements Parcelable {

    private List<ServiceCharacteristic> serviceCharacteristic = new ArrayList<>();
    private String macAddress;
    private String name;
    private String uuid;

    private BLEService() {
        //empty
    }

    public BLEService(Parcel in) {
        in.readTypedList(serviceCharacteristic, ServiceCharacteristic.CREATOR);
        this.macAddress = in.readString();
        this.name = in.readString();
        this.uuid = in.readString();
    }

    public static BLEService Builder() {
        return new BLEService();
    }

    public BLEService addName(String name) {
        this.name = name;
        return this;
    }

    public BLEService addUuid(String uuid) {
        this.uuid = uuid;
        return this;
    }

    public BLEService addMacAddress(String macAddress) {
        this.macAddress = macAddress;
        return this;
    }

    public BLEService addServiceCharacteristic(ServiceCharacteristic sc) {
        this.serviceCharacteristic.add(sc);
        return this;
    }

    public List<ServiceCharacteristic> getServiceCharacteristic() {
        return serviceCharacteristic;
    }

    public String getName() {
        return name;
    }

    public String getUuid() {
        return uuid;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public static final Parcelable.Creator<BLEService> CREATOR = new Parcelable.Creator<BLEService>() {
        public BLEService createFromParcel(Parcel in) {
            return new BLEService(in);
        }

        public BLEService[] newArray(int size) {
            return new BLEService[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeTypedList(this.serviceCharacteristic);
        dest.writeString(this.macAddress);
        dest.writeString(this.name);
        dest.writeString(this.uuid);
    }
}
