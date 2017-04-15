package com.lody.virtual.client.hook.patchs.am;

import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ComponentInfo;
import android.os.Build;
import android.os.IBinder;
import android.os.IInterface;

import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.client.env.Constants;
import com.lody.virtual.client.hook.base.Hook;
import com.lody.virtual.client.ipc.VActivityManager;
import com.lody.virtual.client.stub.StubPendingActivity;
import com.lody.virtual.client.stub.StubPendingReceiver;
import com.lody.virtual.client.stub.StubPendingService;
import com.lody.virtual.helper.compat.ActivityManagerCompat;
import com.lody.virtual.helper.utils.ArrayUtils;
import com.lody.virtual.helper.utils.VLog;
import com.lody.virtual.os.VUserHandle;

import java.lang.reflect.Method;

/**
 * @author Lody
 */
/* package */ class GetIntentSender extends Hook {

	private final static String TAG = "GetIntentSender";
	@Override
	public String getName() {
		return "getIntentSender";
	}

	@Override
	public Object call(Object who, Method method, Object... args) throws Throwable {
		String creator = (String) args[1];
		args[1] = getHostPkg();
		String[] resolvedTypes = (String[]) args[6];
		int indexToken = ArrayUtils.indexOfFirst(args, IBinder.class);
		IBinder token = indexToken == -1 ? null : (IBinder) args[indexToken];
		int type = (int) args[0];
		if (args[5] instanceof Intent[]) {
			Intent[] intents = (Intent[]) args[5];
			if (intents.length > 0) {
				Intent intent = intents[intents.length - 1];
				if (resolvedTypes != null && resolvedTypes.length > 0) {
					intent.setDataAndType(intent.getData(), resolvedTypes[resolvedTypes.length - 1]);
				}
				Intent proxyIntent = redirectIntentSender(type, creator, intent, token);
				if (proxyIntent != null) {
					intents[intents.length - 1] = proxyIntent;
				}
			}
		}
		if (args.length > 7 && args[7] instanceof Integer) {
            args[7] = PendingIntent.FLAG_UPDATE_CURRENT;
        }
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			if (args[args.length-1] instanceof Integer) {
				int userId = (int)args[args.length-1];
				if (userId == VUserHandle.USER_CURRENT) {
					args[args.length-1] = VUserHandle.USER_CURRENT_OR_SELF;
				}
			}
		}
//		if (resolvedTypes != null && resolvedTypes.length != 0) {
//			args[6] = new String[resolvedTypes.length];
//		} else {
//			args[6] = new String[] { null};
//		}
		IInterface sender = (IInterface) method.invoke(who, args);
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR2 && sender != null && creator != null) {
			VActivityManager.get().addPendingIntent(sender.asBinder(), creator);
		}
		return sender;
	}

	private Intent redirectIntentSender(int type, String creator, Intent intent, IBinder token) {
		Intent newIntent = intent.cloneFilter();
		boolean ok = false;

		switch (type) {
			case ActivityManagerCompat.INTENT_SENDER_ACTIVITY: {
				VLog.d(TAG, "INTENT_SENDER_ACTIVITY " + intent.toString());
				ComponentInfo info = VirtualCore.get().resolveActivityInfo(intent, VUserHandle.myUserId());
				if (info != null) {
					ok = true;
					newIntent.setClass(getHostContext(), StubPendingActivity.class);
					newIntent.setFlags(intent.getFlags());
//					newIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					if (token != null) {
						VLog.d(TAG, "token not null");
						try {
							ComponentName componentName = VActivityManager.get().getActivityForToken(token);
							if (componentName != null) {
								VLog.d(TAG, "component " + componentName.toString());
								newIntent.putExtra("_VA_|_caller_", componentName);
							}
						}catch (Exception e){
							VLog.logbug(TAG, VLog.getStackTraceString(e));
						}
					}
				}

			} break;

			case ActivityManagerCompat.INTENT_SENDER_ACTIVITY_RESULT: {
				VLog.d(TAG, "INTENT_SENDER_ACTIVITY_RESULT " + intent.toString());
				break;
			}

			case ActivityManagerCompat.INTENT_SENDER_SERVICE: {
				ComponentInfo info = VirtualCore.get().resolveServiceInfo(intent, VUserHandle.myUserId());
				VLog.d(TAG, "INTENT_SENDER_SERVICE " + intent.toString());
				if (info != null) {
					ok= true;
					newIntent.setClass(getHostContext(), StubPendingService.class);
				}

			} break;

			case ActivityManagerCompat.INTENT_SENDER_BROADCAST: {
				ok = true;
				VLog.d(TAG, "INTENT_SENDER_BROADCAST " + intent.toString());
				newIntent.setClass(getHostContext(), StubPendingReceiver.class);
			} break;

		}

		if (!ok) {
			return null;
		}

		newIntent.putExtra("_VA_|_user_id_", VUserHandle.myUserId());
		newIntent.putExtra("_VA_|_intent_", intent);
		newIntent.putExtra("_VA_|_creator_", creator);
		newIntent.putExtra("_VA_|_from_inner_", true);

		return newIntent;
	}

	@Override
	public boolean isEnable() {
		return isAppProcess();
	}

}
