package com.polestar.task.network.responses;

import com.google.gson.annotations.SerializedName;
import com.polestar.task.network.datamodels.Product;

import java.util.ArrayList;

public class ProductsResponse {
    @SerializedName("products")
    public ArrayList<Product> mProducts;
}
