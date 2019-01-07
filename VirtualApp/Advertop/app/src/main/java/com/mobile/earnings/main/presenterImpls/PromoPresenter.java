package com.mobile.earnings.main.presenterImpls;

import android.util.Log;

import com.mobile.earnings.R;
import com.mobile.earnings.api.RetroFitServiceFactory;
import com.mobile.earnings.api.responses.SendCopyAliPromoCode;
import com.mobile.earnings.api.services.AliCopyService;
import com.mobile.earnings.main.views.PromoView;
import com.crashlytics.android.Crashlytics;

import org.apache.commons.lang3.StringUtils;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.mobile.earnings.utils.Constantaz.GLAI_ENDPOINT;
import static com.mobile.earnings.utils.HttpResponseCodes.HTTP_SUCCESS;




public class PromoPresenter{

	private PromoView view;

	public PromoPresenter(PromoView view){
		this.view = view;
	}

	public void activatePromoCode(final String promoCode, String endPointUrl, final int offerId){
		try{
			if(!StringUtils.isNumeric(promoCode)) {
				view.informUser(R.string.promoAct_notANumberException);
				return;
			}
		} catch(NumberFormatException e){
			Crashlytics.logException(e);
		}
		AliCopyService service = RetroFitServiceFactory.createCustomEndPointService(AliCopyService.class, endPointUrl);
		long promo = 0L;
		try{
			promo = Long.valueOf(promoCode);
		} catch(NumberFormatException e){
			Crashlytics.logException(e);
		}
		Call<SendCopyAliPromoCode> call = service.registerPromo(offerId, promo);
		call.enqueue(new Callback<SendCopyAliPromoCode>(){
			@Override
			public void onResponse(Call<SendCopyAliPromoCode> call, Response<SendCopyAliPromoCode> response){
				switch(response.code()){
					case HTTP_SUCCESS:
						SendCopyAliPromoCode data = response.body();
						if(data != null) {
							String description = data.description;
							switch(data.resultCode){
								case 0:
									view.codeSubmitted(data);
									break;
								case 2:
									int hardcodedGlaiOfferId = 10003;
									if(offerId == hardcodedGlaiOfferId) {
										view.informUser(R.string.promoAct_promoNotExistsException);
									} else{
										activatePromoCode(promoCode, GLAI_ENDPOINT, hardcodedGlaiOfferId);
									}
									break;
								case 3:
									view.informUser(R.string.promoAct_promoActivatedException);
									break;
								case 4:
									view.informUser(R.string.promoAct_bonusNotReadyException);
									break;
								default:
									view.informUser(R.string.promoAct_promoSomeError);
									break;
							}
						}
						break;
					default:
						break;
				}
			}

			@Override
			public void onFailure(Call<SendCopyAliPromoCode> call, Throwable t){
				Log.e("ALI_COPY", "onFailure: " + t.getMessage());
			}
		});
	}
}
