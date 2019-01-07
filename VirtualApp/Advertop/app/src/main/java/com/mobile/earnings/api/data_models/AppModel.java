package com.mobile.earnings.api.data_models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import static com.mobile.earnings.main.adapters.BaseTasksAdapter.VIEW_TYPE_TASK;


public class AppModel implements Parcelable{

	@SerializedName("id")
	public int          id;
	@SerializedName("package_name")
	public String       packageName;
	@SerializedName("name")
	public String       name;
	@SerializedName("image")
	public String       image;
	@SerializedName("description")
	public String       description;
	@SerializedName("tracking_link")
	public String       trackingLink;
	@SerializedName("price")
	public float        priceForDay;
	@SerializedName("days")
	public int          days;
	@SerializedName("days_left")
	public int          daysLeft;
	@SerializedName("keywords")
	public List<String> keywords;
	@SerializedName("limit")
	public int          limit;
	@SerializedName("amount")
	public float        amount;
	@SerializedName("currency")
	public String       currency;
	@SerializedName("status")
	public int          status;
	@SerializedName("availability")
	public Boolean      isAvailable;
	@SerializedName("read")
	public boolean      isTaskWatched;
	@SerializedName("time_delay")
	public int         timeDelayInSeconds;
	@SerializedName("review_available")
	public boolean      isReviewAvailable;

	public int    resourceImage;
	public String defaultTaskReward;
	public int    viewType            = VIEW_TYPE_TASK;
	public String defaultTaskKeywords = "";

	public AppModel(int id, int viewType, String name, String description, String defaultTaskReward, String currency, int resourceImage){
		this.id = id;
		this.name = name;
		this.viewType = viewType;
		this.description = description;
		this.defaultTaskReward = defaultTaskReward;
		this.resourceImage = resourceImage;
		this.currency = currency;
	}


	protected AppModel(Parcel in){
		id = in.readInt();
		packageName = in.readString();
		name = in.readString();
		image = in.readString();
		description = in.readString();
		trackingLink = in.readString();
		priceForDay = in.readFloat();
		days = in.readInt();
		daysLeft = in.readInt();
		keywords = in.createStringArrayList();
		limit = in.readInt();
		amount = in.readFloat();
		currency = in.readString();
		status = in.readInt();
		isAvailable = (Boolean) in.readValue(Boolean.class.getClassLoader());
		isTaskWatched = in.readByte() != 0;
		resourceImage = in.readInt();
		defaultTaskReward = in.readString();
		timeDelayInSeconds = in.readInt();
		isReviewAvailable = in.readByte() != 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags){
		dest.writeInt(id);
		dest.writeString(packageName);
		dest.writeString(name);
		dest.writeString(image);
		dest.writeString(description);
		dest.writeString(trackingLink);
		dest.writeFloat(priceForDay);
		dest.writeInt(days);
		dest.writeInt(daysLeft);
		dest.writeStringList(keywords);
		dest.writeInt(limit);
		dest.writeFloat(amount);
		dest.writeString(currency);
		dest.writeInt(status);
		dest.writeValue(isAvailable);
		dest.writeByte((byte) (isTaskWatched ? 1 : 0));
		dest.writeInt(resourceImage);
		dest.writeString(defaultTaskReward);
		dest.writeInt(timeDelayInSeconds);
		dest.writeByte((byte) (isReviewAvailable ? 1 : 0));
	}

	@Override
	public int describeContents(){
		return 0;
	}

	public static final Creator<AppModel> CREATOR = new Creator<AppModel>(){
		@Override
		public AppModel createFromParcel(Parcel in){
			return new AppModel(in);
		}

		@Override
		public AppModel[] newArray(int size){
			return new AppModel[size];
		}
	};
}
