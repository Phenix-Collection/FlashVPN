package com.polestar.task.network.services;

import com.polestar.task.network.responses.ProductsResponse;
import com.polestar.task.network.responses.UserProductResponse;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface ProductsApi {
    @Headers("Accept: application/json")
    @GET("api/v1/product/getAvailableProducts")
    Call<ProductsResponse> getAvailableProducts(@Query("device_id") String deviceId);

    @Headers("Accept: application/json")
    @POST("api/v1/product/consumeProduct")
    @FormUrlEncoded
    Call<UserProductResponse> consumeProduct(@Field("device_id") String deviceID,
                                             @Field("product_id") long productId,
                                             @Field("amount") int amount,
                                             @Field("email") String email,
                                             @Field("info") String info  //may contain various info base on product type);
                            );
}
