package com.polestar.multiaccount.utils;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;

import java.util.List;

/**
 * Created by yxx on 2016/8/30.
 */
public class TaskManager {

    private static final int MAX_RECENT_TASKS = Integer.MAX_VALUE;

    public static boolean switchTaskByPakageName(Context context,String packageName){
        try{
            ActivityManager activityManager =  (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
            final List<ActivityManager.RecentTaskInfo> recentTasks =
                    activityManager.getRecentTasks(MAX_RECENT_TASKS, ActivityManager.RECENT_IGNORE_UNAVAILABLE);
            MLogs.e("target packageName = " + packageName);
            for(ActivityManager.RecentTaskInfo taskInfo : recentTasks){
                Intent intent = taskInfo.baseIntent;
                MLogs.e("task packageName = " + taskInfo.baseIntent.getExtras());
                MLogs.e("task packageName = " + taskInfo.baseIntent);
                if(taskInfo.baseIntent.getComponent().getPackageName().equals(packageName)){
                    activityManager.moveTaskToFront(taskInfo.persistentId, ActivityManager.MOVE_TASK_WITH_HOME);
                    return true;
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }

    public static boolean isTaskExist(ActivityManager activityManager,int taskId){
        final List<ActivityManager.RecentTaskInfo> recentTasks =
                activityManager.getRecentTasks(MAX_RECENT_TASKS, ActivityManager.RECENT_IGNORE_UNAVAILABLE);
        for(ActivityManager.RecentTaskInfo taskInfo : recentTasks){
            if(taskInfo.id == taskId){
                return true;
            }
        }
        return false;
    }
}
