package winterfell.flash.vpn.reward;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.polestar.ad.adapters.FuseAdLoader;
import com.polestar.ad.adapters.IAdAdapter;
import com.polestar.ad.adapters.IAdLoadListener;
import com.polestar.task.ADErrorCode;
import com.polestar.task.ITaskStatusListener;
import com.polestar.task.database.datamodels.AdTask;
import com.polestar.task.database.datamodels.ReferTask;
import com.polestar.task.database.datamodels.RewardVideoTask;
import com.polestar.task.network.datamodels.Task;

import java.util.ArrayList;
import java.util.List;

import winterfell.flash.vpn.FlashUser;
import winterfell.flash.vpn.utils.EventReporter;
import winterfell.flash.vpn.utils.MLogs;

/**
 * Created by guojia on 2019/3/21.
 */


public class TaskExecutor {

    private Context mContext;
    private AppUser mAppUser;
    public static final int AD_TASK_ON_CLICK = 0;
    public static final int AD_TASK_ON_INSTALL = 1;
    private static final long TASK_EXECUTING_TIMEOUT = 3*1000;
    private Handler mainHandler;

    public TaskExecutor(Context context) {
        mContext = context;
        mAppUser = FlashUser.getInstance();
        mainHandler = new Handler(Looper.getMainLooper());
    }

    /**
     * Not all task will have listener callback, e.g. invite to start another activity
     * @param task
     * @param ll
     */
    public void execute(final Task task, @Nullable ITaskStatusListener ll, Object... args) {
        final int status = checkTask(task);
        final ITaskStatusListener listener = new WrapTaskStatusListener(ll);
        if (status != RewardErrorCode.TASK_OK) {
            mainHandler.post(new Runnable() {
                @Override
                public void run() {
                    listener.onTaskFail(task.mId, new ADErrorCode(status, ""));
                }
            });
            return;
        }
        switch (task.mTaskType) {
            case Task.TASK_TYPE_SHARE_TASK:
//                if (mContext instanceof  InviteActivity) {
//                    MLogs.d("Already in invite activity");
//                    return;
//                } else {
//                    if (mContext instanceof Activity) {
//                        InviteActivity.start((Activity)mContext);
//                    }
//                }
                break;
            case Task.TASK_TYPE_CHECKIN_TASK:
            case Task.TASK_TYPE_RANDOM_AWARD:
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
            case Task.TASK_TYPE_AD_TASK:
                int state = (int) args[0];
                AdTask adTask = task.getAdTask();
                MLogs.d("TASK_TYPE_AD_TASK + state: " + state);
                if (state == AD_TASK_ON_CLICK) {
                    if (adTask.isCpc()) {
                        mAppUser.finishTask(task, listener);
                    } else if (adTask.isCpi()) {
                        TaskPreference.setPendingTask(adTask.mId);
                    }
                } else if (state == AD_TASK_ON_INSTALL) {
                    if (adTask.isCpi()) {
                        mAppUser.finishTask(task, listener);
                        TaskPreference.clearPendingTask(adTask.mId);
                    }
                }
                break;
        }
    }

    public class DefaultAdTaskListener implements ITaskStatusListener {
        @Override
        public void onGeneralError(ADErrorCode code) {

        }

        @Override
        public void onTaskSuccess(long taskId, float payment, float balance) {
            RewardErrorCode.toastMessage(mContext, RewardErrorCode.TASK_OK, payment);
        }

        @Override
        public void onTaskFail(long taskId, ADErrorCode code) {

        }

        @Override
        public void onGetAllAvailableTasks(ArrayList<Task> tasks) {

        }
    }

    public void clickAdTask(AdTask task, @Nullable  ITaskStatusListener listener) {
        if (listener == null) {
            listener = new DefaultAdTaskListener();
        }
        execute(task, listener, AD_TASK_ON_CLICK);
    }

    public void installAdTask(AdTask task,@Nullable  ITaskStatusListener listener) {
        if (listener == null) {
            listener = new DefaultAdTaskListener();
        }
        MLogs.d("installAdTask " + task.pkg);
        execute(task, listener, AD_TASK_ON_INSTALL);
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
        public void onTaskSuccess(final long taskId, final float payment, final float balance) {
            MLogs.d("onTaskSuccess " + payment);
            mAppUser.updateMyBalance(balance);
            TaskPreference.incTaskFinishCount(taskId);
            if (payment > 0 ) {
                EventReporter.setUserProperty(EventReporter.PROP_REWARDED, EventReporter.REWARD_ACTIVE);
            }
            EventReporter.taskEvent(taskId, 0);
            mainHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (mListener != null) {
                        mListener.onTaskSuccess(taskId, payment, balance);
                    }
                }
            });
        }

        @Override
        public void onTaskFail(final long taskId, final ADErrorCode code) {
            MLogs.d("onTaskFail " + taskId + " code: " + code);
            EventReporter.taskEvent(taskId, code.getErrCode());
            mainHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (mListener != null) {
                        mListener.onTaskFail(taskId, code);
                    }
                }
            });
        }

        @Override
        public void onGetAllAvailableTasks(final ArrayList<Task> tasks) {
            MLogs.d("onGetAllAvailableTasks should not be here!!");
            mainHandler.post(new Runnable() {
                @Override
                public void run() {
                    mListener.onGetAllAvailableTasks(tasks);
                }
            });
        }

        @Override
        public void onGeneralError(final ADErrorCode code) {
            EventReporter.taskEvent(-1, code.getErrCode());
            mainHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (mListener != null) {
                        mListener.onGeneralError(code);
                    }
                }
            });
        }
    }

    private void executeVideoTask(final RewardVideoTask task, final ITaskStatusListener listener) {
        final FuseAdLoader loader = FuseAdLoader.get(task.adSlot, mContext);
        if (loader == null) {
            MLogs.d("Wrong adSlot config in task " + task.toString());
            mainHandler.post(new Runnable() {
                @Override
                public void run() {
                    listener.onTaskFail(task.mId, new ADErrorCode(RewardErrorCode.TASK_UNEXPECTED_ERROR, ""));
                }
            });
            return;
        }
        loader.loadAd(mContext, 2, new IAdLoadListener() {
            @Override
            public void onAdLoaded(IAdAdapter ad) {
                ad.show();
            }

            @Override
            public void onAdClicked(IAdAdapter ad) {

            }

            @Override
            public void onAdClosed(IAdAdapter ad) {
                loader.preloadAd(mContext);
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
