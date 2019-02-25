package com.polestar.clone.client.hook.proxies.am;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ServiceInfo;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;

import com.polestar.clone.client.VClientImpl;
import com.polestar.clone.client.core.VirtualCore;
import com.polestar.clone.client.interfaces.IInjector;
import com.polestar.clone.client.ipc.VActivityManager;
import com.polestar.clone.client.ipc.VPackageManager;
import com.polestar.clone.helper.compat.BuildCompat;
import com.polestar.clone.helper.utils.ComponentUtils;
import com.polestar.clone.helper.utils.Reflect;
import com.polestar.clone.helper.utils.VLog;
import com.polestar.clone.os.VUserHandle;
import com.polestar.clone.remote.InstalledAppInfo;
import com.polestar.clone.remote.StubActivityRecord;
import com.polestar.clone.StubService;

import java.util.List;

import mirror.android.app.ActivityManagerNative;
import mirror.android.app.ActivityThread;
import mirror.android.app.ClientTransactionHandler;
import mirror.android.app.IActivityManager;
import mirror.android.app.servertransaction.ClientTransaction;
import mirror.android.app.servertransaction.LaunchActivityItem;

/**
     * @author Lody
     * @see Handler.Callback
     */
    public class HCallbackStub implements Handler.Callback, IInjector {


        private static final int LAUNCH_ACTIVITY = BuildCompat.isPie()? -1 : ActivityThread.H.LAUNCH_ACTIVITY.get();
        private static final int CREATE_SERVICE = ActivityThread.H.CREATE_SERVICE.get();
        private static final int SCHEDULE_CRASH =
                ActivityThread.H.SCHEDULE_CRASH != null ? ActivityThread.H.SCHEDULE_CRASH.get() : -1;

        private static final int EXECUTE_TRANSACTION =
                BuildCompat.isPie()?  ActivityThread.H.EXECUTE_TRANSACTION.get() : -1;

        private static final String TAG = HCallbackStub.class.getSimpleName();
        private static final HCallbackStub sCallback = new HCallbackStub();

        private boolean mCalling = false;


        private Handler.Callback otherCallback;
        private HCallbackStub() {
        }

        public static HCallbackStub getDefault() {
            return sCallback;
        }

        private static Handler getH() {
            return ActivityThread.mH.get(VirtualCore.mainThread());
        }

        private static Handler.Callback getHCallback() {
            try {
                Handler handler = getH();
                return mirror.android.os.Handler.mCallback.get(handler);
            } catch (Throwable e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        public boolean handleMessage(Message msg) {
            if (!mCalling) {
                mCalling = true;
                try {
                    if (LAUNCH_ACTIVITY == msg.what) {
                        VLog.d(TAG, "LAUNCH_ACTIVITY");
                        if (!handleLaunchActivity(msg, msg.obj)) {
                            return true;
                        }
                    } else if (CREATE_SERVICE == msg.what) {
                        Intent intent = ActivityThread.CreateServiceData.intent.get(msg.obj);
                                ServiceInfo info = Reflect.on(msg.obj).get("info");
                        if (!VirtualCore.get().getHostPkg().equals(info.packageName)) {
                            info.applicationInfo = VPackageManager.get().getApplicationInfo(info.packageName, 0, VUserHandle.myUserId());
                        }
                        VLog.d(TAG, "CREATE_SERVICE " + intent + " comp: " + info);
                       // VLog.d(TAG, "CREATE_SERVICE " + intent + " codepath: " + info.applicationInfo.);
                        if (!VClientImpl.get().isBound()) {

    //                            Object packageInfo = ActivityThread.getPackageInfoNoCheck.call(VirtualCore.mainThread(), info.applicationInfo,
    //                                    CompatibilityInfo.DEFAULT_COMPATIBILITY_INFO.get());
                            if (!VirtualCore.get().getHostPkg().equals(info.packageName)) {
                                VClientImpl.get().bindApplication(info.packageName, info.processName);
                            } else {
                                VLog.logbug(TAG, "create host package service while application not bound");
                            }

    //                            mCalling = false;
    //                            getH().sendMessageAtFrontOfQueue(Message.obtain(msg));
    //                            return true;
                        }
                        if (VClientImpl.get().isBound()) {
                            //mStartedService = true;
                            StubService.startup(VirtualCore.get().getContext(), VClientImpl.get().getVPid());
                        }
                    } else if (SCHEDULE_CRASH == msg.what) {
                        // to avoid the exception send from System.
                        return true;
                    } else if (BuildCompat.isPie() && msg.what == EXECUTE_TRANSACTION) {
                        if (!handleExecuteTransaction(msg)) {
                            return true;
                        }
                    }
                    if (otherCallback != null) {
                        try {
                            boolean desired = otherCallback.handleMessage(msg);
                            mCalling = false;
                            return desired;
                        } catch (Throwable e) {
                            VLog.logbug(TAG, VLog.getStackTraceString(e));
                        }
                    } else {
                        mCalling = false;
                    }
                } finally {
                    mCalling = false;
                }
            }
            return false;
        }

    private boolean handleExecuteTransaction(Message arg8) {
        boolean v0_1;
        Object v2 = arg8.obj;
        if(ClientTransactionHandler.getActivityClient.call(VirtualCore.mainThread(), new Object[]{ClientTransaction.mActivityToken.get(v2)}) == null) {
            Object v0 = ClientTransaction.mActivityCallbacks.get(v2);
            if(v0 != null && !((List)v0).isEmpty()) {
                v0 = ((List)v0).get(0);
                v0_1 = v0.getClass() != LaunchActivityItem.TYPE ? true : this.handleLaunchActivity(arg8, v0);
                return v0_1;
            }

            v0_1 = true;
        }
        else {
            v0_1 = true;
        }

        return v0_1;
    }

        private boolean handleLaunchActivity(Message msg, Object activityRecord) {
            Object r = activityRecord;
            Intent stubIntent = Build.VERSION.SDK_INT >= 28 ?
                    LaunchActivityItem.mIntent.get(r) : ActivityThread.ActivityClientRecord.intent.get(r);
            StubActivityRecord saveInstance = new StubActivityRecord(stubIntent);
            if (saveInstance.intent == null) {
                return true;
            }
            Intent intent = saveInstance.intent;
            VLog.d(TAG, "handleLaunchActivity " + intent.toString());
            ComponentName caller = saveInstance.caller;
            IBinder token = Build.VERSION.SDK_INT >= 28 ?
                    ClientTransaction.mActivityToken.get(msg.obj) : ActivityThread.ActivityClientRecord.token.get(r);
            ActivityInfo info = saveInstance.info;
            if (VClientImpl.get().getToken() == null) {
                InstalledAppInfo installedAppInfo = VirtualCore.get().getInstalledAppInfo(info.packageName, 0);
                if(installedAppInfo == null){
                    return true;
                }
                VActivityManager.get().processRestarted(info.packageName, info.processName, saveInstance.userId);
                getH().sendMessageAtFrontOfQueue(Message.obtain(msg));
                return false;
            }
            if (!VClientImpl.get().isBound()) {
                VClientImpl.get().bindApplication(info.packageName, info.processName);
                getH().sendMessageAtFrontOfQueue(Message.obtain(msg));
                return false;
            }
            int taskId = IActivityManager.getTaskForActivity.call(
                    ActivityManagerNative.getDefault.call(),
                    token,
                    false
            );
            VActivityManager.get().onActivityCreate(ComponentUtils.toComponentName(info), caller, token, info, intent, ComponentUtils.getTaskAffinity(info), taskId, info.launchMode, info.flags);
            ClassLoader appClassLoader = VClientImpl.get().getClassLoader(info.applicationInfo);
            intent.setExtrasClassLoader(appClassLoader);
            if (Build.VERSION.SDK_INT >= 28) {
                LaunchActivityItem.mIntent.set(r, intent);
                LaunchActivityItem.mInfo.set(r, info);
            } else {
                ActivityThread.ActivityClientRecord.intent.set(r, intent);
                ActivityThread.ActivityClientRecord.activityInfo.set(r, info);
            }
            return true;
        }

        @Override
        public void inject() throws Throwable {
            otherCallback = getHCallback();
            mirror.android.os.Handler.mCallback.set(getH(), this);
        }

        @Override
        public boolean isEnvBad() {
            Handler.Callback callback = getHCallback();
            boolean envBad = callback != this;
            if (callback != null && envBad) {
            VLog.logbug(TAG, "HCallback has bad, other callback = " + callback);
            }
            return envBad;
        }

    }
