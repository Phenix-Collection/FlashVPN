package com.lody.virtual.client.hook.patchs.notification;

import android.app.Notification;
import android.os.Build;

import com.lody.virtual.client.hook.base.Hook;
import com.lody.virtual.client.ipc.VNotificationManager;
import com.lody.virtual.server.notification.VNotificationManagerService;
import com.lody.virtual.helper.utils.ArrayUtils;
import com.lody.virtual.os.VUserHandle;

import java.lang.reflect.Method;

/**
 * @author Lody
 */
/* package */ class EnqueueNotification extends Hook {

	@Override
	public String getName() {
		return "enqueueNotification";
	}

	@Override
	public Object call(Object who, Method method, Object... args) throws Throwable {
        //enqueueNotification(pkg, id, notification, idOut);
		String pkg = (String) args[0];
		int notificationIndex = ArrayUtils.indexOfFirst(args, Notification.class);
        int idIndex = ArrayUtils.indexOfFirst(args, Integer.class);
        int id = (int) args[idIndex];
        id = VNotificationManager.get().dealNotificationId(id, pkg, null,getVUserId());
        args[idIndex] = id;
		Notification notification = (Notification) args[notificationIndex];
        if (!VNotificationManager.get().dealNotification(id, notification, pkg)) {
			return 0;
		}
        VNotificationManager.get().addNotification(id, null, pkg, getVUserId());
		args[0] = getHostPkg();
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			if(args[args.length - 1] instanceof Integer) {
				int userId = (int) args[args.length - 1];
				if (userId == VUserHandle.USER_ALL) {
					userId = VUserHandle.myUserId();
				}
				args[args.length - 1] = userId;
			}
		}
		return method.invoke(who, args);
	}
}
