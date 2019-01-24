package com.polestar.task.network.responses;

import com.google.gson.annotations.SerializedName;
import com.polestar.task.network.datamodels.User;
import com.polestar.task.network.datamodels.UserTask;

public class UserTaskResponse {
    @SerializedName("user")
    public User mUser;
    @SerializedName("user_task")
    public UserTask mUserTask;
}
