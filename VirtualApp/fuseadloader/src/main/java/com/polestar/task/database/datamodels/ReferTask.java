package com.polestar.task.database.datamodels;

import com.polestar.task.network.datamodels.Task;

import org.json.JSONObject;

public class ReferTask extends Task {
    public ReferTask(Task task) {
        super(task);
    }

    @Override
    public boolean isValid() {
        return super.isValid();
    }

    @Override
    protected boolean parseTaskDetail(JSONObject detail) {
        return true;
    }
}
