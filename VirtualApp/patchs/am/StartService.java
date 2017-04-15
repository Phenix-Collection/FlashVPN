package com.lody.virtual.client.hook.patchs.am;

import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.os.IInterface;

import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.client.hook.base.Hook;
import com.lody.virtual.client.ipc.VActivityManager;
import com.lody.virtual.client.stub.StubPendingService;
import com.lody.virtual.helper.utils.VLog;
import com.lody.virtual.os.VUserHandle;

import java.lang.reflect.Method;

import static mirror.android.app.ActivityThread.ActivityClientRecord.intent;

/**
 * @author Lody
 *
 */
/* package */ class StartService extends Hook {

	@Override
	public String getName() {
		return "startService";
	}

	@Override
	public Object call(Object who, Method method, Object... args) throws Throwable {
		IInterface appThread = (IInterface) args[0];
		Intent service = (Intent) args[1];
		String resolvedType = (String) args[2];
		if(service!=null) {
			VLog.d("StartService", "intent: " + service.toString());
		}
		if (service.getComponent() != null
				&& (!service.getComponent().getClassName().equals(StubPendingService.class.getName()))
				&& getHostPkg().equals(service.getComponent().getPackageName())) {
			// for server process
			return method.invoke(who, args);
		}
		int userId = VUserHandle.myUserId();
		boolean fromInner = false;
		try {
			fromInner = service.getBooleanExtra("_VA_|_from_inner_", false);
		}catch (Exception e) {
			VLog.logbug("StartService", VLog.getStackTraceString(e));
		}
		if (fromInner) {
			userId = service.getIntExtra("_VA_|_user_id_", userId);
			service = service.getParcelableExtra("_VA_|_intent_");
		} else {
			if (isServerProcess()) {
				try {
					userId = service.getIntExtra("_VA_|_user_id_", VUserHandle.USER_NULL);
				}catch (Exception e){
					VLog.logbug("StartService", VLog.getStackTraceString(e));
				}
			}
		}
		service.setDataAndType(service.getData(), resolvedType);
		ServiceInfo serviceInfo = VirtualCore.get().resolveServiceInfo(service, VUserHandle.myUserId());
		if (serviceInfo != null) {
			return VActivityManager.get().startService(appThread, service, resolvedType, userId);
		}
		return method.invoke(who, args);
	}

	@Override
	public boolean isEnable() {
		return isAppProcess() || isServerProcess();
	}
}
