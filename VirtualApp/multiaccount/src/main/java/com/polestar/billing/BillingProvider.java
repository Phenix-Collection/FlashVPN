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
    public static final String KEY ="[%R%pg[9Yrc\"/cs cn\\r.c/nT'{r&YB]dFcs{D|WdboGy}nt#{}.COu{d\u007FBAC}tnqwFP_q .B\"|ZL_u`frt[EnAB$B{S%!Tp\"#&A|S`QT9 x^f.aGzOQ.n_Z\u007F\\otNq#bp`eLzz^a%=BL}e9|YF=Cpt]c['y\"`Do{";

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
                        MLogs.d(TAG, "SKU:  " + purchase.getSku()+ " time: " + purchase.getPurchaseTime()
                                + " state: " + purchase.getPurchaseState());
                        switch (purchase.getSku()) {
                            case BillingConstants.SKU_AD_FREE:
                                MLogs.d(TAG, "Got a AD free version!!! ");
                                isAdFreeVIP = (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED);
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