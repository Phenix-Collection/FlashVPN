package com.mobile.earnings.main.ali_promo;

import android.os.Handler;
import android.os.Message;

import com.mobile.earnings.main.MainActivity;

import java.lang.ref.WeakReference;




public class TimerUpdateHandler extends Handler{

	public static final  int MESSAGE_UPDATE_TIME = 124;
	private static final int MESSAGE_DELAY_IN_MS = 1000;
	private final WeakReference<MainActivity> activity;

	public TimerUpdateHandler(MainActivity activity){
		this.activity = new WeakReference<>(activity);
	}

	@Override
	public void handleMessage(Message msg){
		super.handleMessage(msg);
		if(msg.what == MESSAGE_UPDATE_TIME) {
			MainActivity mainActivity = activity.get();
			if(mainActivity != null && mainActivity.updateTimer()) {
				sendEmptyMessageDelayed(MESSAGE_UPDATE_TIME, MESSAGE_DELAY_IN_MS);
			}
		}
	}
}
