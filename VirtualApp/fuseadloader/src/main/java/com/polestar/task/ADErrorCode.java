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

    private static final HashMap<Integer, String> sErrMapping = createErrMapping();

    private static HashMap<Integer, String> createErrMapping() {
        HashMap<Integer, String> ret = new HashMap<>();
        ret.put(ERR_SERVER_DOWN_CODE, "Server Down");

        return ret;
    }

    public static ADErrorCode createServerDown() {
        return new ADErrorCode(ERR_SERVER_DOWN_CODE, sErrMapping.get(ERR_SERVER_DOWN_CODE));
    }

    public static ADErrorCode createFromAdErrMsg(String msg) {
        return new ADErrorCode(ErrorCodeInterceptor.getErrCode(msg),
                ErrorCodeInterceptor.getErrMsg(msg));
    }

    public ADErrorCode(int code, String msg) {
        errCode = code;
        errMsg = msg;
    }

    public int getErrCode() {
        return errCode;
    }
    public String getErrMsg() {
        return errMsg;
    }
}
