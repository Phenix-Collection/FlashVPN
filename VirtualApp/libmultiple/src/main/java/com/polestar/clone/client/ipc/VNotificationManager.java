package com.polestar.clone.client.ipc;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.os.Build;
import android.os.IBinder;
import android.os.RemoteException;

import com.polestar.clone.client.core.VirtualCore;
import com.polestar.clone.remote.VParceledListSlice;
import com.polestar.clone.server.INotificationManager;
import com.polestar.clone.server.notification.NotificationCompat;

/**
 * Fake notification manager
 */
public class VNotificationManager {
    private static final VNotificationManager sInstance = new VNotificationManager();
    private final NotificationCompat mNotificationCompat;
    private INotificationManager mRemote;

    private VNotificationManager() {
        mNotificationCompat = NotificationCompat.create();
    }

    public static VNotificationManager get() {
        return sInstance;
    }

    public INotificationManager getService() {
        if (mRemote == null ||
                (!mRemote.asBinder().isBinderAlive() && !VirtualCore.get().isVAppProcess())) {
            synchronized (VNotificationManager.class) {
                final IBinder pmBinder = ServiceManagerNative.getService(ServiceManagerNative.NOTIFICATION);
                mRemote = INotificationManager.Stub.asInterface(pmBinder);
            }
        }
        return mRemote;
    }

    public void clearRemoteInterface() {
        mRemote = null;
    }

    public boolean dealNotification(int id, Notification notification, String packageName) {
        if (notification == null) return false;
        return VirtualCore.get().getHostPkg().equals(packageName)
                || mNotificationCompat.dealNotification(id, notification, packageName);
    }

    public int dealNotificationId(int id, String packageName, String tag, int userId) {
        try {
            return getService().dealNotificationId(id, packageName, tag, userId);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return id;
    }

    public String dealNotificationTag(int id, String packageName, String tag, int userId) {
        try {
            return getService().dealNotificationTag(id, packageName, tag, userId);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return tag;
    }

    public boolean areNotificationsEnabledForPackage(String packageName, int userId) {
        try {
            return getService().areNotificationsEnabledForPackage(packageName, userId);
        } catch (RemoteException e) {
            e.printStackTrace();
            return true;
        }
    }

    public void setNotificationsEnabledForPackage(String packageName, boolean enable, int userId) {
        try {
            getService().setNotificationsEnabledForPackage(packageName, enable, userId);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void addNotification(int id, String tag, String packageName, int userId) {
        try {
            getService().addNotification(id, tag, packageName, userId);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void cancelAllNotification(String packageName, int userId) {
        try {
            getService().cancelAllNotification(packageName, userId);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @TargetApi(Build.VERSION_CODES.O)
    public void createNotificationChannelGroups(String pkg, VParceledListSlice channelGroupList) {
        try {
            getService().createNotificationChannelGroups(pkg, channelGroupList);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @TargetApi(Build.VERSION_CODES.O)
    public void createNotificationChannels(String pkg, VParceledListSlice channelsList) {
        try {
            getService().createNotificationChannels(pkg, channelsList);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @TargetApi(Build.VERSION_CODES.O)
    public NotificationChannel getNotificationChannel(String pkg, String channelId) {
        try {
            return getService().getNotificationChannel(pkg, channelId);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }

    @TargetApi(Build.VERSION_CODES.O)
    public void deleteNotificationChannel(String pkg, String channelId) {
        try {
            getService().deleteNotificationChannel(pkg, channelId);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @TargetApi(Build.VERSION_CODES.O)
    public VParceledListSlice getNotificationChannels(String pkg) {
        try {
            return getService().getNotificationChannels(pkg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }

    @TargetApi(Build.VERSION_CODES.O)
    public void deleteNotificationChannelGroup(String pkg, String channelGroupId) {
        try {
            getService().deleteNotificationChannelGroup(pkg, channelGroupId);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @TargetApi(Build.VERSION_CODES.O)
    public VParceledListSlice getNotificationChannelGroups(String pkg) {
        try {
            return getService().getNotificationChannelGroups(pkg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }
}
