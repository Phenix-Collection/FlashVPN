package com.mobile.earnings.fcm;

import android.util.Log;

import rx.Observable;
import rx.Subscriber;
import rx_fcm.FcmReceiverUIBackground;
import rx_fcm.Message;




public class FcmBackgroundReceiver implements FcmReceiverUIBackground{
	@Override
	public void onNotification(Observable<Message> oMessage){
		oMessage.subscribe(new Subscriber<Message>(){
			@Override
			public void onCompleted(){

			}

			@Override
			public void onError(Throwable e){
				Log.e("FCM", "Some error occurs: "+e.getMessage());
			}

			@Override
			public void onNext(Message message){
				Log.e("FCM", "DATA: "+message.payload().toString());
			}
		});
	}
}
