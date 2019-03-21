package winterfell.flash.vpn.billing;

/**
 * Created by guojia on 2017/6/25.
 */

import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsResponseListener;

import java.util.ArrayList;
import java.util.List;

import winterfell.flash.vpn.FlashApp;
import winterfell.flash.vpn.FlashUser;
import winterfell.flash.vpn.utils.MLogs;
import winterfell.flash.vpn.utils.PreferenceUtils;

/**
 * An interface that provides an access to BillingLibrary methods
 */
public class BillingProvider {
    private static BillingProvider sInstance;
    private BillingManager manager;
    private final String TAG = "BillingProvider";

    private OnStatusUpdatedListener statusUpdatedListener;

    private BillingProvider() {
            manager = new BillingManager(FlashApp.getApp(), new BillingManager.BillingUpdatesListener() {
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
                        MLogs.d(TAG, "SKU:  " + purchase);
                        switch (purchase.getSku()) {
//                            case BillingConstants.SKU_PREMIUM_1_MONTH:
//                            case BillingConstants.SKU_PREMIUM_3_MONTH:
//                            case BillingConstants.SKU_PREMIUM_12_MONTH:
                            default:
                                if(purchase.isAutoRenewing()) {
                                    MLogs.d(TAG, "Got a AD free version!!! ");
                                    FlashUser.getInstance().setVIP(true);
                                }
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
                }
            });
            refreshSubsSkuDetails();
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

    public interface OnStatusUpdatedListener {
        void onStatusUpdated();
    }

    public void updateStatus(OnStatusUpdatedListener listener) {
        statusUpdatedListener = listener;
        if (manager != null) {
            manager.queryPurchases();
        }
    }

    //NEED to sync with SkuDetails.toString()
    private static final String SPLIT = "SkuDetails: ";
    public static final String PREF_CACHE_SKU_LIST = "pref_sku_list";

    public void querySkuDetails( final String itemType, final SkuDetailsResponseListener listener){
        String cache = PreferenceUtils.getString(FlashApp.getApp(), PREF_CACHE_SKU_LIST, null);
        if (cache != null) {
            try {
                String[] array = cache.split(SPLIT);
                List<SkuDetails> list = new ArrayList<>();
                for(String s:array){
                    if(!TextUtils.isEmpty(s)){
                        list.add(new SkuDetails(s));
                    }
                }
                listener.onSkuDetailsResponse(BillingClient.BillingResponse.OK, list);
                MLogs.d(TAG,"sku query from cache");
                return;
            }catch (Throwable ex) {
                ex.printStackTrace();
            }
        }
        if (manager != null) {
            manager.querySkuDetailsAsync(itemType, BillingConstants.getSkuList(itemType), listener);
        }
    }

    private void refreshSubsSkuDetails() {
        manager.querySkuDetailsAsync(BillingClient.SkuType.SUBS,
                BillingConstants.getSkuList(BillingClient.SkuType.SUBS), new SkuDetailsResponseListener() {
                    @Override
                    public void onSkuDetailsResponse(int responseCode, List<SkuDetails> skuDetailsList) {
                    }
                });
    }
}