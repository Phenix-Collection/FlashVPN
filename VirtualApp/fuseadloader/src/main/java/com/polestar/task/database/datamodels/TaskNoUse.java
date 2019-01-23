package com.polestar.task.database.datamodels;

import org.json.JSONObject;

/**
 * Created by guojia on 2019/1/17.
 */

public abstract class TaskNoUse {
    public static final String TASK_TYPE_SHARE = "share";
    public static final String TASK_TYPE_CHECKIN = "checkin";
    public static final String TASK_TYPE_REWARD_VIDEO = "rv";
    public static final String TASK_TYPE_AD = "ad";

    public long id;
    public String type;
    /**
     * payout to user; default 0
     */
    public int payout;
    /**
     * end time of task; default Long.MAX --> forever effective
     */
    public long endTime;
    /**
     * limit per day; default 1
     */
    public int dayLimit;
    /**
     * total limit per user; default 1
     */
    public int totalLimit;
    /**
     * TBD
     */
    public String status;
    public String rank;


    public TaskNoUse(JSONObject jsonObject) {
        id = jsonObject.optLong("id");
        type = jsonObject.optString("type");
        payout = jsonObject.optInt("payout", 0);
        endTime = jsonObject.optLong("endTime", Long.MAX_VALUE);
        dayLimit = jsonObject.optInt("dayLimit", 1);
        totalLimit = jsonObject.optInt("totalLimit", 1);
        status = jsonObject.optString("status");
        rank = jsonObject.optString("rank");
        JSONObject detail = jsonObject.optJSONObject("detail");
        parseTaskDetail(detail);
    }

    abstract protected void parseTaskDetail(JSONObject detail);

    /**
     * Whether it's a valid task
     * @return
     */
    public boolean isValid(){
        return dayLimit > 0 && totalLimit > 0 && dayLimit < totalLimit
                && payout >= 0;
    }

    /**
     * Whether the task is effective and can be rewarded
     * @return
     */
    public boolean isEffective() {
        return isValid() && System.currentTimeMillis() < endTime;
    }
}
