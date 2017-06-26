package com.polestar.billing;

/**
 * Created by guojia on 2017/6/25.
 */

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.Purchase;
import com.lody.virtual.client.core.VirtualCore;
import com.polestar.multiaccount.MApp;
import com.polestar.multiaccount.utils.MLogs;
import com.polestar.multiaccount.utils.PreferencesUtils;

import java.util.List;

/**
 * An interface that provides an access to BillingLibrary methods
 */
public class BillingProvider {
    private static BillingProvider sInstance;
    private BillingManager manager;
    private final String TAG = "BillingProvider";
    private boolean isAdFreeVIP;

    private OnStatusUpdatedListener statusUpdatedListener;

    private BillingProvider() {
        if (VirtualCore.get().isMainProcess()) {
            manager = new BillingManager(MApp.getApp(), new BillingManager.BillingUpdatesListener() {
                @Override
                public void onBillingClientSetupFinished() {
                    MLogs.d(TAG, "onBillingClientSetupFinished");
                }

                @Override
                public void onConsumeFinished(String token, @BillingClient.BillingResponse int result) {

                }

                @Override
                public void onPurchasesUpdated(List<Purchase> purchases) {
                    for (Purchase purchase : purchases) {
                        switch (purchase.getSku()) {
                            case BillingConstants.SKU_AD_FREE:
                                MLogs.d(TAG, "Got a AD free version!!!");
                                isAdFreeVIP = true;
                                break;
                        }
                    }
                    if (statusUpdatedListener != null) {
                        new Handler(Looper.myLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                if (statusUpdatedListener != null) {
                                    statusUpdatedListener.onStatusUpdated();
                                }
                            }
                        });
                    }
                    VirtualCore.get().reloadLockerSetting(PreferencesUtils.getEncodedPatternPassword(MApp.getApp()), PreferencesUtils.isAdFree());
                }
            });
        }
    }

    synchronized public static BillingProvider get() {
        if (sInstance == null ) {
            sInstance = new BillingProvider();
        }
        return  sInstance;
    }

    public BillingManager getBillingManager() {
        return manager;
    }

    public boolean isAdFreeVIP() {
        return isAdFreeVIP;
    }

    public interface OnStatusUpdatedListener {
        void onStatusUpdated();
    }

    public void updateStatus(OnStatusUpdatedListener listener) {
        statusUpdatedListener = listener;
        if (manager != null) {
            manager.queryPurchases();
        }
    }
}