package com.mobile.earnings.api;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.mobile.earnings.utils.Constantaz.APP_PAY_ENDPOINT;

public class RetroFitServiceFactory{

	public static <T> T createSimpleRetroFitService(final Class<T> clazz){
		HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
		interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
		OkHttpClient client = new OkHttpClient.Builder().addInterceptor(interceptor).build();
		final Retrofit retrofit = new Retrofit.Builder()
				.addConverterFactory(GsonConverterFactory.create())
				.baseUrl(APP_PAY_ENDPOINT)
				.client(client)
				.build();
		return retrofit.create(clazz);
	}

	public static <T> T createCustomEndPointService(Class<T> clazz, String endPoint){
		HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
		interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
		OkHttpClient client = new OkHttpClient.Builder().addInterceptor(interceptor).build();
		final Retrofit retrofit = new Retrofit.Builder()
				.addConverterFactory(GsonConverterFactory.create())
				.baseUrl(endPoint)
				.client(client)
				.build();
		return retrofit.create(clazz);
	}

}
