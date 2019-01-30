package com.polestar.task;

import com.polestar.task.network.ErrorCodeInterceptor;

import java.util.HashMap;

/**
 * Created by guojia on 2019/1/19.
 */

public class ADErrorCode {
    private int errCode;
    private String errMsg;

    public static final int ERR_SERVER_DOWN_CODE = 1000;
    public static final int USER_NOTEXIST = 1;
    public static final int TASK_NOTEXIST = 2;
    public static final int PRODUCT_NOTEXIST = 3;
    public static final int TOTAL_LIMITTED = 4;
    public static final int MISS_PARAMETER = 5;
    public static final int NOT_ENOUGH_MONEY = 6;
    public static final int INVALID_REFERRAL_CODE = 7;
    public static final int ALREADY_REFERRED = 8;
    public static final int INVALID_REQUEST = 9;
    public static final int DAY_LIMITTED = 10;
    public static final int OP_TOO_FREQUENT = 11;
    public static final int MAX_SERVER_ERR_CODE = ERR_SERVER_DOWN_CODE;
    public static final int FLOW_CONTROL_ERR_CODE = 1001;

    private static final HashMap<Integer, String> sErrMapping = createErrMapping();

    private static HashMap<Integer, String> createErrMapping() {
        HashMap<Integer, String> ret = new HashMap<>();
        ret.put(ERR_SERVER_DOWN_CODE, "Server Down");
        ret.put(FLOW_CONTROL_ERR_CODE, "Too Many Requests");

        return ret;
    }

    public static ADErrorCode createServerDown() {
        return new ADErrorCode(ERR_SERVER_DOWN_CODE, sErrMapping.get(ERR_SERVER_DOWN_CODE));
    }
    public static ADErrorCode createTooManyRequests() {
        return new ADErrorCode(FLOW_CONTROL_ERR_CODE, sErrMapping.get(FLOW_CONTROL_ERR_CODE));
    }

    public static ADErrorCode createFromAdErrMsg(String msg) {
        return new ADErrorCode(ErrorCodeInterceptor.getErrCode(msg),
                ErrorCodeInterceptor.getErrMsg(msg));
    }

    public ADErrorCode(int code, String msg) {
        errCode = code;
        errMsg = msg;
    }

    public String toString() {
        return "ADErr errCode:" + errCode + " errMsg:" + errMsg;
    }

    public int getErrCode() {
        return errCode;
    }
    public String getErrMsg() {
        return errMsg;
    }
}
