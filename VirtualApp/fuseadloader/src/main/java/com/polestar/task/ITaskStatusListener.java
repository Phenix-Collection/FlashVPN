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
     * @param payment 始终是当前这个单次任务给的钱；不会包括之前的pending任务帐的回款
     * @param balance 用户当前的余额
     */
    void onTaskSuccess(long taskId, float payment, float balance);
    void onTaskFail(long taskId, ADErrorCode code);

    void onGetAllAvailableTasks(ArrayList<Task> tasks);
    void onGeneralError(ADErrorCode code);
}
