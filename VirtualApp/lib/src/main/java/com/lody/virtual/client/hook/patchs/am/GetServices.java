package com.lody.virtual.client.hook.patchs.am;

import android.app.ActivityManager;

import com.lody.virtual.client.hook.base.Hook;
import com.lody.virtual.client.ipc.VActivityManager;
import com.lody.virtual.helper.proto.VParceledListSlice;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Lody
 *
 * @see android.app.IActivityManager#getServices(int, int)
 *
 */
public class GetServices extends Hook {
	@Override
	public String getName() {
		return "getServices";
	}

	@Override
	public Object call(Object who, Method method, Object... args) throws Throwable {
		int maxNum = (int) args[0];
		int flags = (int) args[1];
		VParceledListSlice services = VActivityManager.get().getServices(maxNum, flags);
		if (services!=null) {
			return services.getList();
		} else {
			return new ArrayList<List<ActivityManager.RunningServiceInfo>>(0);
		}
	}

	@Override
	public boolean isEnable() {
		return isAppProcess();
	}
}
