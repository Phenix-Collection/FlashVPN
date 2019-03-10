package com.polestar.task.database.datamodels;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.os.Build;
import android.text.TextUtils;

import com.polestar.ad.AdLog;
import com.polestar.ad.AdUtils;
import com.polestar.task.network.datamodels.Task;

import org.json.JSONObject;

import java.util.Locale;

/**
 * Created by guojia on 2019/1/17.
 */

public class AdTask extends Task {
    private static final String SLOT_ALL_AD_PLACEMENT = "slot_*";
    private static final String SLOT_DIVIDER = ";";
    private static final String SLOT_ALL_POP_PLACEMENT = "pop_*";
    private static final String SLOT_ALL_TASK_PLACEMENT = "task_*";
    public static final String TASK_SLOT_PREFIX = "task_";
    public static final String AD_TASK_PREF = "ad_task_pref";

    private static final String FLOW_CPC = "cpc";
    private static final String FLOW_CPI = "cpi";

    public String adid;
    /**
     * the description of the offer, e.g. most popular game in xxx
     */
    public String adDesc;
//    /**
//     * the description of the conversion flow, e.g. install and upgrade to level 3
//     * the flow description should be in the base task model
//     */
//    public String flowDesc;
    public String impUrl;
    public String clickUrl;  //MUST
//    public String title; //MUST move to base task
    public String flow; //cpi/cpc //MUST
    public String imageUrl;
    public String iconUrl;  //MUST
    public String videoUrl;
    public String ctaText; //MUST
    public String pkg;  //MUST
    public String country;
    /**
     * include, exclude slots, divided by ";"
     * include default "slot_*;task_*"
     */
    public String includeSlots;
    public String excludeSlots;
    /**
     * default 0; only backfill for ad placements; larger is higher priority
     */
    public int priority;

    public AdTask() {
        super();
    }
    public AdTask(Task task) {
        super(task);
    }

    @Override
    public boolean isValid() {
        return super.isValid()
                && !TextUtils.isEmpty(adid)
                && !TextUtils.isEmpty(iconUrl)
                && !TextUtils.isEmpty(mTitle)
                && !TextUtils.isEmpty(ctaText)
                && !TextUtils.isEmpty(pkg)
                && !TextUtils.isEmpty(clickUrl);
    }

    @Override
    protected boolean parseTaskDetail(JSONObject detail) {
        adid = detail.optString("adid");
        adDesc = detail.optString("adDesc");
//        flowDesc = detail.optString("flowDesc");
        flow = detail.optString("flow");
        if (!FLOW_CPC.equals(flow) && !FLOW_CPI.equals(flow)) {
            return false;
        }
        impUrl = detail.optString("impUrl");
        clickUrl = detail.optString("clkUrl");
//        title = detail.optString("title");
        imageUrl = detail.optString("image");
        iconUrl = detail.optString("icon");
        videoUrl = detail.optString("video");
        ctaText = detail.optString("cta");
        country = detail.optString("geo", "*");
        pkg = detail.optString("pkg");
        includeSlots = detail.optString("include",
                SLOT_ALL_AD_PLACEMENT + SLOT_DIVIDER + SLOT_ALL_TASK_PLACEMENT);
        excludeSlots = detail.optString("exclude","");
        priority = detail.optInt("priority", 0);
        return true;
    }

    private static boolean isValidSlotName(String slot) {
        return slot!= null && (slot.startsWith("pop") || slot.startsWith("slot") || slot.startsWith("task"));
    }

    public boolean canFillToSlot(Context context, String slot) {
        if (!isValidSlotName(slot)) {
            return false;
        }
        if (!isValid()) {
            return false;
        }
        if (!country.equals("*")) {
            android.content.res.Configuration configuration = context.getResources().getConfiguration();
            Locale locale;
            if (Build.VERSION.SDK_INT >= 24) {
                locale = configuration.getLocales().get(0);
            } else {
                locale = configuration.locale;
            }
            String geo = locale.getCountry();
            if (!TextUtils.isEmpty(geo) && !country.toLowerCase().contains(geo.toLowerCase())){
                return false;
            }
        }
        if (slot.startsWith("slot")) {
            if (includeSlots.contains(SLOT_ALL_AD_PLACEMENT)
                    || includeSlots.contains(slot)) {
                if (excludeSlots.contains(SLOT_ALL_AD_PLACEMENT)
                        || excludeSlots.contains(slot)) {
                    return false;
                }
            } else {
                return false;
            }
        }
        if (slot.startsWith("pop")) {
            if (includeSlots.contains(SLOT_ALL_POP_PLACEMENT)
                    || includeSlots.contains(slot)) {
                if (excludeSlots.contains(SLOT_ALL_POP_PLACEMENT)
                        || excludeSlots.contains(slot)) {
                    return false;
                }
            } else {
                return false;
            }
        }
        if (slot.startsWith("task")) {
            if (includeSlots.contains(SLOT_ALL_TASK_PLACEMENT)
                    || includeSlots.contains(slot)) {
                if (excludeSlots.contains(SLOT_ALL_TASK_PLACEMENT)
                        || excludeSlots.contains(slot)) {
                    return false;
                }
            } else {
                return false;
            }
        }
        if (isIgnored(context, slot)) {
            return false;
        }
        boolean isInstalled = false;
        try{
            ApplicationInfo ai = context.getPackageManager().getApplicationInfo(pkg, 0);
            isInstalled = (ai!= null);
        }catch (Exception ex) {

        }
        return !isInstalled;
    }

    public void ignoreFor(Context context, String slot, long interval) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(AD_TASK_PREF, Context.MODE_PRIVATE);
        sharedPreferences.edit().putLong("ignore_" + slot + "_" + mId, System.currentTimeMillis() + interval).commit();
    }

    public boolean isIgnored(Context context, String slot) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(AD_TASK_PREF, Context.MODE_PRIVATE);
        return System.currentTimeMillis() - sharedPreferences.getLong("ignore_" + slot + "_" + mId, 0) < 0;
    }

    public void updateShowTime(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(AD_TASK_PREF, Context.MODE_PRIVATE);
        sharedPreferences.edit().putLong("show_" + mId, System.currentTimeMillis()).commit();
    }

    public long getShowTime(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(AD_TASK_PREF, Context.MODE_PRIVATE);
        return sharedPreferences.getLong("show_"  + mId, 0);
    }

    public boolean isCpc() {
        return FLOW_CPC.equals(flow);
    }

    public boolean isCpi() {
        return FLOW_CPI.equals(flow);
    }
}
