package com.polestar.task.database.datamodels;

import com.polestar.task.network.datamodels.Task;

import org.json.JSONObject;

public class RandomAwardTask extends Task{
    public RandomAwardTask(Task task) {
        super(task);
    }

    @Override
    protected boolean parseTaskDetail(JSONObject detail) {
        return true;
    }

    @Override
    public boolean isValid() {
        return super.isValid();
    }
}
