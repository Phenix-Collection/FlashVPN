package com.polestar.task.database.datamodels;

import android.text.TextUtils;

import com.polestar.task.network.datamodels.Task;

import org.json.JSONObject;

/**
 * Created by guojia on 2019/1/17.
 */

public class RewardVideoTask extends Task {
    /**
     * ad slot; can get ad config by ad lot
     */
    public String adSlot;
    public String desc;
//    public AdConfig adConfig;

    @Override
    public boolean isValid() {
        return super.isValid() && !TextUtils.isEmpty(adSlot);
    }

    @Override
    protected boolean parseTaskDetail(JSONObject detail) {
        adSlot = detail.optString("adSlot");
        desc = detail.optString("desc");
        return true;
    }

    public RewardVideoTask(Task task) {
        super(task);
    }
}
