package com.polestar.task.network;

import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Calendar;
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

//    public static boolean tooClose(Date one, Date two, long milliseconds) {
//        Calendar thisop = Calendar.getInstance();
//        thisop.setTime(two);
//        Calendar lastop = Calendar.getInstance();
//        lastop.setTime(one);
//        long timeDiff = Math.abs(thisop.getTimeInMillis() - lastop.getTimeInMillis());
//        if (timeDiff <= milliseconds) {
//            return true;
//        } else {
//            return false;
//        }
//    }

    public static boolean tooCloseWithNow(Date one, long milliseconds) {
        if (one == null) {
            return false;
        }
        Calendar thisop = Calendar.getInstance();
        Calendar lastop = Calendar.getInstance();
        lastop.setTime(one);
        long timeDiff = Math.abs(thisop.getTimeInMillis() - lastop.getTimeInMillis());
        if (timeDiff <= milliseconds) {
            return true;
        } else {
            return false;
        }
    }
}
