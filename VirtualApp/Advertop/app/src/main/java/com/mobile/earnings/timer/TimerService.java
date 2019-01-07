package com.mobile.earnings.timer;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import com.mobile.earnings.App;
import com.mobile.earnings.R;
import com.mobile.earnings.api.data_models.AppModel;
import com.mobile.earnings.single.task.DetailedTaskActivity;

import static com.mobile.earnings.utils.Constantaz.PREF_TIMER_DONE;



public class TimerService extends Service implements Timer{

	public static final  String PAUSE           = "should_be_paused";
	public static final int NOTIFICATION_ID = 152;

	private IBinder binder;
	private boolean isStarted = false;
	private TimerHandler timerHandler;
	private long         startTimeInMs;
	private int timerInSeconds = 20;
	private NotificationManager notificationManager;
	private AppModel appModel;

	@Nullable
	@Override
	public IBinder onBind(Intent intent){
		if(binder == null) {
			return binder = new TimerBinder();
		}
		return binder;
	}

	@Override
	public void onCreate(){
		super.onCreate();
		notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		Log.e("TIMER", "CREATED");
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId){
		boolean pause = intent.getBooleanExtra(PAUSE, false);
		appModel = intent.getParcelableExtra(DetailedTaskActivity.EXTRA_FROM_PUSH);
		if(pause) {
			if(isStarted)
				pauseTimer();
		} else{
			if(!isStarted)
				startTimer();
		}
		return START_NOT_STICKY;
	}

	@Override
	public void onDestroy(){
		Log.e("TIMER", "destroyed");
		stopTimer();
		super.onDestroy();
	}

	@Override
	public void update(){
		long currentTime = System.currentTimeMillis();
		int timePassedInSeconds = (int) (((currentTime - startTimeInMs) / 1000) % 60);
		int secondsLeft = timerInSeconds - timePassedInSeconds;
		updateNotification(secondsLeft);
		if(secondsLeft < 0) {
			stopTimer();
			stopSelf();
			notificationManager.notify(NOTIFICATION_ID + 1, createNotification(secondsLeft));
		}
		Log.e("TIMER", "Seconds: " + secondsLeft);
	}

	private void startTimer(){
		if(timerHandler == null) {
			timerHandler = new TimerHandler(this);
		}
		isStarted = true;
		startTimeInMs = System.currentTimeMillis();
		timerHandler.sendEmptyMessage(TimerHandler.WHAT);
		startForeground(NOTIFICATION_ID, createNotification(timerInSeconds));
		Log.e("TIMER", "START");
	}

	private void pauseTimer(){
		long currentTime = System.currentTimeMillis();
		int timePassedInSeconds = (int) (((currentTime - startTimeInMs) / 1000) % 60);
		timerInSeconds -= timePassedInSeconds;
		isStarted = false;
		timerHandler.removeMessages(TimerHandler.WHAT);
		timerHandler.removeCallbacksAndMessages(null);
		Log.e("TIMER", "PAUSE");
	}

	private void stopTimer(){
		Log.e("TIMER", "STOP");
		isStarted = false;
		timerHandler.removeMessages(TimerHandler.WHAT);
		timerHandler.removeCallbacksAndMessages(null);
		App.getPrefs().edit().putBoolean(PREF_TIMER_DONE, true).apply();
	}

	private void updateNotification(int seconds){
		notificationManager.notify(NOTIFICATION_ID, createNotification(seconds));
	}

	@NonNull
	private Notification createNotification(int seconds){
		String contentText = "";
		if(seconds < 0) {
			contentText = App.getContext().getString(R.string.timer_finished);
		} else{
			contentText = String.format(App.getContext().getString(R.string.timer_update), seconds);
		}
		NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
		builder.setCategory(NotificationCompat.CATEGORY_ALARM).setContentTitle(App.getContext().getString(R.string.app_name)).setContentText(contentText).setColor(App.getRes().getColor(R.color.colorPrimary)).setPriority(android.support.v4.app.NotificationCompat.PRIORITY_HIGH);
		if(seconds < 0 && appModel != null){
			PendingIntent intent = PendingIntent.getActivity(this, 0,
					DetailedTaskActivity.getDetailedTaskIntent(this,
							appModel, true), PendingIntent.FLAG_ONE_SHOT);
			builder.setAutoCancel(true);
			builder.setContentIntent(intent);
		}
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			builder.setSmallIcon(R.drawable.ic_notification);
			builder.setLargeIcon(BitmapFactory.decodeResource(App.getContext().getResources(), R.mipmap.ic_launcher));
			builder.setVisibility(Notification.VISIBILITY_PUBLIC);
		} else{
			builder.setSmallIcon(R.mipmap.ic_launcher);
		}
		return builder.build();
	}

	public class TimerBinder extends Binder{

		TimerService getService(){
			return TimerService.this;
		}
	}
}
