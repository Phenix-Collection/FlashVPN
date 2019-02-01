package com.polestar.superclone.reward;

import com.polestar.ad.adapters.FuseAdLoader;
import com.polestar.ad.adapters.IAdAdapter;
import com.polestar.ad.adapters.IAdLoadListener;
import com.polestar.superclone.utils.EventReporter;
import com.polestar.superclone.utils.MLogs;
import com.polestar.task.ADErrorCode;
import com.polestar.task.ITaskStatusListener;
import com.polestar.task.database.datamodels.ReferTask;
import com.polestar.task.database.datamodels.RewardVideoTask;
import com.polestar.task.database.datamodels.ShareTask;
import com.polestar.task.network.AdApiHelper;
import com.polestar.task.network.datamodels.Task;

import android.app.Activity;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static com.polestar.task.ADErrorCode.ERR_SERVER_DOWN_CODE;

/**
 * Created by guojia on 2019/1/31.
 */

public class TaskExecutor {

    private Activity mActivity;
    private AppUser mAppUser;
    private static final long TASK_EXECUTING_TIMEOUT = 3*1000;

    public TaskExecutor(Activity activity) {
        mActivity = activity;
        mAppUser = AppUser.getInstance();
    }

    /**
     * Not all task will have listener callback, e.g. invite to start another activity
     * @param task
     * @param ll
     */
    public void execute(Task task, @Nullable ITaskStatusListener ll, Object... args) {
        int status = checkTask(task);
        ITaskStatusListener listener = new WrapTaskStatusListener(ll);
        if (status != RewardErrorCode.TASK_OK) {
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    listener.onTaskFail(task.mId, new ADErrorCode(status, ""));
                }
            });
            return;
        }
        switch (task.mTaskType) {
            case Task.TASK_TYPE_SHARE_TASK:
                if (mActivity instanceof  InviteActivity) {
                    MLogs.d("Already in invite activity");
                    return;
                } else {
                    InviteActivity.start(mActivity);
                }
                break;
            case Task.TASK_TYPE_CHECKIN_TASK:
                mAppUser.finishTask(task, listener);
                break;
            case Task.TASK_TYPE_REWARDVIDEO_TASK:
                executeVideoTask((RewardVideoTask) task, listener);
                break;
            case Task.TASK_TYPE_REFER_TASK:
                String code = (String)args[0];
                if (task == null || TextUtils.isEmpty(code)){
                    return;
                }
                if (code.equals(mAppUser.getInviteCode())) {
                    listener.onTaskFail(task.mId, new ADErrorCode(ADErrorCode.INVALID_REFERRAL_CODE, ""));
                    return;
                }
                mAppUser.submitInviteCode(task, (String)args[0], listener);
                break;
        }
    }

    public void submitInviteCode(Task task, String code, ITaskStatusListener listener) {
        execute(task, listener, code);
    }

    public static int checkTask(Task task) {
        if (task == null) {
            return RewardErrorCode.TASK_UNEXPECTED_ERROR;
        }
        if (task instanceof ReferTask) {
            if (!TextUtils.isEmpty(TaskPreference.getReferredBy() )){
                return RewardErrorCode.TASK_CODE_ALREADY_SUBMITTED;
            }
        }
        if (TaskPreference.getTaskFinishTodayCount(task.mId) >= task.mLimitPerDay
                || TaskPreference.getTaskFinishCount(task.mId) >= task.mLimitTotal) {
            return RewardErrorCode.TASK_EXCEED_DAY_LIMIT;
        }
        return RewardErrorCode.TASK_OK;
    }

    private class WrapTaskStatusListener implements ITaskStatusListener{
        private ITaskStatusListener mListener;
        public WrapTaskStatusListener (ITaskStatusListener listener) {
            mListener = listener;

        }

        @Override
        public void onTaskSuccess(long taskId, float payment, float balance) {
            MLogs.d("onTaskSuccess " + payment);
            mAppUser.updateMyBalance(balance);
            TaskPreference.incTaskFinishCount(taskId);
            if (payment > 0 ) {
                EventReporter.setUserProperty(EventReporter.PROP_REWARDED, EventReporter.REWARD_ACTIVE);
            }
            EventReporter.taskEvent(taskId, 0);
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mListener != null) {
                        mListener.onTaskSuccess(taskId, payment, balance);
                    }
                }
            });
        }

        @Override
        public void onTaskFail(long taskId, ADErrorCode code) {
            EventReporter.taskEvent(taskId, code.getErrCode());
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mListener != null) {
                        mListener.onTaskFail(taskId, code);
                    }
                }
            });
        }

        @Override
        public void onGetAllAvailableTasks(ArrayList<Task> tasks) {
            MLogs.d("onGetAllAvailableTasks should not be here!!");
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mListener.onGetAllAvailableTasks(tasks);
                }
            });
        }

        @Override
        public void onGeneralError(ADErrorCode code) {
            EventReporter.taskEvent(-1, code.getErrCode());
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mListener != null) {
                        mListener.onGeneralError(code);
                    }
                }
            });
        }
    }

    private void executeVideoTask(RewardVideoTask task, ITaskStatusListener listener) {
        FuseAdLoader loader = FuseAdLoader.get(task.adSlot, mActivity);
        if (loader == null) {
            MLogs.d("Wrong adSlot config in task " + task.toString());
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    listener.onTaskFail(task.mId, new ADErrorCode(RewardErrorCode.TASK_UNEXPECTED_ERROR, ""));
                }
            });
            return;
        }
        loader.loadAd(mActivity, 2, new IAdLoadListener() {
            @Override
            public void onAdLoaded(IAdAdapter ad) {
                ad.show();
            }

            @Override
            public void onAdClicked(IAdAdapter ad) {

            }

            @Override
            public void onAdClosed(IAdAdapter ad) {
                loader.preloadAd(mActivity);
            }

            @Override
            public void onAdListLoaded(List<IAdAdapter> ads) {

            }

            @Override
            public void onError(String error) {
                listener.onTaskFail(task.mId, new ADErrorCode(RewardErrorCode.TASK_AD_NO_FILL, ""));
            }

            @Override
            public void onRewarded(IAdAdapter ad) {
                mAppUser.finishTask(task, listener);
            }
        });
    }
}
