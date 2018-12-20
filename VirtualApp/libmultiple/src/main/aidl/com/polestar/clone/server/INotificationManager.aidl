// INotificationManager.aidl
package com.polestar.clone.server;

// Declare any non-default types here with import statements
import android.app.Notification;
import android.app.NotificationChannel;

import com.polestar.clone.remote.VParceledListSlice;

interface INotificationManager {
    int dealNotificationId(int id, String packageName, String tag, int userId);
    String dealNotificationTag(int id, String packageName, String tag, int userId);
    boolean areNotificationsEnabledForPackage(String packageName, int userId);
    void setNotificationsEnabledForPackage(String packageName, boolean enable, int userId);
    void addNotification(int id, String tag, String packageName, int userId);
    void cancelAllNotification(String packageName, int userId);
    void createNotificationChannelGroups(String pkg, in VParceledListSlice channelGroupList);
    void createNotificationChannels(String pkg, in VParceledListSlice channelsList);
    NotificationChannel getNotificationChannel(String pkg, String channelId);
    void deleteNotificationChannel(String pkg, String channelId);
    VParceledListSlice getNotificationChannels(String pkg);
    void deleteNotificationChannelGroup(String pkg, String channelGroupId);
    VParceledListSlice getNotificationChannelGroups(String pkg);
}
