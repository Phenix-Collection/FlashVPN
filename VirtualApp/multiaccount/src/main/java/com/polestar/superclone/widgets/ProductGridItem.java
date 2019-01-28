package com.polestar.superclone.widgets;

import android.content.Context;
import android.graphics.drawable.Drawable;

import com.polestar.superclone.reward.AssetHelper;
import com.polestar.task.network.datamodels.Product;

/**
 * Created by PolestarApp on 2017/7/17.
 */

public class ProductGridItem {
    public long id;
    public CharSequence name;
    public String description;
    public float price;
    public Drawable icon;
    public String iconUrl;

    private Product mProduct;
    public Product getProduct() {
        return mProduct;
    }

    public static ProductGridItem fromProduct(Product product) {
        ProductGridItem ret = new ProductGridItem();
        ret.mProduct = product;
        ret.name = product.mName;
        ret.description = product.mDescription;
        ret.price = product.mCost;
        ret.iconUrl = product.mIconUrl;
//        if (ret.iconUrl == null || ret.iconUrl.isEmpty()) {
//            ret.iconUrl = "default_product.png"; //TODO change me
//        }
        ret.id = product.mId;
        return ret;
    }

    public void loadIcon(Context context) {
        //目前的实现就是从asset里面去load
        Drawable ret = AssetHelper.getDrawable(context, iconUrl);
        if (ret != null) {
            icon = ret;
        }
    }
}
