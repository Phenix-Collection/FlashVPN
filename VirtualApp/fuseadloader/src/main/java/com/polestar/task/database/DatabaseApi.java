package com.polestar.task.database;

import com.polestar.task.database.datamodels.AdTask;

import java.util.ArrayList;

public interface DatabaseApi {

    public ArrayList<AdTask> getActiveAdTasks();
}
