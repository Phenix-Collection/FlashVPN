package com.polestar.task.network;

import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class MiscUtils {
    public static final String DATA_DATE_FORMAT_TIME = "yyyy-MM-dd HH:mm:ss";
    private static SimpleDateFormat sFormat = new SimpleDateFormat(DATA_DATE_FORMAT_TIME);
    public static long getTimeInMilliSecondsFromUTC(String timeInUTC) {
        if (timeInUTC == null || timeInUTC.isEmpty()) {
            return 0;
        }
        sFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        try {
            Date date = sFormat.parse(timeInUTC);
            Log.i(Configuration.HTTP_TAG, "timeInUTC Str:" + timeInUTC
                    + " inMilli:" + date.getTime() + " inLocalStr:" + date.toString());
            return date.getTime();
        } catch (java.text.ParseException e) {
            e.printStackTrace();
            return 0;
        }
    }
}
