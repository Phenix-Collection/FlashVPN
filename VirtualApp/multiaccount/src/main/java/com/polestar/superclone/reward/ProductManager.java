package com.polestar.superclone.reward;

import android.os.Handler;
import android.os.Looper;

import com.polestar.task.ADErrorCode;
import com.polestar.task.IProductStatusListener;
import com.polestar.task.network.AdApiHelper;
import com.polestar.task.network.datamodels.Product;

import java.util.ArrayList;

/**
 * Created by guojia on 2019/2/1.
 */

public class ProductManager {
    private static ProductManager sInstance;
    private AppUser appUser;
    private Handler mainHandler;

    private ProductManager() {
        appUser = AppUser.getInstance();
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
        return appUser.checkProduct(product, amount);

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
        appUser.buyProduct(product.mId, amount, email, paypal, new WrapProductStatusListener(listener));
    }

    public boolean consumeProduct(Product product, int amount) {
        if (getProductCount(product) < amount) {
            return false;
        }
        return false;
    }

    public boolean consumeProduct(Product product) {
        return consumeProduct(product, 1);
    }

    public int getProductCount(Product product) {
        return 0;
    }


    private class WrapProductStatusListener implements IProductStatusListener {
        private IProductStatusListener mListener ;

        public WrapProductStatusListener(IProductStatusListener listener) {
            mListener = listener;
        }

        @Override
        public void onConsumeSuccess(long id, int amount, float totalCost, float balance) {
            appUser.updateMyBalance(balance);
            //TODO 发货
            mainHandler.post(new Runnable() {
                @Override
                public void run() {
                    mListener.onConsumeSuccess(id, amount, totalCost, balance);
                }
            });
        }

        @Override
        public void onConsumeFail(ADErrorCode code) {
            mainHandler.post(new Runnable() {
                @Override
                public void run() {
                    mListener.onConsumeFail(code);
                }
            });
        }

        @Override
        public void onGetAllAvailableProducts(ArrayList<Product> products) {
            mainHandler.post(new Runnable() {
                @Override
                public void run() {
                    mListener.onGetAllAvailableProducts(products);
                }
            });
        }

        @Override
        public void onGeneralError(ADErrorCode code) {
            mainHandler.post(new Runnable() {
                @Override
                public void run() {
                    mListener.onGeneralError(code);
                }
            });
        }
    }

}
