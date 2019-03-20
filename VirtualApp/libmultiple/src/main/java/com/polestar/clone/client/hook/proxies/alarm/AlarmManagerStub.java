package com.polestar.clone.client.hook.proxies.alarm;

import android.content.Context;
import android.os.Build;
import android.os.WorkSource;

import com.polestar.clone.client.hook.base.MethodProxy;
import com.polestar.clone.client.hook.base.BinderInvocationProxy;
import com.polestar.clone.helper.utils.ArrayUtils;
import com.polestar.clone.helper.utils.VLog;

import java.lang.reflect.Method;

import mirror.android.app.IAlarmManager;

/**
 * @author Lody
 *
 * @see android.app.AlarmManager
 */

//interface IAlarmManager {
//31	/** windowLength == 0 means exact; windowLength < 0 means the let the OS decide */
//		32    void set(String callingPackage, int type, long triggerAtTime, long windowLength,
//33            long interval, int flags, in PendingIntent operation, in IAlarmListener listener,
//34            String listenerTag, in WorkSource workSource, in AlarmManager.AlarmClockInfo alarmClock);
//35    boolean setTime(long millis);
//36    void setTimeZone(String zone);
//37    void remove(in PendingIntent operation, in IAlarmListener listener);
//38    long getNextWakeFromIdleTime();
//39    AlarmManager.AlarmClockInfo getNextAlarmClock(int userId);
//40}
public class AlarmManagerStub extends BinderInvocationProxy {

	public AlarmManagerStub() {
		super(IAlarmManager.Stub.asInterface, Context.ALARM_SERVICE);
	}

	@Override
	protected void onBindMethods() {
		super.onBindMethods();
		addMethodProxy(new Set());
		addMethodProxy(new SetTime());
		addMethodProxy(new SetTimeZone());
		addMethodProxy(new Remove());
	}

	private static class Remove extends MethodProxy {
		@Override
		public String getMethodName() {
			return "remove";
		}

		@Override
		public Object call(Object who, Method method, Object... args) throws Throwable {
			try{
				super.call(who, method, args);
			}catch (Throwable ex) {
			}
			return null;
		}
	}

	private static class SetTimeZone extends MethodProxy {
		@Override
		public String getMethodName() {
			return "setTimeZone";
		}

		@Override
		public Object call(Object who, Method method, Object... args) throws Throwable {
			return null;
		}
	}

	private static class SetTime extends MethodProxy {
		@Override
		public String getMethodName() {
			return "setTime";
		}

		@Override
		public Object call(Object who, Method method, Object... args) throws Throwable {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
				return false;
			}
			return null;
		}
	}

	private static class Set extends MethodProxy {

        @Override
        public String getMethodName() {
            return "set";
        }

        @Override
        public boolean beforeCall(Object who, Method method, Object... args) {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && args[0] instanceof String) {
				args[0] = getHostPkg();
			}
            int index = ArrayUtils.indexOfFirst(args, WorkSource.class);
            if (index >= 0) {
                args[index] = null;
            }
            return true;
        }

		@Override
		public Object call(Object who, Method method, Object... args) throws Throwable {
			try {
				return super.call(who, method, args);
			} catch (Exception e) {
				return null;
			}
		}
	}
}
