package com.polestar.superclone.reward;

import com.polestar.superclone.MApp;
import com.polestar.task.ADErrorCode;
import com.polestar.task.ITaskStatusListener;
import com.polestar.task.database.DatabaseApi;
import com.polestar.task.database.DatabaseFileImpl;
import com.polestar.task.database.DatabaseImplFactory;
import com.polestar.task.database.datamodels.CheckInTask;
import com.polestar.task.database.datamodels.RewardVideoTask;
import com.polestar.task.database.datamodels.ShareTask;
import com.polestar.task.network.AdApiHelper;
import com.polestar.task.network.datamodels.Task;
import com.polestar.task.network.datamodels.User;
import com.polestar.task.network.datamodels.UserTask;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by guojia on 2019/1/24.
 */

public class AppUser {

    private String mInviteCode;
    private float mBalance;
    private String mId;
    private static AppUser sAppUser = null;
    private DatabaseApi databaseApi;

    private final static long UPDATE_INTERVAL = 3600*1000;
    private AppUser() {
        init();
    }

    private void init() {
        databaseApi = DatabaseImplFactory.getDatabaseApi(MApp.getApp());
        User userInfo = databaseApi.getMyUserInfo();
        if (userInfo != null) {
            mBalance = userInfo.mBalance;
            mId = userInfo.mDeviceId;
            mInviteCode = userInfo.mReferralCode;
        }
//        if (needUpdate()) {
//            AdApiHelper.getAvailableTasks(new ITaskStatusListener() {
//                @Override
//                public void onTaskSuccess(long taskId, float payment, float balance) {
//
//                }
//
//                @Override
//                public void onTaskFail(long taskId, ADErrorCode code) {
//
//                }
//
//                @Override
//                public void onGetAllAvailableTasks(ArrayList<Task> tasks) {
//                    databaseApi.setActiveTasks(tasks);
//                }
//
//                @Override
//                public void onGeneralError(ADErrorCode code) {
//
//                }
//            });
//
//
//        }
        scheduleUpdateTasks();
    }

    private void scheduleUpdateTasks() {

    }

    synchronized public static AppUser getInstance() {
        if (sAppUser == null) {
            sAppUser = new AppUser();
            sAppUser.init();
        }
        return sAppUser;
    }

    public String getInviteCode( ) {
        return mInviteCode;
    }

    public float getMyBalance() {
        return mBalance;
    }

    public String getMyId() {
        //0. preference
        //1. google aid
        //2. device id
        //3. android id
        //4. Random UUID
        return mId;
    }

    public ShareTask getInviteTask() {
        List<Task> list = databaseApi.getActiveTasksByType(Task.TASK_TYPE_SHARE_TASK);
        return list!= null && list.size() > 0 ? (ShareTask) list.get(0):null;
    }

    public CheckInTask getCheckInTask() {
        List<Task> list = databaseApi.getActiveTasksByType(Task.TASK_TYPE_CHECKIN_TASK);
        return list!= null && list.size() > 0 ? (CheckInTask)list.get(0):null;
    }

    public RewardVideoTask getVideoTask() {
        List<Task> list = databaseApi.getActiveTasksByType(Task.TASK_TYPE_REWARDVIDEO_TASK);
        return list!= null && list.size() > 0 ? (RewardVideoTask) list.get(0):null;
    }

//    public  getVideoTask() {
//        List<Task> list = databaseApi.getActiveTasksByType(Task.TASK_TYPE_REWARDVIDEO_TASK);
//        return list!= null && list.size() > 0 ? (RewardVideoTask) list.get(0):null;
//    }

    public void register() {

    }

    public void checkIn() {

    }

    public boolean hasCheckinToday() {
        return  false;
    }

    public void submitInviteCode() {
        //AdApiHelper.finishTask(getMyId(), );
    }
}
