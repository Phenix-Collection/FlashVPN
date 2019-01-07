package com.mobile.earnings.timer;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;



public class TimerHandler extends Handler{

	public static final int WHAT = 15;
	private static final long DELAY = 1000L;
	@NonNull
	private Timer updater;

	public TimerHandler(@NonNull Timer updater){
		super(Looper.getMainLooper());
		this.updater = updater;
	}

	@Override
	public void handleMessage(Message msg){
		super.handleMessage(msg);
		if(WHAT == msg.what){
			updater.update();
			sendEmptyMessageDelayed(WHAT, DELAY);
		}
	}
}
