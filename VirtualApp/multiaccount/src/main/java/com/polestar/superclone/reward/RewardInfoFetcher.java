package com.polestar.superclone.reward;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;

import com.polestar.superclone.utils.CommonUtils;
import com.polestar.superclone.utils.EventReporter;
import com.polestar.superclone.utils.MLogs;
import com.polestar.superclone.utils.RemoteConfig;
import com.polestar.task.ADErrorCode;
import com.polestar.task.IProductStatusListener;
import com.polestar.task.ITaskStatusListener;
import com.polestar.task.IUserStatusListener;
import com.polestar.task.database.DatabaseApi;
import com.polestar.task.database.DatabaseImplFactory;
import com.polestar.task.database.datamodels.AdTask;
import com.polestar.task.network.AdApiHelper;
import com.polestar.task.network.datamodels.Product;
import com.polestar.task.network.datamodels.Task;
import com.polestar.task.network.datamodels.User;

import java.util.ArrayList;
import java.util.HashSet;

/**
 * Created by guojia on 2019/1/26.
 */

public class RewardInfoFetcher extends BroadcastReceiver{
    private final static String TAG = "RewardInfoFetcher";

    private Context mContext;
    private static RewardInfoFetcher sInstance;
    private Handler workHandler;
    private static long UPDATE_INTERVAL = 3600*1000;

    private final static long FORCE_UPDATE_INTERVAL = 2000;
    private final static int FORCE_RETRY_TIMES = 5;

    private final static int MSG_FETCH_INFO = 1;

    private DatabaseApi databaseApi;
    private int forceRetry;

    private HashSet<IRewardInfoFetchListener> mRegistry;

    public interface IRewardInfoFetchListener{
        void onFetched();
    }

    private RewardInfoFetcher(Context context) {
        UPDATE_INTERVAL = RemoteConfig.getLong("config_update_interval_sec")*1000;
        mContext = context;
        forceRetry = 0;
        databaseApi = DatabaseImplFactory.getDatabaseApi(context);
        mRegistry = new HashSet<>();
        HandlerThread thread = new HandlerThread("sync_task");
        thread.start();
        workHandler = new Handler(thread.getLooper()){
            @Override
            public void handleMessage(Message msg) {
                switch(msg.what) {
                    case MSG_FETCH_INFO:
                        long interval;
                        if (databaseApi.isDataAvailable()) {
                            forceRetry = 0;
                            interval = UPDATE_INTERVAL;
                        } else {
                            interval = forceRetry++ >= FORCE_RETRY_TIMES ? UPDATE_INTERVAL : FORCE_UPDATE_INTERVAL;
                        }
                        checkAndFetchInfo(!databaseApi.isDataAvailable());
                        workHandler.sendMessageDelayed(workHandler.obtainMessage(MSG_FETCH_INFO), interval);
                        break;
                }
            }
        };
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        mContext.registerReceiver(this, filter);
    }

    //TODO need device id?
    private void checkAndFetchInfo(boolean force) {
        MLogs.d(TAG, "checkAndFetchInfo force: " + force + " myId " + AppUser.getInstance().getMyId());
        if(!force && (System.currentTimeMillis() - TaskPreference.getLastUpdateTime()
                < UPDATE_INTERVAL)) {
            MLogs.d(TAG, "already fetched at " + TaskPreference.getLastUpdateTime());
            return;
        }
        AdApiHelper.register(mContext, AppUser.getInstance().getMyId(), new IUserStatusListener() {
            @Override
            public void onRegisterSuccess(User user) {
                databaseApi.setUserInfo(user);
                MLogs.d(TAG, "register success " + user);
                AdApiHelper.getAvailableTasks(AppUser.getInstance().getMyId(), new ITaskStatusListener(){
                    @Override
                    public void onTaskSuccess(long taskId, float payment, float balance) {

                    }

                    @Override
                    public void onTaskFail(long taskId, ADErrorCode code) {

                    }

                    @Override
                    public void onGetAllAvailableTasks(ArrayList<Task> tasks) {
//                        AdTask adTask1 = new AdTask();
//                        adTask1.mId = 101;
//                        adTask1.mDescription = "Install the app to get additional bonus";
//                        adTask1.mTitle = "Mine Sweeper Classic";
//                        adTask1.mLimitPerDay = 1;
//                        adTask1.mLimitTotal = 1;
//                        adTask1.mPayout = 105;
//                        adTask1.mTaskType = Task.TASK_TYPE_AD_TASK;
//
//                        adTask1.includeSlots = "slot_*;pop_*;task_*";
//                        adTask1.excludeSlots = "";
//                        adTask1.clickUrl = "market://details?id=com.polestar.minesweeper&referrer?UTM_SOURCE=spc_update";
//                        adTask1.iconUrl = "https://googleads.g.doubleclick.net/pagead/imgad?id=CICAgKDb-7HatwEQkAMYkAMyCC6zU1eF3o0Y";
//                        adTask1.imageUrl = "https://googleads.g.doubleclick.net/pagead/imgad?id=CICAgKC7p4CmaxCoChiyBTIINW9N25lE9Ig";
//                        adTask1.priority = 1;
//                        adTask1.flow="cpc";
//                        adTask1.adDesc="Play this funny game and get 100 coins reward!";
//                        adTask1.adid="1000";
//                        adTask1.ctaText="Install";
//                        adTask1.country="*";
//                        adTask1.pkg="com.polestar.minesweeper";
//                        adTask1.mAdTask = adTask1;
//
//                        AdTask adTask2 = new AdTask(adTask1);
//                        adTask2.mId=1001;
//                        adTask2.includeSlots = "slot_*;task_*";
//                        adTask2.excludeSlots = "";
//                        adTask2.clickUrl = "market://details?id=com.polestar.super.clone&referrer?UTM_SOURCE=spc_update";
//                        adTask2.iconUrl = "https://googleads.g.doubleclick.net/pagead/imgad?id=CICAgKDb-7HatwEQkAMYkAMyCC6zU1eF3o0Y";
//                        adTask2.imageUrl = "https://googleads.g.doubleclick.net/pagead/imgad?id=CICAgKC7p4CmaxCoChiyBTIINW9N25lE9Ig";
//                        adTask2.priority = 10;
//                        adTask2.flow="cpc";
//                        adTask2.adDesc="Play this funny game and get 200 coins reward!";
//                        adTask2.adid="1001";
//                        adTask2.ctaText="Download";
//                        adTask2.country="*";
//                        adTask2.pkg="do.multiple.cloner";
//                        adTask2.mAdTask = adTask2;
//
//                        tasks.add(adTask1);
//                        tasks.add(adTask2);
                        databaseApi.setActiveTasks(tasks);
                        MLogs.d(TAG, "onGetAllAvailableTasks success ");
                        AdApiHelper.getAvailableProducts(AppUser.getInstance().getMyId(), new IProductStatusListener() {
                            @Override
                            public void onConsumeSuccess(long id, int amount, float totalCost, float balance) {

                            }

                            @Override
                            public void onConsumeFail(ADErrorCode code) {

                            }

                            @Override
                            public void onGetAllAvailableProducts(ArrayList<Product> products) {
                                MLogs.d(TAG, "onGetAllAvailableProducts success ");
                                databaseApi.setActiveProducts(products);
                                TaskPreference.updateLastUpdateTime();
                                for(IRewardInfoFetchListener listener: mRegistry) {
                                    listener.onFetched();
                                }
                            }

                            @Override
                            public void onGeneralError(ADErrorCode code) {
                                EventReporter.rewardEvent("fetch_error_" + code.getErrCode());
                                MLogs.d(TAG, "onError " + code);
                            }
                        },force);
                    }

                    @Override
                    public void onGeneralError(ADErrorCode code) {
                        EventReporter.rewardEvent("fetch_error_" + code.getErrCode());
                        MLogs.d(TAG, "onError " + code);
                    }
                },force);
            }

            @Override
            public void onRegisterFailed(ADErrorCode errorCode) {

            }

            @Override
            public void onGeneralError(ADErrorCode code) {
                EventReporter.rewardEvent("fetch_error_" + code.getErrCode());
                MLogs.d(TAG, "onError " + code);
            }
        }, force);

    }

    public static synchronized RewardInfoFetcher get(Context context) {
        if(sInstance == null) {
            sInstance = new RewardInfoFetcher(context);
        }
        return sInstance;
    }

    public void preloadRewardInfo() {
        MLogs.d(TAG, "preloadRewardInfo");
        workHandler.removeMessages(MSG_FETCH_INFO);
        workHandler.sendMessage(
                workHandler.obtainMessage(MSG_FETCH_INFO));
    }

    //Just start another round retry
    public void forceRefresh() {
        forceRetry = 0;
        preloadRewardInfo();
    }

    public synchronized void registerUpdateObserver(IRewardInfoFetchListener listener) {
        mRegistry.add(listener);
    }

    public synchronized void unregisterUpdateObserver(IRewardInfoFetchListener listener) {
        mRegistry.remove(listener);
    }
    //public void fetchRewardInfo(boolean force, )

    @Override
    public void onReceive(Context context, Intent intent) {
        MLogs.d("RewardInfoFetcher " + intent);
        if (CommonUtils.isNetworkAvailable(context)){
            preloadRewardInfo();
        }
    }
}
