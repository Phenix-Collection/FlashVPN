package com.mobile.earnings.utils;

import android.support.annotation.NonNull;

import com.mobile.earnings.api.data_models.AppModel;
import com.mobile.earnings.api.responses.MainTasksResponse;
import com.mobile.earnings.api.data_models.TaskModel;

import java.util.ArrayList;




public class AppModelExtractor{

	public static ArrayList<AppModel> extractAppModels(@NonNull MainTasksResponse response){
		ArrayList<AppModel> tempList = new ArrayList<>();
		for(TaskModel task : response.items){
			tempList.add(task.app);
		}
		return tempList;
	}
}
