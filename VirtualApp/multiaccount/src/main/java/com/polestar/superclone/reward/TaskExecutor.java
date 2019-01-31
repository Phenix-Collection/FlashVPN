package com.polestar.superclone.reward;

import com.polestar.ad.adapters.FuseAdLoader;
import com.polestar.ad.adapters.IAdAdapter;
import com.polestar.ad.adapters.IAdLoadListener;
import com.polestar.superclone.utils.EventReporter;
import com.polestar.superclone.utils.MLogs;
import com.polestar.task.ADErrorCode;
import com.polestar.task.ITaskStatusListener;
import com.polestar.task.database.datamodels.RewardVideoTask;
import com.polestar.task.database.datamodels.ShareTask;
import com.polestar.task.network.datamodels.Task;

import android.app.Activity;
import android.support.annotation.Nullable;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by guojia on 2019/1/31.
 */

public class TaskExecutor {

    private Activity mActivity;
    private AppUser mAppUser;

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
        int status = mAppUser.checkTask(task);
        ITaskStatusListener listener = new TaskReportListener(ll);
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
                mAppUser.submitInviteCode(task, (String)args[0], listener);
                break;
        }
    }

    private class TaskReportListener implements ITaskStatusListener {
        private ITaskStatusListener mListener;

        public TaskReportListener(ITaskStatusListener listener) {
            mListener = listener;
        }

        @Override
        public void onTaskSuccess(long taskId, float payment, float balance) {
            if (payment > 0 ) {
                EventReporter.setUserProperty(EventReporter.PROP_REWARDED, EventReporter.REWARD_ACTIVE);
            }
            EventReporter.taskEvent(taskId, 0);
            if (mListener != null) {
                mListener.onTaskSuccess(taskId, payment, balance);
            }
        }

        @Override
        public void onTaskFail(long taskId, ADErrorCode code) {
            EventReporter.taskEvent(taskId, code.getErrCode());
            if (mListener != null) {
                mListener.onTaskFail(taskId, code);
            }
        }

        @Override
        public void onGetAllAvailableTasks(ArrayList<Task> tasks) {

        }

        @Override
        public void onGeneralError(ADErrorCode code) {
            EventReporter.taskEvent(-1, code.getErrCode());
            if (mListener != null) {
                mListener.onTaskFail(-1, code);
            }
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
                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        listener.onTaskFail(task.mId, new ADErrorCode(RewardErrorCode.TASK_AD_NO_FILL, ""));
                    }
                });
            }

            @Override
            public void onRewarded(IAdAdapter ad) {
                mAppUser.finishTask(task, listener);
            }
        });
    }
}
