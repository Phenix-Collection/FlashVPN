package winterfell.flash.vpn.reward.network.datamodels;

import com.google.gson.annotations.SerializedName;

import winterfell.flash.vpn.utils.MLogs;

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

    public void dump() {
        MLogs.i(mGeo + " " + mCity + " Id " + getId());
    }

    public Region dup() {
        return new Region(mGeo, mCity);
    }
}
