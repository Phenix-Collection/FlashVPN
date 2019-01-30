package com.polestar.superclone.reward;

import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import com.polestar.ad.AdLog;
import com.polestar.ad.AdUtils;
import com.polestar.ad.adapters.FuseAdLoader;
import com.polestar.superclone.MApp;
import com.polestar.superclone.utils.MLogs;
import com.polestar.superclone.utils.RemoteConfig;
import com.polestar.task.ADErrorCode;
import com.polestar.task.IProductStatusListener;
import com.polestar.task.ITaskStatusListener;
import com.polestar.task.database.DatabaseApi;
import com.polestar.task.database.DatabaseImplFactory;
import com.polestar.task.database.datamodels.CheckInTask;
import com.polestar.task.database.datamodels.ReferTask;
import com.polestar.task.database.datamodels.RewardVideoTask;
import com.polestar.task.database.datamodels.ShareTask;
import com.polestar.task.network.AdApiHelper;
import com.polestar.task.network.datamodels.Product;
import com.polestar.task.network.datamodels.Task;
import com.polestar.task.network.datamodels.User;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import static com.polestar.task.ADErrorCode.ERR_SERVER_DOWN_CODE;

/**
 * Created by guojia on 2019/1/24.
 */

public class AppUser {

    private String mInviteCode;
    private float mBalance;
    private String mId;
    private static AppUser sAppUser = null;
    private DatabaseApi databaseApi;
    private final static String TAG = "AppUser";
    private static final String CONF_REWARD_ENABLE = "conf_reward_open";
    private Handler mainHandler;
    private HashSet<IUserUpdateListener> mObservers;
    private static final long TASK_EXECUTING_TIMEOUT = 3*1000;

    private AppUser() {
        databaseApi = DatabaseImplFactory.getDatabaseApi(MApp.getApp());
        mainHandler = new Handler(Looper.getMainLooper());
        mObservers = new HashSet<>();
        mId = getMyId();
        initData();
        RewardInfoFetcher.get(MApp.getApp()).registerUpdateObserver(new RewardInfoFetcher.IRewardInfoFetchListener() {
            @Override
            public void onFetched() {
                MLogs.d(TAG, "onFetched");
                initData();
                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        for (IUserUpdateListener listener: mObservers) {
                            listener.onUserDataUpdated();
                        }
                    }
                });
            }
        });
        RewardInfoFetcher.get(MApp.getApp()).preloadRewardInfo();
//        preloadRewardVideoTask();
    }

    //posted on main thread;
    public interface IUserUpdateListener {
        void onUserDataUpdated();
    }

    public void forceRefreshData() {
        RewardInfoFetcher.get(MApp.getApp()).forceRefresh();
    }

    public void listenOnUserUpdate(IUserUpdateListener listener) {
        mObservers.remove(listener);
    }

    public void stopListenOnUserUpdate(IUserUpdateListener listener) {
        mObservers.add(listener);
    }

    public static boolean isRewardEnabled() {
        return RemoteConfig.getBoolean(CONF_REWARD_ENABLE);
    }

    public boolean isRewardAvailable() {
        //config is open && reward data available
        return isRewardEnabled()
                && databaseApi!=null && databaseApi.isDataAvailable();
    }

    private void initData() {
        MLogs.d(TAG, "My user id: " + mId);
        User userInfo = databaseApi.getMyUserInfo();
        if (userInfo != null) {
            mBalance = userInfo.mBalance;
            mInviteCode = userInfo.mReferralCode;
        }
    }


    synchronized public static AppUser getInstance() {
        if (sAppUser == null) {
            sAppUser = new AppUser();
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
        String id = TaskPreference.getMyId();
        if (!TextUtils.isEmpty(id)) {
            return id;
        } else {
            id = AdUtils.getGoogleAdvertisingId(MApp.getApp());
        }
        if (TextUtils.isEmpty(id)) {
            id = AdUtils.getDeviceID(MApp.getApp());
        }
        if (TextUtils.isEmpty(id)) {
            id = AdUtils.getAndroidID(MApp.getApp());
        }
        if (TextUtils.isEmpty(id)) {
            id = UUID.randomUUID().toString();
        }
        TaskPreference.setMyId(id);
        return id;
    }

    public ShareTask getInviteTask() {
        List<Task> list = databaseApi.getActiveTasksByType(Task.TASK_TYPE_SHARE_TASK);
        return list!= null && list.size() > 0 ? list.get(0).getShareTask():null;
    }

    public CheckInTask getCheckInTask() {
        List<Task> list = databaseApi.getActiveTasksByType(Task.TASK_TYPE_CHECKIN_TASK);
        return list!= null && list.size() > 0 ? list.get(0).getCheckInTask():null;
    }

    public RewardVideoTask getVideoTask() {
        List<Task> list = databaseApi.getActiveTasksByType(Task.TASK_TYPE_REWARDVIDEO_TASK);
        return list!= null && list.size() > 0 ? list.get(0).getRewardVideoTask():null;
    }

    public ReferTask getReferTask() {
        List<Task> list = databaseApi.getActiveTasksByType(Task.TASK_TYPE_REFER_TASK);
        return list!= null && list.size() > 0 ? list.get(0).getReferTask():null;
    }

    public List<Product> getProducts() {
        return databaseApi.getAllProductInfo();
    }

//    public  getVideoTask() {
//        List<Task> list = databaseApi.getActiveTasksByType(Task.TASK_TYPE_REWARDVIDEO_TASK);
//        return list!= null && list.size() > 0 ? (RewardVideoTask) list.get(0):null;
//    }

    public int checkProduct(Product product) {
        return checkProduct(product, 1);
    }

    public int checkProduct(Product product, int amount){
        return RewardErrorCode.PRODUCT_OK;
    }

    public void consumeProduct(long productId, int amount, String email, String info, IProductStatusListener listener) {
        AdApiHelper.consumeProduct(getMyId(), productId, amount, email, info, new WrapProductStatusListener(listener));
    }

    public void finishTask(Task task, ITaskStatusListener listener) {
        if (task == null) return;
        AdApiHelper.finishTask(mId, task.mId, null, new WrapTaskStatusListener(listener));
    }

    public void submitInviteCode(Task task, String code, ITaskStatusListener listener) {
        if (task == null || TextUtils.isEmpty(code)) return;
        AdApiHelper.finishTask(mId, task.mId, code, new WrapTaskStatusListener(listener));
    }

    public int checkTask(Task task) {
        if (task == null) {
            return RewardErrorCode.TASK_UNEXPECTED_ERROR;
        }
        if (TaskPreference.getTaskFinishTodayCount(task.mId) >= task.mLimitPerDay
                || TaskPreference.getTaskFinishCount(task.mId) >= task.mLimitTotal) {
            return RewardErrorCode.TASK_EXCEED_DAY_LIMIT;
        }
        return RewardErrorCode.TASK_OK;
    }

    public Task getTaskById(long taskId) {
        return databaseApi.getTaskById(taskId);
    }

    public void preloadRewardVideoTask() {
        if (databaseApi.isDataAvailable()) {
            RewardVideoTask task = getVideoTask();
            if (task != null && checkTask(task) == RewardErrorCode.TASK_OK) {
                FuseAdLoader adLoader = FuseAdLoader.get(task.adSlot, MApp.getApp());
                if (adLoader != null) {
                    adLoader.preloadAd(MApp.getApp());
                }
            }
        }
    }

    private class WrapProductStatusListener implements IProductStatusListener {
        private IProductStatusListener mListener ;

        public WrapProductStatusListener(IProductStatusListener listener) {
            mListener = listener;
        }

        @Override
        public void onConsumeSuccess(long id, int amount, float totalCost, float balance) {
            mBalance = balance;
            //TODO 发货
            mainHandler.post(new Runnable() {
                @Override
                public void run() {
                    mListener.onConsumeSuccess(id, amount, totalCost, balance);
                }
            });
        }

        @Override
        public void onConsumeFail(ADErrorCode code) {
            mainHandler.post(new Runnable() {
                @Override
                public void run() {
                    mListener.onConsumeFail(code);
                }
            });
        }

        @Override
        public void onGetAllAvailableProducts(ArrayList<Product> products) {
            mainHandler.post(new Runnable() {
                @Override
                public void run() {
                    mListener.onGetAllAvailableProducts(products);
                }
            });
        }

        @Override
        public void onGeneralError(ADErrorCode code) {
            mainHandler.post(new Runnable() {
                @Override
                public void run() {
                    mListener.onGeneralError(code);
                }
            });
        }
    }

    private class WrapTaskStatusListener implements ITaskStatusListener{
        private ITaskStatusListener mListener;
        private Timer mTimer;
        public WrapTaskStatusListener (ITaskStatusListener listener) {
            mListener = listener;
            mTimer = new Timer("task_timer");
            mTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    onGeneralError(new ADErrorCode(ERR_SERVER_DOWN_CODE, "task timeout"));
                    mListener = null;
                }
            }, TASK_EXECUTING_TIMEOUT);
        }

        @Override
        public void onTaskSuccess(long taskId, float payment, float balance) {
            mTimer.cancel();
            mBalance = balance;
            TaskPreference.incTaskFinishCount(taskId);
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
        public void onTaskFail(long taskId, ADErrorCode code) {
            mTimer.cancel();
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
        public void onGetAllAvailableTasks(ArrayList<Task> tasks) {
            MLogs.d("onGetAllAvailableTasks should not be here!!");
            mTimer.cancel();
            mainHandler.post(new Runnable() {
                @Override
                public void run() {
                    mListener.onGetAllAvailableTasks(tasks);
                }
            });
        }

        @Override
        public void onGeneralError(ADErrorCode code) {
            mTimer.cancel();
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
}
