package com.polestar.booster.util;


import android.content.Context;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class TimeUtil {
    public static final long SECOND = 1000L;
    public static final long MINUTE = 60000L;
    public static final long HOUR = 3600000L;
    public static final long DAY = 86400000L;
    public static final long WEEK = 604800000L;
    static final SimpleDateFormat sDateFormatUTC;
    static final SimpleDateFormat sDateFormat;
    static final SimpleDateFormat sDateHourFormatUTC;
    static final SimpleDateFormat sDateHourMinuteFormatUTC;

    public TimeUtil() {
    }

    public static String dateNowUTC() {
        return sDateFormatUTC.format(new Date());
    }

    public static String dateNow() {
        return sDateFormat.format(new Date());
    }

    public static String dateHourNowUTC() {
        return sDateHourFormatUTC.format(new Date());
    }

    public static String dateHourMinuteNowUTC() {
        return sDateHourMinuteFormatUTC.format(new Date());
    }

    public static long getDayStart(long timeInMillis) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(timeInMillis);
        cal.set(11, 0);
        cal.set(12, 0);
        cal.set(13, 0);
        return cal.getTimeInMillis();
    }

    public static long getTodayStart() {
        return getDayStart(System.currentTimeMillis());
    }

    public static boolean isSystem12HourTimeFormat(Context ctx) {
        try {
            DateFormat e = android.text.format.DateFormat.getTimeFormat(ctx);
            String pattern = ((SimpleDateFormat)e).toLocalizedPattern();
            return pattern == null?false:("HH:mm".equals(pattern)?false:pattern.contains("h:mm"));
        } catch (Exception var3) {
            return false;
        }
    }

    static {
        sDateFormatUTC = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        sDateFormatUTC.setTimeZone(TimeZone.getTimeZone("UTC"));
        sDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        sDateHourFormatUTC = new SimpleDateFormat("yyyy-MM-dd HH", Locale.US);
        sDateHourFormatUTC.setTimeZone(TimeZone.getTimeZone("UTC"));
        sDateHourMinuteFormatUTC = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US);
        sDateHourMinuteFormatUTC.setTimeZone(TimeZone.getTimeZone("UTC"));
    }
}
