package com.polestar.task.network;

import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import com.polestar.ad.AdUtils;
import com.polestar.task.database.DatabaseApi;
import com.polestar.task.database.DatabaseImplFactory;
import com.polestar.task.network.datamodels.User;
import com.witter.msg.Sender;

import java.util.HashSet;
import java.util.UUID;

import winterfell.flash.vpn.FlashApp;
import winterfell.flash.vpn.network.VPNServerIntermediaManager;
import winterfell.flash.vpn.utils.EventReporter;
import winterfell.flash.vpn.utils.MLogs;
import winterfell.flash.vpn.utils.PreferenceUtils;

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
    private static boolean isSecure;

    private VPNServerIntermediaManager mVpnServerInterManager;

    private AppUser() {
        Sender.check(FlashApp.getApp());
        mVpnServerInterManager = VPNServerIntermediaManager.getInstance(FlashApp.getApp());
        databaseApi = DatabaseImplFactory.getDatabaseApi(FlashApp.getApp(), DatabaseImplFactory.TARGET_FLASH_VPN);
        mainHandler = new Handler(Looper.getMainLooper());
        mObservers = new HashSet<>();
        mId = getMyId();
        initData();
        RewardInfoFetcher.get(FlashApp.getApp()).registerUpdateObserver(new RewardInfoFetcher.IRewardInfoFetchListener() {
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
        RewardInfoFetcher.get(FlashApp.getApp()).preloadRewardInfo();
//        preloadRewardVideoTask();
    }

    public static boolean check() {
        isSecure = checkInternal();
        return isSecure;
    }

    private static boolean checkInternal() {
//        MLogs.d("JJJJJ","" +AdCipher.getCertificateHashCode(FlashApp.getApp()));
        if (Sender.check(FlashApp.getApp()) != 1) {
            EventReporter.rewardEvent("check_1");
            return false;
        }
        return true;
        /*
        PackageManager pm = FlashApp.getApp().getPackageManager();
        String fakeClass = null;
        try {
            pm.getApplicationInfo("a.b", 0);
            EventReporter.rewardEvent("check_2");
            return false;
        } catch (Exception ex) {
            for(StackTraceElement element: ex.getStackTrace()) {
                String lower = element.getClassName().toLowerCase();
                if (lower.contains("vclient")
                        || lower.contains("hook")
                        || lower.contains("lody")
                        || lower.contains("lbe")
                        || lower.contains("mochat")
                        || lower.contains("dual")
                        || lower.contains("xpose")){
                    fakeClass = lower;
                    break;
                }
                if (lower.startsWith("android.")
                        || lower.startsWith("com.polestar.")
                        || lower.startsWith("com.android.")
                        || lower.startsWith("dalvik.system.")
                        || lower.startsWith("java.")){
                    continue;
                }
                fakeClass = lower;
                break;

            }
        }
        if (fakeClass !=  null) {
            EventReporter.rewardEvent("check_"+fakeClass);
            return false;
        } else {
            return true;
        }*/
    }

    //posted on main thread;
    public interface IUserUpdateListener {
        void onUserDataUpdated();
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

//    public static boolean isRewardEnabled() {
//        return (!FlashApp.isSupportPkg()) && isSecure && RemoteConfig.getBoolean(CONF_REWARD_ENABLE) ;
//    }

//    public boolean isRewardAvailable() {
//        //config is open && reward data available
//        return isRewardEnabled()
//                && databaseApi!=null && databaseApi.isDataAvailable();
//    }

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

    public void updateMyBalance(float balance)  {
        mBalance = balance;
    }

    public String getMyId() {
        //0. preference
        //1. google aid
        //2. device id
        //3. android id
        //4. Random UUID
        String id = PreferenceUtils.getMyId();
        if (!TextUtils.isEmpty(id)) {
            return id;
        } else {
            id = AdUtils.getGoogleAdvertisingId(FlashApp.getApp());
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
        PreferenceUtils.setMyId(id);
        return id;
    }

//    public ShareTask getInviteTask() {
//        List<Task> list = databaseApi.getActiveTasksByType(Task.TASK_TYPE_SHARE_TASK);
//        return list!= null && list.size() > 0 ? list.get(0).getShareTask():null;
//    }
//
//    public CheckInTask getCheckInTask() {
//        List<Task> list = databaseApi.getActiveTasksByType(Task.TASK_TYPE_CHECKIN_TASK);
//        return list!= null && list.size() > 0 ? list.get(0).getCheckInTask():null;
//    }
//
//    public RewardVideoTask getVideoTask() {
//        List<Task> list = databaseApi.getActiveTasksByType(Task.TASK_TYPE_REWARDVIDEO_TASK);
//        return list!= null && list.size() > 0 ? list.get(0).getRewardVideoTask():null;
//    }
//
//    public ReferTask getReferTask() {
//        List<Task> list = databaseApi.getActiveTasksByType(Task.TASK_TYPE_REFER_TASK);
//        return list!= null && list.size() > 0 ? list.get(0).getReferTask():null;
//    }
//
//    public RandomAwardTask getRandomAwardTask() {
//        List<Task> list = databaseApi.getActiveTasksByType(Task.TASK_TYPE_RANDOM_AWARD);
//        return list!= null && list.size() > 0 ? list.get(0).getRandomAwardTask():null;
//    }
//
//    public List<Product> getProducts() {
//        return databaseApi.getAllProductInfo();
//    }
//
////    public  getVideoTask() {
////        List<Task> list = databaseApi.getActiveTasksByType(Task.TASK_TYPE_REWARDVIDEO_TASK);
////        return list!= null && list.size() > 0 ? (RewardVideoTask) list.get(0):null;
////    }
//
//    public List<Task> getRecommendTasks() {
//        ArrayList<Task> res = new ArrayList<>();
//        res.add(getVideoTask());
//        res.add(getInviteTask());
//        return res;
////        List<Task> list;
////        list = databaseApi.get
////        list.add(databaseApi.)
//    }
//
//    public Product get1CloneProduct() {
//        List<Product> list = databaseApi.getProductInfoByType(Product.PRODUCT_TYPE_1_CLONE);
//        MLogs.d(TAG, "1clone " + list.size());
//        return list!= null && list.size() > 0 ? list.get(0):null;
//    }
//
//    void buyProduct(long productId, int amount, String email, String info, IProductStatusListener listener) {
//        AdApiHelper.consumeProduct(getMyId(), productId, amount, email, info, listener);
//    }
//
//    void finishTask(Task task, ITaskStatusListener listener) {
//        AdApiHelper.finishTask(mId, task.mId, null, listener);
//    }
//
//    void submitInviteCode(Task task, String code, ITaskStatusListener listener) {
//        AdApiHelper.finishTask(mId, task.mId, code, listener);
//    }

//    public int checkTask(Task task) {
//        if (task == null) {
//            return RewardErrorCode.TASK_UNEXPECTED_ERROR;
//        }
//        if (task instanceof ReferTask) {
//            if (!TextUtils.isEmpty(TaskPreference.getReferredBy() )){
//                return RewardErrorCode.TASK_CODE_ALREADY_SUBMITTED;
//            }
//        }
//        if (TaskPreference.getTaskFinishTodayCount(task.mId) >= task.mLimitPerDay
//                || TaskPreference.getTaskFinishCount(task.mId) >= task.mLimitTotal) {
//            return RewardErrorCode.TASK_EXCEED_DAY_LIMIT;
//        }
//        return RewardErrorCode.TASK_OK;
//    }
//
//    public void preloadRewardVideoTask() {
//        if (databaseApi.isDataAvailable()) {
//            RewardVideoTask task = getVideoTask();
//            if (task != null && TaskExecutor.checkTask(task) == RewardErrorCode.TASK_OK) {
//                FuseAdLoader adLoader = FuseAdLoader.get(task.adSlot, FlashApp.getApp());
//                if (adLoader != null) {
//                    adLoader.preloadAd(FlashApp.getApp());
//                }
//            }
//        }
//    }
//
//    public boolean isRewardVideoTaskReady() {
//        if (databaseApi.isDataAvailable()) {
//            RewardVideoTask task = getVideoTask();
//            if (task != null && TaskExecutor.checkTask(task) == RewardErrorCode.TASK_OK) {
//                FuseAdLoader adLoader = FuseAdLoader.get(task.adSlot, FlashApp.getApp());
//                if (adLoader != null) {
//                    return adLoader.hasValidCache();
//                }
//            }
//        }
//        return true;
//    }
//
//    public void setReferrerCode(String code) {
//        TaskPreference.setReferredBy(code);
//    }
//
//    public String getReferrerCode() {
//        return TaskPreference.getReferredBy();
//    }

//    public boolean checkAdFree() {
//        return ProductManager.getInstance().checkAndConsumeAdFreeTime();
//    }
//
//    public boolean checkAndConsumeClone(int num) {
//        return ProductManager.getInstance().checkAndConsumeClone(num);
//    }

}
