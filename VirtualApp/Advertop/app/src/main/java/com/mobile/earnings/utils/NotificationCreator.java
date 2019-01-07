package com.mobile.earnings.utils;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.os.Build;
import android.support.v4.app.NotificationCompat;

import com.mobile.earnings.App;
import com.mobile.earnings.R;
import com.mobile.earnings.splash.SplashScreenActivity;

import static com.mobile.earnings.utils.Constantaz.EXTRA_OPEN_ACTIVE;
import static com.mobile.earnings.utils.Constantaz.EXTRA_OPEN_DETAILED;
import static com.mobile.earnings.utils.Constantaz.PUSH_CUSTOM;
import static com.mobile.earnings.utils.Constantaz.PUSH_DESTROYER;
import static com.mobile.earnings.utils.Constantaz.PUSH_NEW_TASK_BUNDLE_TYPE;
import static com.mobile.earnings.utils.Constantaz.PUSH_NEW_TASK_REMINDER_TYPE;
import static com.mobile.earnings.utils.Constantaz.PUSH_NEW_TASK_TYPE;
import static com.mobile.earnings.utils.Constantaz.PUSH_REVIEW_TASK_READY;

public class NotificationCreator{

	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	public static void createAndNotifyNotification(Context context, String taskType, int appId, String message){
		NotificationManager mNotifyMgr = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		if(taskType.contentEquals(PUSH_DESTROYER)){
			mNotifyMgr.cancel(appId);
			return;
		}
		Intent intent = new Intent(context, SplashScreenActivity.class);
		String contentText = "";
		switch(taskType){
			case PUSH_NEW_TASK_REMINDER_TYPE:
				intent.putExtra(EXTRA_OPEN_ACTIVE, true);
				contentText = App.getRes().getString(R.string.notificationReminderMessage);
				break;
			case PUSH_NEW_TASK_TYPE:
				intent.putExtra(EXTRA_OPEN_DETAILED, appId);
				contentText = App.getRes().getString(R.string.notificationNewTaskMessage);
				break;
			case PUSH_NEW_TASK_BUNDLE_TYPE:
				contentText = App.getRes().getString(R.string.notificationNewTasksMessage);
				break;
			case PUSH_CUSTOM:
				contentText = message;
				break;
			case PUSH_REVIEW_TASK_READY:
				contentText = App.getRes().getString(R.string.push_content_text_review_task_available);
				intent.putExtra(EXTRA_OPEN_DETAILED, appId);
				break;
			default:
				break;
		}
		PendingIntent resultPendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_ONE_SHOT);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context);
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			mBuilder.setSmallIcon(R.drawable.ic_notification);
			mBuilder.setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher));
		} else{
			mBuilder.setSmallIcon(R.mipmap.ic_launcher);
		}
		mBuilder.setContentTitle(App.getRes().getString(R.string.app_name));
		mBuilder.setContentText(contentText);
		mBuilder.setContentIntent(resultPendingIntent);
		mBuilder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
		mBuilder.setAutoCancel(true);
		//noinspection deprecation
		mBuilder.setColor(App.getRes().getColor(R.color.colorPrimary));
		mBuilder.setPriority(NotificationCompat.PRIORITY_HIGH);
		mBuilder.setCategory(Notification.CATEGORY_EVENT);
		mBuilder.setVisibility(Notification.VISIBILITY_PUBLIC);
		mNotifyMgr.notify(appId, mBuilder.build());
	}
}
