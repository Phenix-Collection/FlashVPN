package com.polestar.task;

import com.polestar.task.network.datamodels.Task;

import java.util.ArrayList;

/**
 * Created by guojia on 2019/1/19.
 */

public interface ITaskStatusListener {

    /**
     *
     * @param taskId
     * @param payment
     * @param balance updated from server
     */
    void onTaskSuccess(long taskId, float payment, float balance);
    void onTaskFail(long taskId, ADErrorCode code);

    void onGetAllAvailableTasks(ArrayList<Task> tasks);
    void onGeneralError(ADErrorCode code);
}
