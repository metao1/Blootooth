package com.metao.app.model;

import android.os.Parcel;
import android.os.Parcelable;

public class ServiceCharacteristic implements Parcelable {

    public static final Parcelable.Creator<ServiceCharacteristic> CREATOR
            = new Parcelable.Creator<ServiceCharacteristic>() {
        public ServiceCharacteristic createFromParcel(Parcel in) {
            return new ServiceCharacteristic(in);
        }

        public ServiceCharacteristic[] newArray(int size) {
            return new ServiceCharacteristic[size];
        }
    };
    private String uuid;
    private String name;

    private ServiceCharacteristic() {
        //empty
    }

    public ServiceCharacteristic(Parcel in) {
        this.uuid = in.readString();
        this.name = in.readString();
    }

    public static ServiceCharacteristic Builder() {
        return new ServiceCharacteristic();
    }

    public ServiceCharacteristic addName(String name) {
        this.name = name;
        return this;
    }

    public ServiceCharacteristic addCharacteristicId(String uuid) {
        this.uuid = uuid;
        return this;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.uuid);
        dest.writeString(this.name);
    }
}
