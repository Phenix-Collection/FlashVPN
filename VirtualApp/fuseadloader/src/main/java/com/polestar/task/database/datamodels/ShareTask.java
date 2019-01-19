package com.polestar.task.database.datamodels;

import org.json.JSONObject;

/**
 * Created by guojia on 2019/1/17.
 */

public class ShareTask extends Task {
    public String shareUrl;
    public String desc;

    public ShareTask(JSONObject jsonObject) {
        super(jsonObject);
    }

    @Override
    protected void parseTaskDetail(JSONObject detail) {
        shareUrl = detail.optString("url","");
        desc = detail.optString("desc", "");
    }

    @Override
    public boolean isValid() {
        return super.isValid() &&
                (shareUrl.startsWith("market://") || shareUrl.startsWith("http:") || shareUrl.startsWith("https"));
    }
}
