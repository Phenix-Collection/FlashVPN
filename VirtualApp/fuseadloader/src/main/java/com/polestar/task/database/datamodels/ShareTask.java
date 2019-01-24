package com.polestar.task.database.datamodels;

import com.polestar.task.network.datamodels.Task;

import org.json.JSONObject;

/**
 * Created by guojia on 2019/1/17.
 */

public class ShareTask extends Task {
    public String shareUrl;
    public String desc;

    public ShareTask(Task task) {
        super(task);
    }

    @Override
    protected boolean parseTaskDetail(JSONObject detail) {
        shareUrl = detail.optString("url","");
        desc = detail.optString("desc", "");
        return true;
    }

    @Override
    public boolean isValid() {
        return super.isValid() &&
                (shareUrl.startsWith("market://") || shareUrl.startsWith("http:") || shareUrl.startsWith("https"));
    }
}
