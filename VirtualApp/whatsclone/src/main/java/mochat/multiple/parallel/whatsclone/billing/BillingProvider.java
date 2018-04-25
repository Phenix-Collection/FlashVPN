package mochat.multiple.parallel.whatsclone.billing;

/**
 * Created by guojia on 2017/6/25.
 */

import android.os.Handler;
import android.os.Looper;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.Purchase;
import com.lody.virtual.client.core.VirtualCore;
import mochat.multiple.parallel.whatsclone.MApp;
import mochat.multiple.parallel.whatsclone.utils.AppManager;
import mochat.multiple.parallel.whatsclone.utils.MLogs;

import java.util.List;

/**
 * An interface that provides an access to BillingLibrary methods
 */
public class BillingProvider {
    private static BillingProvider sInstance;
    private BillingManager manager;
    private final String TAG = "BillingProvider";
    private boolean isAdFreeVIP;
    public static final String KEY ="~&{\u007FCq^cN%A/GN9wt}S# Ft{DUf#B@PD^]ZPxW'nU@uuX%]A]D\\nco$=g]Bp9be^!!}B\\@FbSt}g\\yO=sox_ZRNl\"C{\"Gn~pF\\\"Yu%|%S^uyAtn=zBUTECN";

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
                    AppManager.reloadLockerSetting();
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