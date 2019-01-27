package com.polestar.task.database.datamodels;

import android.text.TextUtils;

import com.polestar.task.network.datamodels.Task;

import org.json.JSONObject;

/**
 * Created by guojia on 2019/1/17.
 */

public class AdTask extends Task {
    public static final String SLOT_ALL_AD_PLACEMENT = "slot_*";
    public static final String SLOT_DIVIDER = ";";
    public static final String SLOT_ALL_POP_PLACEMENT = "pop_*";
    public static final String SLOT_ALL_TASK_PLACEMENT = "task_*";

    public String adid;
    /**
     * the description of the offer, e.g. most popular game in xxx
     */
    public String adDesc;
//    /**
//     * the description of the conversion flow, e.g. install and upgrade to level 3
//     * the flow description should be in the base task model
//     */
//    public String flowDesc;
    public String impUrl;
    public String clickUrl;  //MUST
//    public String title; //MUST move to base task
    public String flow; //cpi
    public String imageUrl;
    public String iconUrl;  //MUST
    public String videoUrl;
    public String ctaText; //MUST
    public String pkg;  //MUST
    /**
     * include, exclude slots, divided by ";"
     * include default "slot_*;task_*"
     */
    public String includeSlots;
    public String excludeSlots;
    /**
     * default 0; only backfill for ad placements; larger is higher priority
     */
    public int priority;

    public AdTask(Task task) {
        super(task);
    }

    @Override
    public boolean isValid() {
        return super.isValid()
                && !TextUtils.isEmpty(adid)
                && !TextUtils.isEmpty(iconUrl)
                && !TextUtils.isEmpty(mTitle)
                && !TextUtils.isEmpty(ctaText)
                && !TextUtils.isEmpty(pkg)
                && !TextUtils.isEmpty(clickUrl);
    }

    @Override
    protected boolean parseTaskDetail(JSONObject detail) {
        adid = detail.optString("adid");
        adDesc = detail.optString("adDesc");
//        flowDesc = detail.optString("flowDesc");
        flow = detail.optString("flow");
        impUrl = detail.optString("impUrl");
        clickUrl = detail.optString("clkUrl");
//        title = detail.optString("title");
        imageUrl = detail.optString("image");
        iconUrl = detail.optString("icon");
        videoUrl = detail.optString("video");
        ctaText = detail.optString("cta");
        pkg = detail.optString("pkg");
        includeSlots = detail.optString("include",
                SLOT_ALL_AD_PLACEMENT + SLOT_DIVIDER + SLOT_ALL_TASK_PLACEMENT);
        excludeSlots = detail.optString("exclude");
        priority = detail.optInt("priority", 0);
        return true;
    }
}
