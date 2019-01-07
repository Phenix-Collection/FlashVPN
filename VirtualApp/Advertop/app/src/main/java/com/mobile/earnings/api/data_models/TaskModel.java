package com.mobile.earnings.api.data_models;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;



public class TaskModel implements Serializable{

	@SerializedName("id")
	public int      id;
	@SerializedName("application_id")
	public int      applicationId;
	@SerializedName("user_id")
	public int      userId;
	@SerializedName("status")
	public int      status;
	@SerializedName("app")
	public AppModel app;

	public TaskModel(AppModel app){
		this.app = app;
	}
}