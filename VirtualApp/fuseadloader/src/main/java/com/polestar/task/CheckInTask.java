package com.polestar.task;

import android.text.TextUtils;

import org.json.JSONObject;

/**
 * Created by guojia on 2019/1/17.
 */

public class CheckInTask extends Task {
    public int[] award;

    @Override
    public boolean isValid() {
        return super.isValid();
    }

    @Override
    protected void parseTaskDetail(JSONObject detail) {
        String s = detail.optString("award");
        if (!TextUtils.isEmpty(s)){
            String[] arr = s.split(";");
            if (arr!= null && arr.length > 0) {
                award = new int[arr.length];
                int i = 0;
                for(String a: arr) {
                    award[i++] = Integer.valueOf(a);
                }
            }
        }
    }

    public CheckInTask(JSONObject jsonObject) {
        super(jsonObject);
    }

    public int getCheckInAward() {
        if (award!= null && award.length > 0) {
            //TODO should return the real award according to checkin days
            return award[0];
        }
        return payout;
    }
}
