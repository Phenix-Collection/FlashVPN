package com.polestar.task.database.datamodels;

import android.text.TextUtils;

import com.polestar.task.network.datamodels.Task;

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
    protected boolean parseTaskDetail(JSONObject detail) {
        String s = detail.optString("award");
        if (!TextUtils.isEmpty(s)){
            String[] arr = s.split(";");
            if (arr!= null && arr.length > 0) {
                award = new int[arr.length];
                int i = 0;
                for(String a: arr) {
                    award[i++] = Integer.valueOf(a);
                }
                return true;
            }
        }
        return false;
    }

    public CheckInTask(Task task) {
        super(task);
    }

    /*public int getCheckInAward() {
        if (award!= null && award.length > 0) {
            //TODO should return the real award according to checkin days
            return award[0];
        }
        return payout;
    }*/
}
