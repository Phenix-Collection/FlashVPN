package com.mobile.earnings.main.presenterImpls;

import android.support.annotation.NonNull;
import android.util.Log;

import com.mobile.earnings.App;
import com.mobile.earnings.R;
import com.mobile.earnings.api.RetroFitServiceFactory;
import com.mobile.earnings.api.data_models.PaymentSystem;
import com.mobile.earnings.api.data_models.PaymentSystemResponse;
import com.mobile.earnings.api.responses.ReplenishmentResponse;
import com.mobile.earnings.api.responses.UserFundsResponse;
import com.mobile.earnings.api.services.FundsApi;
import com.mobile.earnings.main.presenters.FundsPresenter;
import com.mobile.earnings.main.views.FundsView;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.mobile.earnings.utils.HttpResponseCodes.HTTP_AMOUNT_UNDER_LIMIT;
import static com.mobile.earnings.utils.HttpResponseCodes.HTTP_NOT_ENOUGH_FUNDS;
import static com.mobile.earnings.utils.HttpResponseCodes.HTTP_PRIVAT_ERROR;
import static com.mobile.earnings.utils.HttpResponseCodes.HTTP_REQUEST_LIMIT_ERROR;
import static com.mobile.earnings.utils.HttpResponseCodes.HTTP_SUCCESS;
import static com.mobile.earnings.utils.HttpResponseCodes.HTTP_USER_NOT_FOUND;

public class FundsPresenterImpl implements FundsPresenter{

	private FundsView view;

	public FundsPresenterImpl(FundsView view){
		this.view = view;
	}

	@Override
	public void getFundsFromServer(){
		view.showLoading();
		FundsApi service = RetroFitServiceFactory.createSimpleRetroFitService(FundsApi.class);
		Call<UserFundsResponse> call = service.getFunds(App.getDeviceID());
		call.enqueue(new Callback<UserFundsResponse>(){
			@Override
			public void onResponse(Call<UserFundsResponse> call, Response<UserFundsResponse> response){
				switch(response.code()){
					case HTTP_SUCCESS:
						UserFundsResponse userFunds = response.body();
						float referralBalance = userFunds.balance.firstReferralBalance + userFunds.balance.secondReferralBalance;
						view.setDataFromServer(userFunds.balance.ownBalance, referralBalance, userFunds.balance.totalBalance, userFunds.balance.expectedFunds, userFunds.balance.currencySign);
						break;
					case HTTP_USER_NOT_FOUND:
						view.informUser(R.string.refillRequestUserNotFound);
						break;
					default:
						break;
				}
				getPaymentSystems();
			}

			@Override
			public void onFailure(Call<UserFundsResponse> call, Throwable t){
				Log.e("FUNDS", "onFailure: " + t.getMessage());
				view.hideLoading();
			}
		});
	}

	@Override
	public void getPaymentSystems(){
		FundsApi service = RetroFitServiceFactory.createSimpleRetroFitService(FundsApi.class);
		Call<PaymentSystemResponse> call = service.getPaymentSystems(App.getDeviceID());
		call.enqueue(new Callback<PaymentSystemResponse>(){
			@Override
			public void onResponse(@NonNull Call<PaymentSystemResponse> call, @NonNull Response<PaymentSystemResponse> response){
				view.hideLoading();
				switch(response.code()){
					case HTTP_SUCCESS:
						PaymentSystemResponse paymentSystemResponse = response.body();
						if(paymentSystemResponse != null) {
							view.createPaymentSystemSheet(addPaymentSystemIcons(paymentSystemResponse.data));
							view.canReleaseFunds(paymentSystemResponse.canReleaseFunds);
						}
						break;
					case HTTP_USER_NOT_FOUND:
						view.informUser(R.string.refillRequestUserNotFound);
						break;
					default:
						break;
				}
			}

			@Override
			public void onFailure(@NonNull Call<PaymentSystemResponse> call, @NonNull Throwable t){
				view.hideLoading();
				Log.e("FUNDS", "onFailure: " + t.getMessage());
			}
		});
	}

	public void checkAmount(final String account, final float amount, final String name){
		FundsApi service = RetroFitServiceFactory.createSimpleRetroFitService(FundsApi.class);
		Call<ReplenishmentResponse> call = service.check(App.getDeviceID(), amount);
		call.enqueue(new Callback<ReplenishmentResponse>(){
			@Override
			public void onResponse(Call<ReplenishmentResponse> call, Response<ReplenishmentResponse> response){
				ReplenishmentResponse replenishmentResponse = response.body();
				view.hideLoading();
				switch(response.code()){
					case HTTP_SUCCESS:
						if(replenishmentResponse != null) {
							if(replenishmentResponse.getStatus()) {
								view.showRefillDialog(account, replenishmentResponse.getAmount(), replenishmentResponse.getAmountClean(), name);
							} else{
								view.informAboutLimit(replenishmentResponse.getLimit());
							}
						} else{
							view.informUser(R.string.someErrorText);
						}
						break;
					case HTTP_USER_NOT_FOUND:
						view.informUser(R.string.refillRequestUserNotFound);
						break;
					case HTTP_NOT_ENOUGH_FUNDS:
						view.informUser(R.string.roulette_NoFundsError);
						break;
					case HTTP_AMOUNT_UNDER_LIMIT:
						if(replenishmentResponse != null) {
							view.informAboutLimit(replenishmentResponse.getLimit());
						} else{
							view.informUser(R.string.someErrorText);
						}
						break;
					default:
						break;
				}
			}

			@Override
			public void onFailure(Call<ReplenishmentResponse> call, Throwable t){
				Log.e("FUNDS", "onFailure: " + t.getMessage());
				view.hideLoading();
			}
		});
	}

	public void transferFunds(){
		view.showLoading();
		FundsApi service = RetroFitServiceFactory.createSimpleRetroFitService(FundsApi.class);
		Call<UserFundsResponse> call = service.transfer(App.getDeviceID());
		call.enqueue(new Callback<UserFundsResponse>(){
			@Override
			public void onResponse(Call<UserFundsResponse> call, Response<UserFundsResponse> response){
				switch(response.code()){
					case HTTP_SUCCESS:
						view.hideLoading();
						UserFundsResponse userFunds = response.body();
						float referralBalance = userFunds.balance.firstReferralBalance + userFunds.balance.secondReferralBalance;
						view.setDataFromServer(userFunds.balance.ownBalance, referralBalance, userFunds.balance.totalBalance, userFunds.balance.expectedFunds, userFunds.balance.currencySign);
						view.informUser(R.string.fragmentFunds_moneyTransferred);
						break;
					case HTTP_USER_NOT_FOUND:
						view.hideLoading();
						view.informUser(R.string.refillRequestUserNotFound);
						break;
					case HTTP_NOT_ENOUGH_FUNDS:
						view.hideLoading();
						view.informUser(R.string.roulette_NoFundsError);
						break;
					default:
						view.hideLoading();
						break;
				}
			}

			@Override
			public void onFailure(Call<UserFundsResponse> call, Throwable t){
				Log.e("FUNDS", "onFailure: " + t.getMessage());
				view.hideLoading();
			}
		});
	}

	public void refillUserBalance(String userAccount, float amount, String paymentSystemName){
		FundsApi service = RetroFitServiceFactory.createSimpleRetroFitService(FundsApi.class);
		Call<ReplenishmentResponse> call = service.refillBalance(App.getDeviceID(), userAccount, amount, paymentSystemName);
		call.enqueue(new Callback<ReplenishmentResponse>(){
			@Override
			public void onResponse(Call<ReplenishmentResponse> call, Response<ReplenishmentResponse> response){
				switch(response.code()){
					case HTTP_SUCCESS:
						view.hideLoading();
						view.informUser(R.string.purchaseDone);
						getFundsFromServer();
						break;
					case HTTP_USER_NOT_FOUND:
						view.hideLoading();
						view.informUser(R.string.refillRequestUserNotFound);
						break;
					case HTTP_NOT_ENOUGH_FUNDS:
						view.hideLoading();
						view.informUser(R.string.roulette_NoFundsError);
						break;
					case HTTP_AMOUNT_UNDER_LIMIT:
						view.hideLoading();
						view.informAboutLimit(response.body().getLimit());
						break;
					case HTTP_PRIVAT_ERROR:
						view.hideLoading();
						view.informUser(R.string.fundsFrag_privatError);
						break;
					case HTTP_REQUEST_LIMIT_ERROR:
						view.hideLoading();
						view.informUser(R.string.fundsFrag_privatRequestLimitError);
						break;
					default:
						view.hideLoading();
						break;
				}
			}

			@Override
			public void onFailure(Call<ReplenishmentResponse> call, Throwable t){
				Log.e("FUNDS", "onFailure: " + t.getMessage());
				view.hideLoading();
			}
		});
	}

	private ArrayList<PaymentSystem> addPaymentSystemIcons(ArrayList<PaymentSystem> items){
		for(PaymentSystem system : items){
			switch(system.name){
				case "Webmoney":
					system.icon = R.drawable.ic_web_money;
					break;
				case "Qiwi":
					system.icon = R.drawable.ic_qiwi;
					break;
				case "Yandex":
					system.icon = R.drawable.ic_yandex;
					break;
				case "Tele2":
					system.icon = R.drawable.ic_tele2;
					break;
				case "Megafon":
					system.icon = R.drawable.ic_megafon;
					break;
				case "Beeline":
					system.icon = R.drawable.ic_beeline;
					break;
				case "Mts":
					system.icon = R.drawable.ic_mts;
					break;
				case "Kyivstar":
					system.icon = R.drawable.ic_kyivstar;
					break;
				case "Lifecell":
					system.icon = R.drawable.ic_lifecell;
					break;
				case "Vodafone":
					system.icon = R.drawable.ic_vodafone;
					break;
				case "Visa":
					system.icon = R.drawable.ic_visa;
					break;
				default:
					system.icon = R.drawable.ic_mastercard;
					break;
			}
		}
		return items;
	}
}
