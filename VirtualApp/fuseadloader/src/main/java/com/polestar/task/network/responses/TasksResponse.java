package com.polestar.task.network.responses;

import com.google.gson.annotations.SerializedName;
import com.polestar.task.network.datamodels.Task;

import java.util.ArrayList;

public class TasksResponse {
    @SerializedName("tasks")
    public ArrayList<Task> mTasks;
}
