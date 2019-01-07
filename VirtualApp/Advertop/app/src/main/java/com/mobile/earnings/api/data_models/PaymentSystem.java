package com.mobile.earnings.api.data_models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;




public class PaymentSystem implements Parcelable{

	@SerializedName("id")
	public int    id;
	@SerializedName("name")
	public String name;
	@SerializedName("active")
	public int    active;
	public int    icon;

	protected PaymentSystem(Parcel in){
		id = in.readInt();
		name = in.readString();
		active = in.readInt();
		icon = in.readInt();
	}

	public static final Creator<PaymentSystem> CREATOR = new Creator<PaymentSystem>(){
		@Override
		public PaymentSystem createFromParcel(Parcel in){
			return new PaymentSystem(in);
		}

		@Override
		public PaymentSystem[] newArray(int size){
			return new PaymentSystem[size];
		}
	};

	@Override
	public int describeContents(){
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags){
		dest.writeInt(id);
		dest.writeString(name);
		dest.writeInt(active);
		dest.writeInt(icon);
	}
}