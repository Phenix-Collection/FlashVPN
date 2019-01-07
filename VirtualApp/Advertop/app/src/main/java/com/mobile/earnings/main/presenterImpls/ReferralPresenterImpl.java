package com.mobile.earnings.main.presenterImpls;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.util.Log;

import com.mobile.earnings.App;
import com.mobile.earnings.R;
import com.mobile.earnings.api.RetroFitServiceFactory;
import com.mobile.earnings.api.responses.UserResponse;
import com.mobile.earnings.api.services.ProfileApi;
import com.mobile.earnings.main.presenters.ReferralPresenter;
import com.mobile.earnings.main.views.ReferralView;
import com.mobile.earnings.utils.Constantaz;

import org.apache.commons.lang3.StringEscapeUtils;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.mobile.earnings.utils.HttpResponseCodes.HTTP_SUCCESS;
import static com.mobile.earnings.utils.HttpResponseCodes.HTTP_USER_NOT_FOUND;

public class ReferralPresenterImpl implements ReferralPresenter{

	private static final String TAG = "FRIENDS_FRAGMENT";
	private ReferralView view;

	public ReferralPresenterImpl(ReferralView view){
		this.view = view;
	}

	@Override
	public void getDataFromServer(final boolean isActive){
		String deviceId = App.getDeviceID();
		ProfileApi service = RetroFitServiceFactory.createSimpleRetroFitService(ProfileApi.class);
		service.getUserInfo(deviceId, Constantaz.DEVICE_TYPE).enqueue(new Callback<UserResponse>(){
			@Override
			public void onResponse(Call<UserResponse> call, Response<UserResponse> response){
				switch(response.code()){
					case HTTP_SUCCESS:
						UserResponse userResponse = response.body();
						int firstReferralCount = userResponse.userInfo.referralFirstCount;
						String firstReferralBalance = String.valueOf(userResponse.userInfo.referralFirstBalance);
						int secondReferralCount = userResponse.userInfo.referralSecondCount;
						String secondReferralBalance = String.valueOf(userResponse.userInfo.referralSecondBalance);
						String currency = StringEscapeUtils.unescapeJava("\\" + userResponse.userInfo.currency);
						if(isActive){
							view.setData(userResponse.userInfo.referralActiveCount, secondReferralCount, secondReferralBalance, currency);
						} else view.setData(0, firstReferralCount, firstReferralBalance, currency);
						break;
					case HTTP_USER_NOT_FOUND:
						view.informUser(R.string.refillRequestUserNotFound);
						break;
				}
			}

			@Override
			public void onFailure(Call<UserResponse> call, Throwable t){
				Log.e("REFERRAL", "onFailure: "+t.getMessage());
			}
		});
	}

	@Override
	public void copyCodeToBuffer(Context context, String promoCode){
		ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
		ClipData clip = ClipData.newPlainText("promoCode", promoCode);
		clipboard.setPrimaryClip(clip);
		view.informUser(R.string.referralFrag_promoCopied);
	}
}
