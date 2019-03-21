package winterfell.flash.vpn.reward;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;

import com.polestar.task.ADErrorCode;
import com.polestar.task.IProductStatusListener;
import com.polestar.task.network.datamodels.Product;

import java.util.ArrayList;

import winterfell.flash.vpn.FlashApp;
import winterfell.flash.vpn.FlashUser;
import winterfell.flash.vpn.utils.EventReporter;

/**
 * Created by guojia on 2019/3/21.
 */
public class ProductManager {
    private static ProductManager sInstance;
    private AppUser appUser;
    private Handler mainHandler;

    private ProductManager() {
        appUser = FlashUser.getInstance();
        mainHandler = new Handler(Looper.getMainLooper());
    }

    public static synchronized ProductManager getInstance() {
        if (sInstance == null) {
            sInstance = new ProductManager();
        }
        return sInstance;
    }

    public int canBuyProduct(Product product) {
        return canBuyProduct(product, 1);
    }

    public int canBuyProduct(Product product, int amount){
        if (appUser.getMyBalance() >= (product.mCost*amount)){
            return RewardErrorCode.PRODUCT_OK;
        } else {
            return RewardErrorCode.PRODUCT_NO_ENOUGH_COIN;
        }

    }

    public void buyProduct(Product product, IProductStatusListener listener, Object... args)  {
        buyProduct(product, 1, listener, args);
    }

    public void buyProduct(Product product, int amount, IProductStatusListener listener, Object... args)  {
        String email = null;
        String paypal = null;
        if (product.isMoneyProduct()) {
            email = (String) args[0];
        }
        if (product.isPaypal()) {
            paypal = (String) args[1];
        }
        appUser.buyProduct(product.mId, amount, email, paypal, new WrapProductStatusListener(listener, product));
    }

    private class WrapProductStatusListener implements IProductStatusListener {
        private IProductStatusListener mListener ;
        private Product mProduct ;

        public WrapProductStatusListener(IProductStatusListener listener, Product product) {
            mListener = listener;
            mProduct = product;
        }

        @Override
        public void onConsumeSuccess(final long id, final int amount, final float totalCost, final float balance) {
            appUser.updateMyBalance(balance);
            storeProduct(mProduct, amount);
            EventReporter.productEvent("consume_"+id);
            //TODO 发货
            mainHandler.post(new Runnable() {
                @Override
                public void run() {
                    mListener.onConsumeSuccess(id, amount, totalCost, balance);
                }
            });
        }

        @Override
        public void onConsumeFail(final ADErrorCode code) {
            EventReporter.productEvent("consume_fail_"+code.getErrCode());
            mainHandler.post(new Runnable() {
                @Override
                public void run() {
                    mListener.onConsumeFail(code);
                }
            });
        }

        @Override
        public void onGetAllAvailableProducts(final ArrayList<Product> products) {
            mainHandler.post(new Runnable() {
                @Override
                public void run() {
                    mListener.onGetAllAvailableProducts(products);
                }
            });
        }

        @Override
        public void onGeneralError(final ADErrorCode code) {
            EventReporter.rewardEvent("error_"+code.getErrCode());
            mainHandler.post(new Runnable() {
                @Override
                public void run() {
                    mListener.onGeneralError(code);
                }
            });
        }
    }

    private SharedPreferences getSharedPreference() {
        SharedPreferences sp = FlashApp.getApp().getSharedPreferences("reward_product", Context.MODE_PRIVATE);
        return  sp;
    }

    private void storeProduct(Product product, int amount) {
        if (!product.isFunctionalProduct()) {
            return;
        }
        switch (product.mProductType) {
            case Product.PRODUCT_TYPE_1_CLONE:
                addCloneCount(1);
                break;
            case Product.PRODUCT_TYPE_10_CLONE:
                addCloneCount(10);
                break;
            case Product.PRODUCT_TYPE_REMOVE_AD_1DAY:
                addAdFreeExpireTime(24*60*60*1000);
                break;
            case Product.PRODUCT_TYPE_REMOVE_AD_7DAY:
                addAdFreeExpireTime(7*24*60*60*1000);
                break;
            case Product.PRODUCT_TYPE_REMOVE_AD_30DAY:
                addAdFreeExpireTime(30*24*60*60*1000);
                break;
        }
    }

    private void addAdFreeExpireTime(long time) {
        getSharedPreference().edit().putLong("product_adfree_expire", getAdFreeExpireTime() + time).commit();
    }

    private long getAdFreeExpireTime() {
        long time = getSharedPreference().getLong("product_adfree_expire", 0);
        if (time < System.currentTimeMillis()) {
            time = System.currentTimeMillis() - 1000;
        }
        return  time;
    }

    public boolean checkAndConsumeAdFreeTime() {
        return getAdFreeExpireTime() > System.currentTimeMillis();
    }

    private int getCloneCount() {
        return getSharedPreference().getInt("product_clone", 0);
    }

    private void addCloneCount(int add) {
        getSharedPreference().edit().putInt("product_clone", getCloneCount() + add).commit();
    }

    public boolean checkAndConsumeClone(int num) {
        if (getCloneCount() >= num) {
            addCloneCount(-num);
            return true;
        }
        return false;
    }

}
