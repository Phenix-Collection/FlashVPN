package com.polestar.task.network;

public class Configuration {
    public static String URL_PREFIX = "http://13.112.221.35";

    public static long API_COMMON_INTERVAL = 5 * 1000; //5 seconds 单次操作的时间间隔

    public static long API_RANGE_INTERVAL = 60 * 1000; //一段时间内不能超过多少次的时间区域
    public static int API_RANGE_MAX_COUNT = 6; //一段时间内不能超过多少次

    public static int APP_VERSION_CODE = 0;


    public static final int STATUS_OPEN = 1;
    public static final int STATUS_CLOSED = 2;

    public static final String HTTP_TAG = "httpTag";

    public static final String ADERR_PREFIX = "ADErr";
    public static final String ADERR_SEPARATOR = ":";
}
