package com.polestar.superclone.reward;

import android.content.Context;

import com.polestar.superclone.MApp;
import com.polestar.superclone.R;
import com.polestar.task.ADErrorCode;

/**
 * Created by guojia on 2019/1/29.
 */

final public class RewardErrorCode {
    private static final int REWARD_ERROR_CODE_BASE = ADErrorCode.MAX_SERVER_ERR_CODE + 1000;
    public static final int TASK_OK = REWARD_ERROR_CODE_BASE;
    public static final int TASK_EXCEED_DAY_LIMIT = REWARD_ERROR_CODE_BASE + 1;
    public static final int TASK_AD_NO_FILL = REWARD_ERROR_CODE_BASE + 2;
    public static final int TASK_UNEXPECTED_ERROR = REWARD_ERROR_CODE_BASE + 3;
    public static final int TASK_AD_LOADING = REWARD_ERROR_CODE_BASE + 4;

    public static final String getToastMessage(int code, Object ... args) {
        return getToastMessage(MApp.getApp(), code, args);
    }

    public static final String getToastMessage(Context context, int code, Object ... args) {
        switch (code) {
            case TASK_AD_NO_FILL:
                return context.getString(R.string.error_ad_no_fill);
            case TASK_EXCEED_DAY_LIMIT:
            case ADErrorCode.DAY_LIMITTED:
            case ADErrorCode.TOTAL_LIMITTED:
                return context.getString(R.string.error_day_limit);
            case TASK_OK:
                float payment = (float) args[0];
                return context.getString(R.string.task_ok, payment);
            default:
                break;
        }
        return context.getString(R.string.error_unexpected);
    }
}
