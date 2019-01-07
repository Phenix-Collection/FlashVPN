package com.mobile.earnings.main.ali_promo;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.mobile.earnings.App;
import com.mobile.earnings.R;
import com.mobile.earnings.main.MainActivity;




public class AliPromoService extends Service{

	public static final int    ALI_SERVICE_NOTIFICATION_ID  = 734;
	public static final String EXTRA_ALI_SERVICE_END_TIME   = "end_time";
	public static final String EXTRA_ALI_SERVICE_TIMER_ENDS = "timer_ends";

	private long mStartTime;
	private int  mEndTime;
	private boolean mIsTimerRunning  = false;
	private IBinder mServiceBinder   = new AliPromoBinder();
	private boolean mIsServiceForeground = false;

	@Override
	public void onCreate(){
		super.onCreate();
		mStartTime = 0;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId){
		mEndTime = intent.getIntExtra(EXTRA_ALI_SERVICE_END_TIME, 0);
		return START_NOT_STICKY;
	}

	@Nullable
	@Override
	public IBinder onBind(Intent intent){
		return mServiceBinder;
	}

	public void runBackground(){
		stopForeground(true);
		mIsServiceForeground = false;
	}

	public void runForeground(){
		mIsServiceForeground = true;
		startForeground(ALI_SERVICE_NOTIFICATION_ID, createNotification(getSecondsLeft()));
	}

	public boolean isServiceForeground(){
		return mIsServiceForeground;
	}

	public void updateNotification(int secondsLeft){
		NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		manager.notify(ALI_SERVICE_NOTIFICATION_ID, createNotification(secondsLeft));
	}

	public void startTimer(){
		if(!mIsTimerRunning) {
			mStartTime = System.currentTimeMillis();
			mIsTimerRunning = true;
		} else
			Log.i("SERVICE", "Timer already started: ");
	}

	public boolean isTimerRunning(){
		return mIsTimerRunning;
	}

	public int getSecondsLeft(){
		int secondsLeft = (int) (System.currentTimeMillis() - mStartTime) / 1000;
		return mEndTime - secondsLeft;
	}

	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	private Notification createNotification(int secondsLeft){
		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this);
		String contentText = "";
		Intent intent = new Intent(this, MainActivity.class);
		intent.addCategory(Intent.CATEGORY_LAUNCHER);
		intent.setAction(Intent.ACTION_MAIN);
		if(secondsLeft > 0) {
			contentText = App.getContext().getString(R.string.timer_start_notification, secondsLeft);
		} else{
			contentText = App.getContext().getString(R.string.timer_resultTitle);
			intent.putExtra(EXTRA_ALI_SERVICE_TIMER_ENDS, true);
		}
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			mBuilder.setSmallIcon(R.drawable.ic_notification);
			mBuilder.setLargeIcon(BitmapFactory.decodeResource(App.getContext().getResources(), R.mipmap.ic_launcher));
		} else{
			mBuilder.setSmallIcon(R.mipmap.ic_launcher);
		}
		mBuilder.setContentText(contentText).setContentTitle(App.getRes().getString(R.string.app_name));
		PendingIntent resultPendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		mBuilder.setContentIntent(resultPendingIntent);
		mBuilder.setAutoCancel(true);
		//noinspection deprecation
		mBuilder.setColor(App.getRes().getColor(R.color.colorPrimary));
		mBuilder.setPriority(NotificationCompat.PRIORITY_HIGH);
		mBuilder.setCategory(Notification.CATEGORY_EVENT);
		mBuilder.setVisibility(Notification.VISIBILITY_PUBLIC);
		return mBuilder.build();
	}


	public class AliPromoBinder extends Binder{
		public AliPromoService getService(){
			return AliPromoService.this;
		}
	}
}
