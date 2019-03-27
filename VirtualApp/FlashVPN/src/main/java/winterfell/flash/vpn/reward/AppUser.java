package winterfell.flash.vpn.reward;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import com.polestar.ad.AdUtils;
import com.polestar.ad.adapters.FuseAdLoader;
import com.polestar.ad.adapters.IAdAdapter;
import com.polestar.ad.adapters.IAdLoadListener;
import com.polestar.task.ADErrorCode;
import com.polestar.task.IAdTaskStateObserver;
import com.polestar.task.IProductStatusListener;
import com.polestar.task.ITaskStatusListener;
import com.polestar.task.IUserStatusListener;
import com.polestar.task.database.DatabaseApi;
import com.polestar.task.database.DatabaseImplFactory;
import com.polestar.task.database.datamodels.AdTask;
import com.polestar.task.database.datamodels.CheckInTask;
import com.polestar.task.database.datamodels.RandomAwardTask;
import com.polestar.task.database.datamodels.ReferTask;
import com.polestar.task.database.datamodels.RewardVideoTask;
import com.polestar.task.database.datamodels.ShareTask;

import winterfell.flash.vpn.BuildConfig;
import winterfell.flash.vpn.FlashUser;

import com.polestar.task.network.AdApiHelper;
import com.polestar.task.network.Configuration;
import com.polestar.task.network.datamodels.Product;
import com.polestar.task.network.datamodels.Task;
import com.polestar.task.network.datamodels.User;
import com.witter.msg.Sender;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import winterfell.flash.vpn.FlashApp;
import winterfell.flash.vpn.utils.EventReporter;
import winterfell.flash.vpn.utils.MD5Utils;
import winterfell.flash.vpn.utils.MLogs;
import winterfell.flash.vpn.utils.RemoteConfig;

/**
 * Created by guojia on 2019/1/24.
 * Reward Basic info
 */

abstract public class AppUser implements IAdTaskStateObserver {

    private String mInviteCode;
    private float mBalance;
    private String mId;
    private DatabaseApi databaseApi;
    private final static String TAG = "AppUser";
    private static final String CONF_REWARD_ENABLE = "conf_reward_open";
    private Handler mainHandler;
    private HashSet<IUserUpdateListener> mObservers;
    private static boolean isSecure;

    protected AppUser() {
        check();
        mId = getMyId();
        Configuration.URL_PREFIX = RemoteConfig.getString("config_task_server");
        Configuration.APP_VERSION_CODE = BuildConfig.VERSION_CODE;
        Configuration.PKG_NAME = BuildConfig.APPLICATION_ID;
        FuseAdLoader.setUserId(mId);

        databaseApi = DatabaseImplFactory.getDatabaseApi(FlashApp.getApp());
        mainHandler = new Handler(Looper.getMainLooper());
        mObservers = new HashSet<>();

        initData();
        RewardInfoFetcher.get(FlashApp.getApp()).registerUpdateObserver(new RewardInfoFetcher.IRewardInfoFetchListener() {
            @Override
            public void onFetched() {
                MLogs.d(TAG, "onFetched");
                initData();
                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        for (IUserUpdateListener listener : mObservers) {
                            listener.onUserDataUpdated();
                        }
                    }
                });
            }
        });
        RewardInfoFetcher.get(FlashApp.getApp()).preloadRewardInfo();
        FuseAdLoader.registerAdTaskStateObserver(this);
//        preloadRewardVideoTask();
    }


    public void updateSubscribe(int status){
        AdApiHelper.register(FlashApp.getApp(), getMyId(), new IUserStatusListener() {
            @Override
            public void onRegisterSuccess(User user) {
                for (IUserUpdateListener listener: mObservers) {
                    listener.onUserDataUpdated();
                }
            }

            @Override
            public void onRegisterFailed(ADErrorCode errorCode) {

            }

            @Override
            public void onGeneralError(ADErrorCode code) {

            }
        }, true, status);
    }

    public static boolean check() {
        isSecure = checkInternal();
        return isSecure;
    }

    private static boolean checkInternal() {
//        MLogs.d("JJJJJ","" +AdCipher.getCertificateHashCode(FlashApp.getApp()));
        if (Sender.check(FlashApp.getApp()) != 1) {
            MLogs.d("Check fail 1");
            EventReporter.rewardEvent("check_1");
            return false;
        }
        PackageManager pm = FlashApp.getApp().getPackageManager();
        String fakeClass = null;
        try {
            pm.getApplicationInfo("a.b", 0);
            EventReporter.rewardEvent("check_2");
            return false;
        } catch (Exception ex) {
            for (StackTraceElement element : ex.getStackTrace()) {
                String lower = element.getClassName().toLowerCase();
                if (lower.contains("vclient")
                        || lower.contains("hook")
                        || lower.contains("lody")
                        || lower.contains("lbe")
                        || lower.contains("mochat")
                        || lower.contains("dual")
                        || lower.contains("xpose")) {
                    fakeClass = lower;
                    break;
                }
                if (lower.startsWith("android.")
                        || lower.startsWith("com.polestar.")
                        || lower.startsWith("com.android.")
                        || lower.startsWith("dalvik.system.")
                        || lower.startsWith("java.")) {
                    continue;
                }
                fakeClass = lower;
                break;

            }
        }
        if (fakeClass != null) {
            EventReporter.rewardEvent("check_" + fakeClass);
            return false;
        } else {
            MLogs.d("Check OK");
            return true;
        }
    }

    //posted on main thread;
    public interface IUserUpdateListener {
        void onUserDataUpdated();
        void onVideoTaskAvailable();
    }

    public void forceRefreshData() {
        RewardInfoFetcher.get(FlashApp.getApp()).forceRefresh();
    }

    public void listenOnUserUpdate(IUserUpdateListener listener) {
        mObservers.add(listener);
    }

    public void stopListenOnUserUpdate(IUserUpdateListener listener) {
        mObservers.remove(listener);
    }

    public static boolean isRewardEnabled() {
        return isSecure && RemoteConfig.getBoolean(CONF_REWARD_ENABLE);
    }

    public boolean isRewardAvailable() {
        //config is open && reward data available
        return isRewardEnabled()
                && databaseApi != null && databaseApi.isDataAvailable();
    }

    private void initData() {
        MLogs.d(TAG, "My user id: " + mId);
        User userInfo = databaseApi.getMyUserInfo();
        if (userInfo != null) {
            mBalance = userInfo.mBalance;
            mInviteCode = userInfo.mReferralCode;
        }
    }


//    synchronized public static AppUser getInstance() {
//        if (sAppUser == null) {
//            sAppUser = new AppUser();
//        }
//        return sAppUser;
//    }

    public String getInviteCode() {
        return mInviteCode;
    }

    public float getMyBalance() {
        return mBalance;
    }

    public void updateMyBalance(float balance) {
        mBalance = balance;
    }

    public String getMyId() {
        //0. preference
        //1. google aid
        //2. device id
        //3. android id
        //4. Random UUID
        if (!TextUtils.isEmpty(mId)) {
            return mId;
        }
        String id = TaskPreference.getMyId();
        if (!TextUtils.isEmpty(id)) {
            return id;
        }

        if (TextUtils.isEmpty(id)) {
            id = AdUtils.getDeviceID(FlashApp.getApp());
        }
        if (TextUtils.isEmpty(id)) {
            id = AdUtils.getAndroidID(FlashApp.getApp());
        }
        if (TextUtils.isEmpty(id)) {
            id = UUID.randomUUID().toString();
        }
        id = MD5Utils.getStringMd5(id);
        TaskPreference.setMyId(id);
        return id;
    }

    public ShareTask getInviteTask() {
        List<Task> list = databaseApi.getActiveTasksByType(Task.TASK_TYPE_SHARE_TASK);
        return list != null && list.size() > 0 ? list.get(0).getShareTask() : null;
    }

    public CheckInTask getCheckInTask() {
        List<Task> list = databaseApi.getActiveTasksByType(Task.TASK_TYPE_CHECKIN_TASK);
        return list != null && list.size() > 0 ? list.get(0).getCheckInTask() : null;
    }

    public RewardVideoTask getVideoTask() {
        List<Task> list = databaseApi.getActiveTasksByType(Task.TASK_TYPE_REWARDVIDEO_TASK);
        return list != null && list.size() > 0 ? list.get(0).getRewardVideoTask() : null;
    }

    public ReferTask getReferTask() {
        List<Task> list = databaseApi.getActiveTasksByType(Task.TASK_TYPE_REFER_TASK);
        return list != null && list.size() > 0 ? list.get(0).getReferTask() : null;
    }

    public RandomAwardTask getRandomAwardTask() {
        List<Task> list = databaseApi.getActiveTasksByType(Task.TASK_TYPE_RANDOM_AWARD);
        return list != null && list.size() > 0 ? list.get(0).getRandomAwardTask() : null;
    }

    public List<Product> getProducts() {
        return databaseApi.getAllProductInfo();
    }

//    public  getVideoTask() {
//        List<Task> list = databaseApi.getActiveTasksByType(Task.TASK_TYPE_REWARDVIDEO_TASK);
//        return list!= null && list.size() > 0 ? (RewardVideoTask) list.get(0):null;
//    }

    public List<Task> getRecommendTasks() {
        ArrayList<Task> res = new ArrayList<>();
        res.add(getVideoTask());
        res.add(getInviteTask());
        return res;
//        List<Task> list;
//        list = databaseApi.get
//        list.add(databaseApi.)
    }

    public Product get1CloneProduct() {
        List<Product> list = databaseApi.getProductInfoByType(Product.PRODUCT_TYPE_1_CLONE);
        MLogs.d(TAG, "1clone " + list.size());
        return list != null && list.size() > 0 ? list.get(0) : null;
    }

    void buyProduct(long productId, int amount, String email, String info, IProductStatusListener listener) {
        AdApiHelper.consumeProduct(getMyId(), productId, amount, email, info, listener);
    }

    void finishTask(Task task, ITaskStatusListener listener) {
        AdApiHelper.finishTask(mId, task.mId, null, listener);
    }

    void submitInviteCode(Task task, String code, ITaskStatusListener listener) {
        AdApiHelper.finishTask(mId, task.mId, code, listener);
    }

    public int checkTask(Task task) {
        if (task == null) {
            return RewardErrorCode.TASK_UNEXPECTED_ERROR;
        }
        if (task instanceof ReferTask) {
            if (!TextUtils.isEmpty(TaskPreference.getReferredBy())) {
                return RewardErrorCode.TASK_CODE_ALREADY_SUBMITTED;
            }
        }
        if (TaskPreference.getTaskFinishTodayCount(task.mId) >= task.mLimitPerDay
                || TaskPreference.getTaskFinishCount(task.mId) >= task.mLimitTotal) {
            return RewardErrorCode.TASK_EXCEED_DAY_LIMIT;
        }
        return RewardErrorCode.TASK_OK;
    }

    public void preloadRewardVideoTask() {
        if (databaseApi.isDataAvailable()) {
            RewardVideoTask task = getVideoTask();
            if (task != null && TaskExecutor.checkTask(task) == RewardErrorCode.TASK_OK) {
                FuseAdLoader adLoader = FuseAdLoader.get(task.adSlot, FlashApp.getApp());
                if (adLoader != null) {
                    adLoader.loadAd(FlashApp.getApp(), 2, 1000, new IAdLoadListener() {
                        @Override
                        public void onAdLoaded(IAdAdapter ad) {
                            for (IUserUpdateListener listener: mObservers) {
                                listener.onVideoTaskAvailable();
                            }
                        }

                        @Override
                        public void onAdClicked(IAdAdapter ad) {

                        }

                        @Override
                        public void onAdClosed(IAdAdapter ad) {

                        }

                        @Override
                        public void onAdListLoaded(List<IAdAdapter> ads) {

                        }

                        @Override
                        public void onError(String error) {

                        }

                        @Override
                        public void onRewarded(IAdAdapter ad) {

                        }
                    });
                }
            }
        }
    }

    public boolean isRewardVideoTaskReady() {
        if (databaseApi.isDataAvailable()) {
            RewardVideoTask task = getVideoTask();
            if (task != null && TaskExecutor.checkTask(task) == RewardErrorCode.TASK_OK) {
                FuseAdLoader adLoader = FuseAdLoader.get(task.adSlot, FlashApp.getApp());
                if (adLoader != null) {
                    return adLoader.hasValidCache();
                }
            }
        }
        return false;
    }

    public void setReferrerCode(String code) {
        TaskPreference.setReferredBy(code);
    }

    public String getReferrerCode() {
        return TaskPreference.getReferredBy();
    }

    public boolean checkAdFree() {
        return ProductManager.getInstance().checkAndConsumeAdFreeTime();
    }

//    public boolean checkAndConsumeClone(int num) {
//        return ProductManager.getInstance().checkAndConsumeClone(num);
//    }

    public List<AdTask> getPendingAdTask() {
        List<AdTask> ret = new ArrayList<>();
        List<Task> tasks = databaseApi.getActiveTasksByType(Task.TASK_TYPE_AD_TASK);
        if (tasks != null && tasks.size() > 0) {
            for (Task task : tasks) {
                if (TaskPreference.isPendingTask(task.mId)) {
                    ret.add(task.getAdTask());
                }
            }
        }
        return ret;
    }

    public void updatePendingAdTask(Context context, String pkg) {
        AdTask task = getAdTaskByPkg(pkg);
        MLogs.d("get task for pkg " + pkg + " task:" + task);
        if (task != null && TaskPreference.isPendingTask(task.mId)) {
            FuseAdLoader.notifyAdTaskInstalled(task);
        }
    }

    public void updatePendingAdTask(final Context context) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                List<AdTask> adTaskList = getPendingAdTask();
                for (AdTask task : adTaskList) {
                    long createTime = TaskPreference.getPendingTaskCreateTime(task.mId);
                    //1 week tracking window
                    if (System.currentTimeMillis() - createTime > 7 * 24 * 3600 * 1000) {
                        TaskPreference.clearPendingTask(task.mId);
                    } else {
                        try {
                            PackageManager pm = context.getPackageManager();
                            ApplicationInfo ai = pm.getApplicationInfo(task.pkg, 0);
                            if (ai != null) {
                                FuseAdLoader.notifyAdTaskInstalled(task);
                            }
                        } catch (Exception ex) {

                        }
                    }
                }
            }
        }).start();
    }

    public AdTask getAdTaskByPkg(String pkg) {
        List<Task> tasks = databaseApi.getActiveTasksByType(Task.TASK_TYPE_AD_TASK);
        if (tasks != null && tasks.size() > 0) {
            for (Task task : tasks) {
                AdTask adTask = task.getAdTask();
                if (pkg.equals(adTask.pkg)) {
                    return adTask;
                }

            }
        }
        return null;
    }

    @Override
    public void onAdTaskClicked(AdTask task) {
        new TaskExecutor(FlashApp.getApp()).clickAdTask(task, null);
    }

    @Override
    public void onAdTaskInstalled(AdTask task) {
        new TaskExecutor(FlashApp.getApp()).installAdTask(task, null);
    }
}
