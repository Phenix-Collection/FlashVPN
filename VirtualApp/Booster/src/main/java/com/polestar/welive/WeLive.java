package com.polestar.welive;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.annotation.TargetApi;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;

import com.polestar.booster.BoosterLog;
import com.polestar.booster.BoosterSdk;

/**
 * Created by guojia on 2018/12/12.
 */

public class WeLive {
    public static String ACCOUNT_TYPE = "clone.daemon";
    public final static String ACCOUNT_NAME = "Clone Messaging Daemon";
    public final static long SYNC_PERIOD_SEC = 28800;
    public static void startSync(Context context) {
        BoosterLog.log("JJJJ startSync");
        Account v1 = new Account(ACCOUNT_NAME, ACCOUNT_TYPE);
        AccountManager accountManager = (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);
        if(accountManager != null) {
            try {
                if(!hasAccount(accountManager, ACCOUNT_NAME)) {
                    accountManager.addAccountExplicitly(v1, "", null);
                }
                String providerName = context.getPackageName() + ".sync.provider";
                ContentResolver.setIsSyncable(v1, providerName, 1);
                ContentResolver.setSyncAutomatically(v1, providerName, true);
                ContentResolver.addPeriodicSync(v1, providerName, new Bundle(), SYNC_PERIOD_SEC);
            }
            catch(Exception v0_1) {
                v0_1.printStackTrace();
            }
        }
    }

    @TargetApi(value=21)
    private static void scheduleJob(int jobId, Context arg9) {
        BoosterLog.log(("JJJJ scheduleJob " + jobId));
        long period = SYNC_PERIOD_SEC *1000;
        if(Build.VERSION.SDK_INT >= 21) {
            Object v0 = arg9.getSystemService(Context.JOB_SCHEDULER_SERVICE);
            if(v0 != null) {
                JobInfo.Builder builder = new JobInfo.Builder(jobId, new ComponentName(arg9.getPackageName(), WeLiveJobService.class.getName()));
                switch(jobId) {
                    case 1: {
                        builder.setPeriodic(period);
                        break;
                    }
                    case 2: {
                        builder.setMinimumLatency(period);
                        builder.setRequiresCharging(true);
                        break;
                    }
                    case 3: {
                        builder.setMinimumLatency(period);
                        builder.setRequiresDeviceIdle(true);
                        break;
                    }
                    case 4: {
                        builder.setMinimumLatency(period);
                        builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_UNMETERED);
                        break;
                    }
                }

                builder.setPersisted(true);
                ((JobScheduler)v0).schedule(builder.build());
            }
        }
    }

    private static boolean hasAccount(AccountManager accountManager, String name) {
        Account[] v2;
        if(accountManager != null && !TextUtils.isEmpty(((CharSequence)name))) {
            try {
                v2 = accountManager.getAccountsByType(ACCOUNT_TYPE);
                if(v2 == null) {
                    return false;
                }
            }
            catch(Exception v1) {
                return false;
            }
            for(Account account: v2) {
                if (name.equalsIgnoreCase(account.name)){
                    return true;
                }
            }
        }

        return false;
    }

    public static void startJob(Context context) {
        BoosterLog.log("JJJJ startJob " );
        if(Build.VERSION.SDK_INT >= 21) {
            try {
                PackageManager v0_1 = context.getPackageManager();
                ComponentName v1 = new ComponentName(context.getPackageName(), WeLiveJobService.class.getName());

                v0_1.setComponentEnabledSetting(v1, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
            }
            catch(Throwable v0) {
            }

            try {
                scheduleJob(1, context);
                scheduleJob(2, context);
                scheduleJob(3, context);
                scheduleJob(4, context);
            }
            catch(Throwable v0) {
                v0.printStackTrace();
            }
        }
    }
}
