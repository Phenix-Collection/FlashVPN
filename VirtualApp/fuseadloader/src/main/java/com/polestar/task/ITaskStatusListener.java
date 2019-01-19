package com.polestar.task;

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
    void onTaskSuccess(String taskId, int payment, int balance);
    void onTaskFail(String taskId, ErrorCode code);
}
