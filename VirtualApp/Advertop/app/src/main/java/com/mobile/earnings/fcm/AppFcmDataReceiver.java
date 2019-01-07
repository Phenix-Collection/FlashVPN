package com.mobile.earnings.fcm;

import android.os.Bundle;
import android.util.Log;

import com.mobile.earnings.App;
import com.mobile.earnings.utils.NotificationCreator;

import rx.Observable;
import rx.functions.Action1;
import rx_fcm.FcmReceiverData;
import rx_fcm.Message;




public class AppFcmDataReceiver implements FcmReceiverData{

	@Override
	public Observable<Message> onNotification(Observable<Message> oMessage){
		return oMessage.doOnNext(new Action1<Message>(){
			@Override
			public void call(Message message){
				Log.e("FCM", "Global data: " + message.payload().toString());
				Bundle payload = message.payload();
				String taskType = "";
				int appId = -1;
				String text = "";
				if(payload != null) {
					taskType = message.payload().getString("type");
					String stringAppId = message.payload().getString("app_id");
					appId = Integer.valueOf(stringAppId);
					text = message.payload().getString("message");
				}
				Log.e("TAGA", "ReceiverAppId: "+appId);
				if(taskType != null && !taskType.isEmpty()) {
					NotificationCreator.createAndNotifyNotification(App.getContext(), taskType, appId, text);
				}
			}
		});
	}
}
