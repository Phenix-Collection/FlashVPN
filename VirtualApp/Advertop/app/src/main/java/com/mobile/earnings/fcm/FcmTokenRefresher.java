package com.mobile.earnings.fcm;

import android.util.Log;

import rx.Observable;
import rx.Subscriber;
import rx_fcm.FcmRefreshTokenReceiver;
import rx_fcm.TokenUpdate;




public class FcmTokenRefresher implements FcmRefreshTokenReceiver{

	@Override
	public void onTokenReceive(Observable<TokenUpdate> oTokenUpdate){
		oTokenUpdate.subscribe(new Subscriber<TokenUpdate>(){
			@Override
			public void onCompleted(){

			}

			@Override
			public void onError(Throwable e){
				Log.e("FCM", "TokenUpdateError: "+e.getMessage());
			}

			@Override
			public void onNext(TokenUpdate tokenUpdate){
				Log.i("FCM", "TokenUpdate: "+ tokenUpdate.getToken());
			}
		});
	}
}
