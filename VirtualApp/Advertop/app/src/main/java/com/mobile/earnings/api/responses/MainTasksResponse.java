package com.mobile.earnings.api.responses;

import com.mobile.earnings.api.data_models.TaskModel;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;

public class MainTasksResponse implements Serializable{

	@SerializedName("items")
	public ArrayList<TaskModel> items;

}
