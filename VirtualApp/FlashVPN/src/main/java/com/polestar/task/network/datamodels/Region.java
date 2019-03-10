package com.polestar.task.network.datamodels;

import com.google.gson.annotations.SerializedName;

public class Region {
    @SerializedName("geo")
    public String mGeo;

    @SerializedName("city")
    public String mCity;


    public int getId() {
        return mGeo.hashCode() + mCity.hashCode();
    }

    public Region(String geo, String city) {
        mGeo = geo;
        mCity = city;
    }

    public Region dup() {
        return new Region(mGeo, mCity);
    }
}
