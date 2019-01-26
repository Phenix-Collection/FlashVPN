package com.polestar.superclone.reward;

import android.content.Context;
import android.content.SharedPreferences;

import com.polestar.superclone.MApp;
import com.polestar.superclone.constant.AppConstants;

/**
 * Created by guojia on 2019/1/26.
 */

class TaskPreference {

    private static String PREFERENCE_NAME = "reward_task";

    private static SharedPreferences getSharedPreference(){
       return MApp.getApp().getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
    }

    private static void updateLastUpdateTime() {
        getSharedPreference().edit().putLong("last_udpate_time", System.currentTimeMillis()).commit();
    }

    private static long getLastUpdateTime() {
        return  getSharedPreference().getLong("last_update_time", 0);
    }

    private static void updateTaskFinishTime() {

    }
}
