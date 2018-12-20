package com.polestar.clone.client.core;

import android.os.Build;

import com.polestar.clone.client.hook.base.MethodInvocationProxy;
import com.polestar.clone.client.hook.base.MethodInvocationStub;
import com.polestar.clone.client.hook.delegate.AppInstrumentation;
import com.polestar.clone.client.hook.proxies.account.AccountManagerStub;
import com.polestar.clone.client.hook.proxies.alarm.AlarmManagerStub;
import com.polestar.clone.client.hook.proxies.am.ActivityManagerStub;
import com.polestar.clone.client.hook.proxies.am.AutoFillManagerStub;
import com.polestar.clone.client.hook.proxies.am.HCallbackStub;
import com.polestar.clone.client.hook.proxies.am.UpdateEngineStub;
import com.polestar.clone.client.hook.proxies.appops.AppOpsManagerStub;
import com.polestar.clone.client.hook.proxies.appwidget.AppWidgetManagerStub;
import com.polestar.clone.client.hook.proxies.audio.AudioManagerStub;
import com.polestar.clone.client.hook.proxies.backup.BackupManagerStub;
import com.polestar.clone.client.hook.proxies.bluetooth.BluetoothStub;
import com.polestar.clone.client.hook.proxies.clipboard.ClipBoardStub;
import com.polestar.clone.client.hook.proxies.connectivity.ConnectivityStub;
import com.polestar.clone.client.hook.proxies.content.ContentServiceStub;
import com.polestar.clone.client.hook.proxies.context_hub.ContextHubServiceStub;
import com.polestar.clone.client.hook.proxies.devicepolicy.DevicePolicyManagerStub;
import com.polestar.clone.client.hook.proxies.display.DisplayStub;
import com.polestar.clone.client.hook.proxies.dropbox.DropBoxManagerStub;
import com.polestar.clone.client.hook.proxies.fingerprint.FingerprintManagerStub;
import com.polestar.clone.client.hook.proxies.graphics.GraphicsStatsStub;
import com.polestar.clone.client.hook.proxies.imms.MmsStub;
import com.polestar.clone.client.hook.proxies.input.InputMethodManagerStub;
import com.polestar.clone.client.hook.proxies.isms.ISmsStub;
import com.polestar.clone.client.hook.proxies.isub.ISubStub;
import com.polestar.clone.client.hook.proxies.job.JobServiceStub;
import com.polestar.clone.client.hook.proxies.libcore.LibCoreStub;
import com.polestar.clone.client.hook.proxies.location.LocationManagerStub;
import com.polestar.clone.client.hook.proxies.media.router.MediaRouterServiceStub;
import com.polestar.clone.client.hook.proxies.media.session.SessionManagerStub;
import com.polestar.clone.client.hook.proxies.mount.MountServiceStub;
import com.polestar.clone.client.hook.proxies.network.NetworkManagementStub;
import com.polestar.clone.client.hook.proxies.network.NetworkScoreManagerStub;
import com.polestar.clone.client.hook.proxies.notification.NotificationManagerStub;
import com.polestar.clone.client.hook.proxies.persistent_data_block.PersistentDataBlockServiceStub;
import com.polestar.clone.client.hook.proxies.phonesubinfo.PhoneSubInfoStub;
import com.polestar.clone.client.hook.proxies.pm.PackageManagerStub;
import com.polestar.clone.client.hook.proxies.power.PowerManagerStub;
import com.polestar.clone.client.hook.proxies.restriction.RestrictionStub;
import com.polestar.clone.client.hook.proxies.search.SearchManagerStub;
import com.polestar.clone.client.hook.proxies.shortcut.ShortcutServiceStub;
import com.polestar.clone.client.hook.proxies.statusbar.StatusBarManagerStub;
import com.polestar.clone.client.hook.proxies.telephony.TelephonyRegistryStub;
import com.polestar.clone.client.hook.proxies.telephony.TelephonyStub;
import com.polestar.clone.client.hook.proxies.usage.UsageStatsManagerStub;
import com.polestar.clone.client.hook.proxies.user.UserManagerStub;
import com.polestar.clone.client.hook.proxies.vibrator.VibratorStub;
import com.polestar.clone.client.hook.proxies.wifi.WifiManagerStub;
import com.polestar.clone.client.hook.proxies.wifi_scanner.WifiScannerStub;
import com.polestar.clone.client.hook.proxies.window.WindowManagerStub;
import com.polestar.clone.client.interfaces.IInjector;

import java.util.HashMap;
import java.util.Map;

import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR1;
import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR2;
import static android.os.Build.VERSION_CODES.KITKAT;
import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static android.os.Build.VERSION_CODES.LOLLIPOP_MR1;
import static android.os.Build.VERSION_CODES.M;
import static android.os.Build.VERSION_CODES.N;
import static android.os.Build.VERSION_CODES.O;

/**
 * @author Lody
 *
 */
public final class InvocationStubManager {

    private static InvocationStubManager sInstance = new InvocationStubManager();
    private static boolean sInit;

	private Map<Class<?>, IInjector> mInjectors = new HashMap<>(13);

	private InvocationStubManager() {
	}

	public static InvocationStubManager getInstance() {
		return sInstance;
	}

	void injectAll() throws Throwable {
		for (IInjector injector : mInjectors.values()) {
			injector.inject();
		}
		// XXX: Lazy inject the Instrumentation,
		addInjector(AppInstrumentation.getDefault());
	}

    /**
	 * @return if the InvocationStubManager has been initialized.
	 */
	public boolean isInit() {
		return sInit;
	}


	public void init() throws Throwable {
		if (isInit()) {
			throw new IllegalStateException("InvocationStubManager Has been initialized.");
		}
		injectInternal();
		sInit = true;

	}

	private void injectInternal() throws Throwable {
		if (VirtualCore.get().isMainProcess()) {
			return;
		}
		if (VirtualCore.get().isServerProcess()) {
			addInjector(new ActivityManagerStub());
			addInjector(new PackageManagerStub());
			return;
		}
		if (VirtualCore.get().isVAppProcess()) {
			addInjector(new LibCoreStub());
			addInjector(new ActivityManagerStub());
			addInjector(new PackageManagerStub());
			addInjector(HCallbackStub.getDefault());
			addInjector(new ISmsStub());
			addInjector(new ISubStub());
			addInjector(new DropBoxManagerStub());
			addInjector(new NotificationManagerStub());
			addInjector(new LocationManagerStub());
			addInjector(new WindowManagerStub());
			addInjector(new ClipBoardStub());
			addInjector(new MountServiceStub());
			addInjector(new BackupManagerStub());
			addInjector(new TelephonyStub());
			addInjector(new TelephonyRegistryStub());
			addInjector(new PhoneSubInfoStub());
			addInjector(new PowerManagerStub());
			addInjector(new AppWidgetManagerStub());
			addInjector(new AccountManagerStub());
			addInjector(new AudioManagerStub());
			addInjector(new SearchManagerStub());
			addInjector(new ContentServiceStub());
			addInjector(new ConnectivityStub());
			addInjector(new StatusBarManagerStub());

			if (Build.VERSION.SDK_INT >= JELLY_BEAN_MR2) {
				addInjector(new VibratorStub());
				addInjector(new WifiManagerStub());
				addInjector(new BluetoothStub());
				addInjector(new ContextHubServiceStub());
			}
			if (Build.VERSION.SDK_INT >= JELLY_BEAN_MR1) {
				addInjector(new UserManagerStub());
			}

			if (Build.VERSION.SDK_INT >= JELLY_BEAN_MR1) {
				addInjector(new DisplayStub());
			}
			if (Build.VERSION.SDK_INT >= LOLLIPOP) {
				addInjector(new PersistentDataBlockServiceStub());
				addInjector(new InputMethodManagerStub());
				addInjector(new MmsStub());
				addInjector(new SessionManagerStub());
				addInjector(new JobServiceStub());
				addInjector(new RestrictionStub());
			}
			if (Build.VERSION.SDK_INT >= KITKAT) {
				addInjector(new AlarmManagerStub());
				addInjector(new AppOpsManagerStub());
				addInjector(new MediaRouterServiceStub());
			}
			if (Build.VERSION.SDK_INT >= LOLLIPOP_MR1) {
				addInjector(new GraphicsStatsStub());
				addInjector(new UsageStatsManagerStub());
			}
			if (Build.VERSION.SDK_INT >= M) {
				addInjector(new FingerprintManagerStub());
				addInjector(new NetworkManagementStub());
				addInjector(new NetworkScoreManagerStub());
			}
			if (Build.VERSION.SDK_INT >= N) {
                addInjector(new WifiScannerStub());
                addInjector(new ShortcutServiceStub());
                addInjector(new DevicePolicyManagerStub());
                addInjector(new UpdateEngineStub());
            }
            if (Build.VERSION.SDK_INT >= O){
				addInjector(new AutoFillManagerStub());
			}
		}
	}

	private void addInjector(IInjector IInjector) {
		mInjectors.put(IInjector.getClass(), IInjector);
	}

	public <T extends IInjector> T findInjector(Class<T> clazz) {
		// noinspection unchecked
		return (T) mInjectors.get(clazz);
	}

	public <T extends IInjector> void checkEnv(Class<T> clazz) {
		IInjector IInjector = findInjector(clazz);
		if (IInjector != null && IInjector.isEnvBad()) {
			try {
				IInjector.inject();
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}
	}

	public <T extends IInjector, H extends MethodInvocationStub> H getInvocationStub(Class<T> injectorClass) {
		T injector = findInjector(injectorClass);
		if (injector != null && injector instanceof MethodInvocationProxy) {
			// noinspection unchecked
			return (H) ((MethodInvocationProxy) injector).getInvocationStub();
		}
		return null;
	}

}