package com.polestar.task;

import com.polestar.task.network.datamodels.Product;

import java.util.ArrayList;

/**
 * Created by guojia on 2019/1/19.
 */

public interface IProductStatusListener {
    /**
     * callback when svr confirmed the purchase finished, and coins deducted from users balance
     * after purchase success, app will enable functions for user, e.g remove ads, clone num, vpn hour
     * @param id product ID
     * @param amount  amount of products
     * @param totalCost total cost of coins
     * @param balance balance updated from server
     */
    void onConsumeSuccess(long id, int amount, float totalCost, float balance);
    void onConsumeFail(ADErrorCode code);

    void onGetAllAvailableProducts(ArrayList<Product> products);

    void onGeneralError(ADErrorCode code);
}
