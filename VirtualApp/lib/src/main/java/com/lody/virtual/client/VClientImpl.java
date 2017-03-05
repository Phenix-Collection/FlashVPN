package com.lody.virtual.client;

import android.annotation.SuppressLint;
import android.app.Application;
import android.app.Instrumentation;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.os.Binder;
import android.os.Build;
import android.os.ConditionVariable;
import android.os.Handler;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.os.StrictMode;

import com.lody.virtual.client.core.PatchManager;
import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.client.env.SpecialComponentList;
import com.lody.virtual.client.env.VirtualRuntime;
import com.lody.virtual.client.fixer.ContextFixer;
import com.lody.virtual.client.hook.delegate.AppInstrumentation;
import com.lody.virtual.client.hook.patchs.am.HCallbackHook;
import com.lody.virtual.client.hook.providers.ProviderHook;
import com.lody.virtual.client.hook.secondary.ProxyServiceFactory;
import com.lody.virtual.client.ipc.VActivityManager;
import com.lody.virtual.client.ipc.VPackageManager;
import com.lody.virtual.client.stub.StubManifest;
import com.lody.virtual.remote.PendingResultData;
import com.lody.virtual.helper.utils.VLog;
import com.lody.virtual.os.VEnvironment;
import com.lody.virtual.os.VUserHandle;
import com.lody.virtual.server.secondary.FakeIdentityBinder;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import mirror.android.app.ActivityThread;
import mirror.android.app.ActivityThreadNMR1;
import mirror.android.app.ContextImpl;
import mirror.android.app.ContextImplICS;
import mirror.android.app.ContextImplKitkat;
import mirror.android.app.IActivityManager;
import mirror.android.app.LoadedApk;
import mirror.android.content.res.CompatibilityInfo;
import mirror.android.providers.Settings;
import mirror.android.renderscript.RenderScriptCacheDir;
import mirror.android.view.HardwareRenderer;
import mirror.android.view.RenderScript;
import mirror.android.view.ThreadedRenderer;
import mirror.com.android.internal.content.ReferrerIntent;
import mirror.dalvik.system.VMRuntime;

import static com.lody.virtual.os.VUserHandle.getUserId;

/**
 * @author Lody
 */

public final class VClientImpl extends IVClient.Stub {

	private static final int NEW_INTENT = 11;
    private static final int RECEIVER = 12;

	private static final String TAG = VClientImpl.class.getSimpleName();
	@SuppressLint("StaticFieldLeak")
	private static final VClientImpl gClient = new VClientImpl();
    private final H mH = new H();
    private ConditionVariable mTempLock;
	private Instrumentation mInstrumentation = AppInstrumentation.getDefault();

	private IBinder token;
	private int vuid;
	private AppBindData mBoundApplication;
	private Application mInitialApplication;

    public static VClientImpl get() {
        return gClient;
    }

	public boolean isBound() {
		return mBoundApplication != null;
	}


	public Application getCurrentApplication() {
		return mInitialApplication;
	}

	public String getCurrentPackage() {
		return mBoundApplication != null ? mBoundApplication.appInfo.packageName : null;
	}

	public int getVUid() {
		return vuid;
	}

	public int getBaseVUid() {
		return VUserHandle.getAppId(vuid);
	}

	public ClassLoader getClassLoader(ApplicationInfo appInfo) {
		Context context = createPackageContext(appInfo.packageName);
		return context.getClassLoader();
	}

	private void sendMessage(int what, Object obj) {
		Message msg = Message.obtain();
		msg.what = what;
		msg.obj = obj;
		mH.sendMessage(msg);
	}

	@Override
	public IBinder getAppThread() {
		Binder appThread = ActivityThread.getApplicationThread.call(VirtualCore.mainThread());
		return new FakeIdentityBinder(appThread) {
			@Override
			protected int getFakeUid() {
				return Process.SYSTEM_UID;
			}
		};
	}

	@Override
	public IBinder getToken() {
		return token;
	}

	public void initProcess(IBinder token, int vuid) {
		if (this.token != null) {
			Exception ex =  new IllegalStateException("Token is exist!");
			VLog.logbug(TAG, VLog.getStackTraceString(ex));
		}
		this.token = token;
		this.vuid = vuid;
		VLog.d(TAG, "initProcess for vuid: " + vuid);
	}

	private void handleNewIntent(NewIntentData data) {
		Intent intent;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
			intent = ReferrerIntent.ctor.newInstance(data.intent, data.creator);
		} else {
			intent = data.intent;
		}
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.N) {
            ActivityThread.performNewIntents.call(
                    VirtualCore.mainThread(),
                    data.token,
                    Collections.singletonList(intent)
            );
		} else {
            ActivityThreadNMR1.performNewIntents.call(
                    VirtualCore.mainThread(),
                    data.token,
                    Collections.singletonList(intent),
                    true
            );
        }
	}

	@Override
	public void bindApplication(final String packageName, final String processName) {
		if (Looper.getMainLooper() == Looper.myLooper()) {
			bindApplicationNoCheck(packageName, processName, new ConditionVariable());
		} else {
			final ConditionVariable lock = new ConditionVariable();
			VirtualRuntime.getUIHandler().post(new Runnable() {
				@Override
				public void run() {
					bindApplicationNoCheck(packageName, processName, lock);
					lock.open();
				}
			});
			lock.block();
		}
	}

	private void bindApplicationNoCheck(String packageName, String processName, ConditionVariable lock) {
		VLog.d(TAG, "bindApplicationNoCheck " + packageName + " proc: " + processName);
		mTempLock = lock;
		if (isBound()) {
			mTempLock  = null;
			if (lock != null) {
				lock.open();
			}
			VLog.logbug(TAG,"Already bound process: " + processName + " for package: " + packageName );
			return;
		}
		try {
			fixInstalledProviders();
		} catch (Throwable e) {
			e.printStackTrace();
		}
		ActivityThread.mInitialApplication.set(
				VirtualCore.mainThread(),
				null
		);
		AppBindData data = new AppBindData();
		data.appInfo = VPackageManager.get().getApplicationInfo(packageName, 0, getUserId(vuid));
		data.processName = processName;
		data.providers = VPackageManager.get().queryContentProviders(processName, getVUid(), PackageManager.GET_META_DATA);
		mBoundApplication = data;
		VirtualRuntime.setupRuntime(data.processName, data.appInfo);
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public synchronized void start() {
				new Exception().printStackTrace();
				super.start();
			}
		});
        int targetSdkVersion = data.appInfo.targetSdkVersion;
        if (targetSdkVersion < Build.VERSION_CODES.GINGERBREAD) {
			StrictMode.ThreadPolicy newPolicy = new StrictMode.ThreadPolicy.Builder(StrictMode.getThreadPolicy()).permitNetwork().build();
			StrictMode.setThreadPolicy(newPolicy);
		}
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            if (mirror.android.os.StrictMode.sVmPolicyMask != null) {
                mirror.android.os.StrictMode.sVmPolicyMask.set(0);
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && targetSdkVersion < Build.VERSION_CODES.LOLLIPOP) {
            mirror.android.os.Message.updateCheckRecycle.call(targetSdkVersion);
        }
        if (StubManifest.ENABLE_IO_REDIRECT && SpecialComponentList.needIORedirect(packageName)) {
            startIOUniformer();
        }
		NativeEngine.hookNative();
		Object mainThread = VirtualCore.mainThread();
		NativeEngine.startDexOverride();
		Context context = createPackageContext(data.appInfo.packageName);
		System.setProperty("java.io.tmpdir", context.getCacheDir().getAbsolutePath());
		File codeCacheDir;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			codeCacheDir = context.getCodeCacheDir();
		} else {
			codeCacheDir = context.getCacheDir();
		}
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            if (HardwareRenderer.setupDiskCache != null) {
                HardwareRenderer.setupDiskCache.call(codeCacheDir);
            }
        } else {
            if (ThreadedRenderer.setupDiskCache != null) {
                ThreadedRenderer.setupDiskCache.call(codeCacheDir);
            }
        }
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			if (RenderScriptCacheDir.setupDiskCache != null) {
				RenderScriptCacheDir.setupDiskCache.call(codeCacheDir);
			}
		} else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
			if (RenderScript.setupDiskCache != null) {
				RenderScript.setupDiskCache.call(codeCacheDir);
			}
		}
		File filesDir = new File(data.appInfo.dataDir, "files");
		File cacheDir = new File(data.appInfo.dataDir, "cache");
		if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
			if (ContextImplICS.mExternalFilesDir != null) {
				ContextImplICS.mExternalFilesDir.set(context, filesDir);
			}
			if (ContextImplICS.mExternalCacheDir != null) {
				ContextImplICS.mExternalCacheDir.set(context, cacheDir);
			}
		} else {
			if (ContextImplKitkat.mExternalCacheDirs != null) {
				ContextImplKitkat.mExternalCacheDirs.set(context, new File[] {cacheDir});
			}
			if (ContextImplKitkat.mExternalFilesDirs != null) {
				ContextImplKitkat.mExternalFilesDirs.set(context, new File[] {filesDir});
			}
		}
		Object boundApp = fixBoundApp(mBoundApplication);
		if (mainThread != null) {
			mBoundApplication.info = ActivityThread.getPackageInfoNoCheck.call(mainThread, data.appInfo,
					CompatibilityInfo.DEFAULT_COMPATIBILITY_INFO.get());
		}
		if (mBoundApplication.info == null) {
			VLog.logbug(TAG, "getPackageInfoNoCheck mainThread : " + mainThread == null? "null":"not null" + " error");
			mBoundApplication.info = ContextImpl.mPackageInfo.get(context);
		}
		mirror.android.app.ActivityThread.AppBindData.info.set(boundApp, data.info);
		VMRuntime.setTargetSdkVersion.call(VMRuntime.getRuntime.call(), data.appInfo.targetSdkVersion);

		boolean conflict = SpecialComponentList.isConflictingInstrumentation(packageName);
		if (!conflict) {
			PatchManager.getInstance().checkEnv(AppInstrumentation.class);
		}
		if (data.info == null) {
			VLog.logbug("VClientImpl", "bindApplicationNoCheck:" + packageName + ":"+processName + ":data.info null");
			//should return here
		}
        mInitialApplication = LoadedApk.makeApplication.call(data.info, false, null);
		if(mInitialApplication == null) {
			VLog.logbug(TAG, "mInitialApplication is null");
			if (data.info != null) {
				mInitialApplication = LoadedApk.makeApplication.call(data.info, false, null);
			}
		}
        mirror.android.app.ActivityThread.mInitialApplication.set(mainThread, mInitialApplication);
        ContextFixer.fixContext(mInitialApplication);
		List<ProviderInfo> providers = VPackageManager.get().queryContentProviders(data.processName, vuid, PackageManager.GET_META_DATA);
		if (providers != null) {
            installContentProviders(mInitialApplication, providers);
		}
		if (lock != null) {
			lock.open();
			mTempLock = null;
		}
		try {
            mInstrumentation.callApplicationOnCreate(mInitialApplication);
			PatchManager.getInstance().checkEnv(HCallbackHook.class);
            if (conflict) {
				PatchManager.getInstance().checkEnv(AppInstrumentation.class);
			}
            Application createdApp = ActivityThread.mInitialApplication.get(mainThread);
            if (createdApp != null) {
                mInitialApplication = createdApp;
            }
		} catch (Exception e) {
            if (!mInstrumentation.onException(mInitialApplication, e)) {
				throw new RuntimeException(
                        "Unable to create application " + mInitialApplication == null? "null app" : mInitialApplication.getClass().getName()
								+ ": " + e.toString(), e);
			}
		}
		VActivityManager.get().appDoneExecuting();
	}

	@SuppressLint("SdCardPath")
	private void startIOUniformer() {
		ApplicationInfo info = mBoundApplication.appInfo;
		NativeEngine.redirect("/data/data/" + info.packageName + "/", info.dataDir + "/");
		NativeEngine.redirect("/data/user/0/" + info.packageName + "/", info.dataDir + "/");

 		/*
		*  /data/user/0/{Host-Pkg}/virtual/data/user/{user-id}/lib -> /data/user/0/{Host-Pkg}/virtual/data/app/{App-Pkg}/lib/
		*/
		NativeEngine.redirect(
				new File(VEnvironment.getUserSystemDirectory(VUserHandle.myUserId()).getAbsolutePath(), "lib").getAbsolutePath() + "/",
				info.nativeLibraryDir + "/");
		NativeEngine.hook();
	}

	private Context createPackageContext(String packageName) {
		try {
			Context hostContext = VirtualCore.get().getContext();
			return hostContext.createPackageContext(packageName, Context.CONTEXT_INCLUDE_CODE | Context.CONTEXT_IGNORE_SECURITY);
		} catch (PackageManager.NameNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	private Object fixBoundApp(AppBindData data) {
		// TODO: Using Native VM Hook to fix the `Camera` and `AudioRecord`.
		Object thread = VirtualCore.mainThread();
		Object boundApp = mirror.android.app.ActivityThread.mBoundApplication.get(thread);
		mirror.android.app.ActivityThread.AppBindData.appInfo.set(boundApp, data.appInfo);
		mirror.android.app.ActivityThread.AppBindData.processName.set(boundApp, data.processName);
		mirror.android.app.ActivityThread.AppBindData.instrumentationName.set(boundApp, new ComponentName(data.appInfo.packageName, Instrumentation.class.getName()));
		return boundApp;
	}

	private void installContentProviders(Context app, List<ProviderInfo> providers) {
		long origId = Binder.clearCallingIdentity();
		Object mainThread = VirtualCore.mainThread();
		try {
			for (ProviderInfo cpi : providers) {
				if (cpi.enabled) {
					ActivityThread.installProvider(mainThread, app, cpi, null);
				}
			}
		} finally {
			Binder.restoreCallingIdentity(origId);
		}
	}


	@Override
	public IBinder acquireProviderClient(ProviderInfo info) {
		VLog.d(TAG, "enter acquireProviderClient " + info.authority);
		if (mTempLock != null) {
			mTempLock.block();
		}
		VLog.d(TAG, "no lock acquireProviderClient " + info.authority + " process " + info.processName);
		if (!VClientImpl.get().isBound()) {
			VClientImpl.get().bindApplication(info.packageName, info.processName);
		}
		IInterface provider = null;
		String[] authorities = info.authority.split(";");
		String authority = authorities.length == 0 ? info.authority : authorities[0];
		ContentResolver resolver = VirtualCore.get().getContext().getContentResolver();
		ContentProviderClient client = null;
		try {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
				client = resolver.acquireUnstableContentProviderClient(authority);
			} else {
				client = resolver.acquireContentProviderClient(authority);
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}
		if (client != null) {
			provider = mirror.android.content.ContentProviderClient.mContentProvider.get(client);
			client.release();
		} else {
			VLog.logbug(TAG, "acquireProviderClient client is null");
		}
		VLog.d(TAG, "acquireProviderClient return " + provider);
		return provider != null ? provider.asBinder() : null;
	}

	private void fixInstalledProviders() {
		clearSettingProvider();
		Map clientMap = ActivityThread.mProviderMap.get(VirtualCore.mainThread());
        boolean highApi = Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN;
		for (Object clientRecord : clientMap.values()) {
            if (highApi) {
				IInterface provider = ActivityThread.ProviderClientRecordJB.mProvider.get(clientRecord);
				Object holder = ActivityThread.ProviderClientRecordJB.mHolder.get(clientRecord);
				ProviderInfo info = IActivityManager.ContentProviderHolder.info.get(holder);
				VLog.d(TAG, "fixInstalledProviders " + info.authority);
				if (holder != null && !info.authority.startsWith(StubManifest.STUB_CP_AUTHORITY)) {
					provider = ProviderHook.createProxy(true, info.authority, provider);
					ActivityThread.ProviderClientRecordJB.mProvider.set(clientRecord, provider);
					IActivityManager.ContentProviderHolder.provider.set(holder, provider);
				}
			} else {
				String authority = ActivityThread.ProviderClientRecord.mName.get(clientRecord);
				IInterface provider = ActivityThread.ProviderClientRecord.mProvider.get(clientRecord);
				if (provider != null && !authority.startsWith(StubManifest.STUB_CP_AUTHORITY)) {
					provider = ProviderHook.createProxy(true, authority, provider);
					ActivityThread.ProviderClientRecord.mProvider.set(clientRecord, provider);
				}
			}
		}

	}

    private void clearSettingProvider() {
        Object cache;
        if (Settings.System.TYPE != null) {
            cache = Settings.System.sNameValueCache.get();
            if (cache != null) {
                Settings.NameValueCache.mContentProvider.set(cache, null);
            }
        }
        if (Settings.Secure.TYPE != null) {
            cache = Settings.Secure.sNameValueCache.get();
            if (cache != null) {
                Settings.NameValueCache.mContentProvider.set(cache, null);
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1 && Settings.Global.TYPE != null) {
            cache = Settings.Global.sNameValueCache.get();
            if (cache != null) {
                Settings.NameValueCache.mContentProvider.set(cache, null);
            }
        }
    }

	@Override
	public void finishActivity(IBinder token) {
		VActivityManager.get().finishActivity(token);
	}

	@Override
	public void scheduleNewIntent(String creator, IBinder token, Intent intent) {
		NewIntentData data = new NewIntentData();
		data.creator = creator;
		data.token = token;
		data.intent = intent;
		sendMessage(NEW_INTENT, data);
	}

    @Override
    public void scheduleReceiver(String processName, ComponentName component, Intent intent, PendingResultData resultData) {
        ReceiverData receiverData = new ReceiverData();
        receiverData.resultData = resultData;
        receiverData.intent = intent;
        receiverData.component = component;
        receiverData.processName = processName;
        sendMessage(RECEIVER, receiverData);
    }

    private void handleReceiver(ReceiverData data) {
        BroadcastReceiver.PendingResult result = data.resultData.build();
		VLog.d(TAG, "handleReceiver " + data.intent + " on " + data.component);
        try {
            if (!isBound()) {
                bindApplication(data.component.getPackageName(), data.processName);
            }
            Context context = mInitialApplication.getBaseContext();
            Context receiverContext = ContextImpl.getReceiverRestrictedContext.call(context);
            String className = data.component.getClassName();
            BroadcastReceiver receiver = (BroadcastReceiver) context.getClassLoader().loadClass(className).newInstance();
            mirror.android.content.BroadcastReceiver.setPendingResult.call(receiver, result);
            data.intent.setExtrasClassLoader(context.getClassLoader());
            receiver.onReceive(receiverContext, data.intent);
            if (mirror.android.content.BroadcastReceiver.getPendingResult.call(receiver) != null) {
                result.finish();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Exception exc = new RuntimeException(
                    "Unable to start receiver " + data.component
                            + ": " + e.toString(), e);
			VLog.logbug(TAG, "Unable to start receiver " + data.component
					+ ": " + e.toString() );
			VLog.logbug(TAG, VLog.getStackTraceString(exc));
        } finally {
			VActivityManager.get().broadcastFinish(data.resultData);
		}
    }

	@Override
	public IBinder createProxyService(ComponentName component, IBinder binder) {
		return ProxyServiceFactory.getProxyService(getCurrentApplication(), component, binder);
	}

	@Override
	public String getDebugInfo() {
		return "process : " + VirtualRuntime.getProcessName() + "\n" +
				"initialPkg : " + VirtualRuntime.getInitialPackageName() + "\n" +
				"vuid : " + vuid;
	}
	private final class NewIntentData {
		String creator;
		IBinder token;
		Intent intent;
	}

	private final class AppBindData {
		String processName;
		ApplicationInfo appInfo;
		List<ProviderInfo> providers;
		Object info;
	}

	private final class ReceiverData {
		PendingResultData resultData;
		Intent intent;
		ComponentName component;
        String processName;
	}

	private class H extends Handler {

		private H() {
			super(Looper.getMainLooper());
		}

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case NEW_INTENT: {
					handleNewIntent((NewIntentData) msg.obj);
				}
				break;
				case RECEIVER: {
					handleReceiver((ReceiverData) msg.obj);
				}
				break;
			}
		}
	}
}
