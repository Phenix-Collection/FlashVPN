package com.polestar.task;

import com.polestar.task.database.datamodels.AdTask;

/**
 * Created by guojia on 2019/3/10.
 */

public interface IAdTaskStateObserver {
    void onAdTaskClicked(AdTask task);
    void onAdTaskInstalled(AdTask task);
}
