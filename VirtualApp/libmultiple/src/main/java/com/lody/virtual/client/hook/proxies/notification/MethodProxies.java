package com.lody.virtual.client.hook.proxies.notification;

import android.annotation.TargetApi;
import android.app.Notification;
import android.os.Build;

import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.client.hook.base.MethodProxy;
import com.lody.virtual.client.hook.utils.MethodParameterUtils;
import com.lody.virtual.client.ipc.VNotificationManager;
import com.lody.virtual.helper.compat.ParceledListSliceCompat;
import com.lody.virtual.helper.utils.ArrayUtils;
import com.lody.virtual.os.VUserHandle;
import com.lody.virtual.remote.VParceledListSlice;

import java.lang.reflect.Method;
import java.util.List;

import mirror.android.content.pm.ParceledListSlice;

/**
 * @author Lody
 */

@SuppressWarnings("unused")
class MethodProxies {

    static class EnqueueNotification extends MethodProxy {

        @Override
        public String getMethodName() {
            return "enqueueNotification";
        }

        @Override
        public Object call(Object who, Method method, Object... args) throws Throwable {
            String pkg = (String) args[0];
            if (getHostPkg().equals(pkg)) {
                return method.invoke(who, args);
            }
            if (!VirtualCore.get().getComponentDelegate().isNotificationEnabled(pkg, VUserHandle.myUserId())){
                return null;
            }
            int notificationIndex = ArrayUtils.indexOfFirst(args, Notification.class);
            int idIndex = ArrayUtils.indexOfFirst(args, Integer.class);
            int id = (int) args[idIndex];
            id = VNotificationManager.get().dealNotificationId(id, pkg, null, getAppUserId());
            args[idIndex] = id;
            Notification notification = (Notification) args[notificationIndex];
            if (!VNotificationManager.get().dealNotification(id, notification, pkg)) {
                return 0;
            }
            VNotificationManager.get().addNotification(id, null, pkg, getAppUserId());
            args[0] = getHostPkg();
            return method.invoke(who, args);
        }
    }

    /* package */ static class EnqueueNotificationWithTag extends MethodProxy {

        @Override
        public String getMethodName() {
            return "enqueueNotificationWithTag";
        }

        @Override
        public Object call(Object who, Method method, Object... args) throws Throwable {
            String pkg = (String) args[0];
            if (getHostPkg().equals(pkg)) {
                return method.invoke(who, args);
            }
            if (!VirtualCore.get().getComponentDelegate().isNotificationEnabled(pkg, VUserHandle.myUserId())){
                return null;
            }
            int notificationIndex = ArrayUtils.indexOfFirst(args, Notification.class);
            int idIndex = ArrayUtils.indexOfFirst(args, Integer.class);
            int tagIndex = (Build.VERSION.SDK_INT >= 18 ? 2 : 1);
            int id = (int) args[idIndex];
            String tag = (String) args[tagIndex];

            id = VNotificationManager.get().dealNotificationId(id, pkg, tag, getAppUserId());
            tag = VNotificationManager.get().dealNotificationTag(id, pkg, tag, getAppUserId());
            args[idIndex] = id;
            args[tagIndex] = tag;
            //key(tag,id)
            Notification notification = (Notification) args[notificationIndex];
            if (!VNotificationManager.get().dealNotification(id, notification, pkg)) {
                return 0;
            }
            VNotificationManager.get().addNotification(id, tag, pkg, getAppUserId());
            args[0] = getHostPkg();
            if (Build.VERSION.SDK_INT >= 18 && args[1] instanceof String) {
                args[1] = getHostPkg();
            }
            return method.invoke(who, args);
        }
    }

    /* package */ static class EnqueueNotificationWithTagPriority extends EnqueueNotificationWithTag {

        @Override
        public String getMethodName() {
            return "enqueueNotificationWithTagPriority";
        }
    }

    /* package */ static class CancelNotificationWithTag extends MethodProxy {

        @Override
        public String getMethodName() {
            return "cancelNotificationWithTag";
        }

        @Override
        public Object call(Object who, Method method, Object... args) throws Throwable {
            String pkg = MethodParameterUtils.replaceFirstAppPkg(args);
            if (getHostPkg().equals(pkg)) {
                return method.invoke(who, args);
            }
            String tag = (String) args[1];
            int id = (int) args[2];
            id = VNotificationManager.get().dealNotificationId(id, pkg, tag, getAppUserId());
            tag = VNotificationManager.get().dealNotificationTag(id, pkg, tag, getAppUserId());

            args[1] = tag;
            args[2] = id;
            return method.invoke(who, args);
        }
    }

    /**
     * @author Lody
     */
    /* package */ static class CancelAllNotifications extends MethodProxy {

        @Override
        public String getMethodName() {
            return "cancelAllNotifications";
        }

        @Override
        public Object call(Object who, Method method, Object... args) throws Throwable {
            String pkg = MethodParameterUtils.replaceFirstAppPkg(args);
            if (VirtualCore.get().isAppInstalled(pkg)) {
                VNotificationManager.get().cancelAllNotification(pkg, getAppUserId());
                return 0;
            }
            return method.invoke(who, args);
        }
    }

    static class AreNotificationsEnabledForPackage extends MethodProxy {
        @Override
        public String getMethodName() {
            return "areNotificationsEnabledForPackage";
        }

        @Override
        public Object call(Object who, Method method, Object... args) throws Throwable {
            String pkg = (String) args[0];
            if (getHostPkg().equals(pkg)) {
                return method.invoke(who, args);
            }
            return VNotificationManager.get().areNotificationsEnabledForPackage(pkg, getAppUserId());
        }
    }

    static class SetNotificationsEnabledForPackage extends MethodProxy {
        @Override
        public String getMethodName() {
            return "setNotificationsEnabledForPackage";
        }

        @Override
        public Object call(Object who, Method method, Object... args) throws Throwable {
            String pkg = (String) args[0];
            if (getHostPkg().equals(pkg)) {
                return method.invoke(who, args);
            }
            int enableIndex = ArrayUtils.indexOfFirst(args, Boolean.class);
            boolean enable = (boolean) args[enableIndex];
            VNotificationManager.get().setNotificationsEnabledForPackage(pkg, enable, getAppUserId());
            return 0;
        }
    }

    // void createNotificationChannelGroups(String pkg, in ParceledListSlice channelGroupList);
    @TargetApi(Build.VERSION_CODES.O)
    static class CreateNotificationChannelGroups extends MethodProxy {
        @Override
        public String getMethodName() {
            return "createNotificationChannelGroups";
        }

        @Override
        public Object call(Object who, Method method, Object... args) throws Throwable {
            String pkg = (String) args[0];
            if (getHostPkg().equals(pkg)) {
                return method.invoke(who, args);
            }
            Object slice = args[1];
            VParceledListSlice list = new VParceledListSlice(ParceledListSlice.getList.call(slice));
            VNotificationManager.get().createNotificationChannelGroups(pkg, list);
            return 0;
        }
    }

    // void createNotificationChannels(String pkg, in ParceledListSlice channelsList);
    @TargetApi(Build.VERSION_CODES.O)
    static class CreateNotificationChannels extends MethodProxy {
        @Override
        public String getMethodName() {
            return "createNotificationChannels";
        }

        @Override
        public Object call(Object who, Method method, Object... args) throws Throwable {
            String pkg = (String) args[0];
            if (getHostPkg().equals(pkg)) {
                return method.invoke(who, args);
            }
            Object slice = args[1];
            VParceledListSlice list = new VParceledListSlice(ParceledListSlice.getList.call(slice));
            VNotificationManager.get().createNotificationChannels(pkg, list);
            return 0;
        }
    }

    // NotificationChannel getNotificationChannel(String pkg, String channelId);
    @TargetApi(Build.VERSION_CODES.O)
    static class GetNotificationChannel extends MethodProxy {
        @Override
        public String getMethodName() {
            return "getNotificationChannel";
        }

        @Override
        public Object call(Object who, Method method, Object... args) throws Throwable {
            String pkg = (String) args[0];
            if (getHostPkg().equals(pkg)) {
                return method.invoke(who, args);
            }
            String id = (String) args[1];
            return VNotificationManager.get().getNotificationChannel(pkg, id);
        }
    }

    // void deleteNotificationChannel(String pkg, String channelId);
    @TargetApi(Build.VERSION_CODES.O)
    static class DeleteNotificationChannel extends MethodProxy {
        @Override
        public String getMethodName() {
            return "deleteNotificationChannel";
        }

        @Override
        public Object call(Object who, Method method, Object... args) throws Throwable {
            String pkg = (String) args[0];
            if (getHostPkg().equals(pkg)) {
                return method.invoke(who, args);
            }
            String id = (String) args[1];
            VNotificationManager.get().deleteNotificationChannel(pkg, id);
            return 0;
        }
    }

    // ParceledListSlice getNotificationChannels(String pkg);
    @TargetApi(Build.VERSION_CODES.O)
    static class GetNotificationChannels extends MethodProxy {
        @Override
        public String getMethodName() {
            return "getNotificationChannels";
        }

        @Override
        public Object call(Object who, Method method, Object... args) throws Throwable {
            String pkg = (String) args[0];
            if (getHostPkg().equals(pkg)) {
                return method.invoke(who, args);
            }
            List list = VNotificationManager.get().getNotificationChannels(pkg).getList();
            return ParceledListSliceCompat.create(list);
        }
    }

    // void deleteNotificationChannelGroup(String pkg, String channelGroupId);
    @TargetApi(Build.VERSION_CODES.O)
    static class DeleteNotificationChannelGroup extends MethodProxy {
        @Override
        public String getMethodName() {
            return "deleteNotificationChannelGroup";
        }

        @Override
        public Object call(Object who, Method method, Object... args) throws Throwable {
            String pkg = (String) args[0];
            if (getHostPkg().equals(pkg)) {
                return method.invoke(who, args);
            }
            String id = (String) args[1];
            VNotificationManager.get().deleteNotificationChannelGroup(pkg, id);
            return 0;
        }
    }

    // ParceledListSlice getNotificationChannelGroups(String pkg);
    @TargetApi(Build.VERSION_CODES.O)
    static class GetNotificationChannelGroups extends MethodProxy {
        @Override
        public String getMethodName() {
            return "getNotificationChannelGroups";
        }

        @Override
        public Object call(Object who, Method method, Object... args) throws Throwable {
            String pkg = (String) args[0];
            if (getHostPkg().equals(pkg)) {
                return method.invoke(who, args);
            }
            List list = VNotificationManager.get().getNotificationChannelGroups(pkg).getList();
            return ParceledListSliceCompat.create(list);
        }
    }
}
