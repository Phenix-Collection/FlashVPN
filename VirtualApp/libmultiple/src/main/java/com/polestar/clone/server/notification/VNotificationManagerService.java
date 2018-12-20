package com.polestar.clone.server.notification;

import android.annotation.TargetApi;
import android.app.NotificationChannel;
import android.app.NotificationChannelGroup;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.os.Parcel;
import android.text.TextUtils;

import com.polestar.clone.helper.utils.Reflect;
import com.polestar.clone.helper.utils.VLog;
import com.polestar.clone.remote.VParceledListSlice;
import com.polestar.clone.server.INotificationManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class VNotificationManagerService extends INotificationManager.Stub {
    private static final AtomicReference<VNotificationManagerService> gService = new AtomicReference<>();
    private NotificationManager mNotificationManager;
    static final String TAG = NotificationCompat.class.getSimpleName();
    private final List<String> mDisables = new ArrayList<>();
    //VApp's Notifications
    private final HashMap<String, List<NotificationInfo>> mNotifications = new HashMap<>();
    private Context mContext;

    private VNotificationManagerService(Context context) {
        mContext = context;
        mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    public static void systemReady(Context context) {
        VNotificationManagerService instance = new VNotificationManagerService(context);
        gService.set(instance);
    }

    public static VNotificationManagerService get() {
        return gService.get();
    }

    /***
     * fake notification's id
     *
     * @param id          notification's id
     * @param packageName notification's package
     * @param userId      user
     * @return
     */
    @Override
    public int dealNotificationId(int id, String packageName, String tag, int userId) {
        return id;
    }

    /***
     * fake notification's tag
     *
     * @param id          notification's id
     * @param packageName notification's package
     * @param tag         notification's tag
     * @param userId      user
     * @return
     */
    @Override
    public String dealNotificationTag(int id, String packageName, String tag, int userId) {
        if (TextUtils.equals(mContext.getPackageName(), packageName)) {
            return tag;
        }
        if (tag == null) {
            return packageName + "@" + userId;
        }
        return packageName + ":" + tag + "@" + userId;
    }

    @Override
    public boolean areNotificationsEnabledForPackage(String packageName, int userId) {
        return !mDisables.contains(packageName + ":" + userId);
    }

    @Override
    public void setNotificationsEnabledForPackage(String packageName, boolean enable, int userId) {
        String key = packageName + ":" + userId;
        if (enable) {
            if (mDisables.contains(key)) {
                mDisables.remove(key);
            }
        } else {
            if (!mDisables.contains(key)) {
                mDisables.add(key);
            }
        }
        //TODO: save mDisables ?
    }

    @Override
    public void addNotification(int id, String tag, String packageName, int userId) {
        NotificationInfo notificationInfo = new NotificationInfo(id, tag, packageName, userId);
        synchronized (mNotifications) {
            List<NotificationInfo> list = mNotifications.get(packageName);
            if (list == null) {
                list = new ArrayList<>();
                mNotifications.put(packageName, list);
            }
            if (!list.contains(notificationInfo)) {
                list.add(notificationInfo);
            }
        }
    }

    @Override
    public void cancelAllNotification(String packageName, int userId) {
        List<NotificationInfo> infos = new ArrayList<>();
        synchronized (mNotifications) {
            List<NotificationInfo> list = mNotifications.get(packageName);
            if (list != null) {
                int count = list.size();
                for (int i = count - 1; i >= 0; i--) {
                    NotificationInfo info = list.get(i);
                    if (info.userId == userId) {
                        infos.add(info);
                        list.remove(i);
                    }
                }
            }
        }
        for (NotificationInfo info : infos) {
            VLog.d(TAG, "cancel " + info.tag + " " + info.id);
            mNotificationManager.cancel(info.tag, info.id);
        }
    }

    @Override
    @TargetApi(Build.VERSION_CODES.O)
    public void createNotificationChannelGroups(String pkg, VParceledListSlice channelGroupList) {
        List<NotificationChannelGroup> channelGroups = channelGroupList.getList();
        List<NotificationChannelGroup> newGroups = fixupNotificationChannelGroups(channelGroups, pkg);
        mNotificationManager.createNotificationChannelGroups(newGroups);
    }

    @Override
    @TargetApi(Build.VERSION_CODES.O)
    public void createNotificationChannels(String pkg, VParceledListSlice channelsList) {
        List<NotificationChannel> channels = channelsList.getList();
        List<NotificationChannel> newChannels = fixupNotificationChannels(channels, pkg);
        mNotificationManager.createNotificationChannels(newChannels);
    }

    @Override
    @TargetApi(Build.VERSION_CODES.O)
    public NotificationChannel getNotificationChannel(String pkg, String channelId) {
        NotificationChannel channel = mNotificationManager.getNotificationChannel(fixupId(channelId, pkg));
        return unFixupNotificationChannel(channel, pkg);
    }

    @Override
    @TargetApi(Build.VERSION_CODES.O)
    public void deleteNotificationChannel(String pkg, String channelId) {
        mNotificationManager.deleteNotificationChannel(fixupId(channelId, pkg));
    }

    @Override
    @TargetApi(Build.VERSION_CODES.O)
    public VParceledListSlice getNotificationChannels(String pkg) {
        List<NotificationChannel> channels = mNotificationManager.getNotificationChannels();
        ArrayList<NotificationChannel> pkgChannels = new ArrayList<>();
        for (NotificationChannel c : channels) {
            if (c.getId().startsWith(pkg))
                pkgChannels.add(c);
        }
        return new VParceledListSlice(unFixupNotificationChannels(pkgChannels, pkg));
    }

    @Override
    @TargetApi(Build.VERSION_CODES.O)
    public void deleteNotificationChannelGroup(String pkg, String channelGroupId) {
        mNotificationManager.deleteNotificationChannelGroup(fixupId(channelGroupId, pkg));
    }

    @Override
    @TargetApi(Build.VERSION_CODES.O)
    public VParceledListSlice getNotificationChannelGroups(String pkg) {
        List<NotificationChannelGroup> groups = mNotificationManager.getNotificationChannelGroups();
        ArrayList<NotificationChannelGroup> pkgGroups = new ArrayList<>();
        for (NotificationChannelGroup g : groups) {
            if (g.getId().startsWith(pkg))
                pkgGroups.add(g);
        }
        return new VParceledListSlice(unFixupNotificationChannelGroups(pkgGroups, pkg));
    }

    private String fixupId(String id, String pkg) {
        // TODO check the id length
        String newId =  pkg + "@";
        if (id != null)
            newId += id;
        return newId;
    }

    private String unFixupId(String id, String pkg) {
        if (id == null || !id.startsWith(pkg))
            return id;
        // TODO check the id length
        String newId = id.substring(pkg.length() + 1);
        if (newId.length() == 0)
            return null;
        return newId;
    }

    @TargetApi(Build.VERSION_CODES.O)
    private NotificationChannel replaceNotificationChannelId(NotificationChannel channel, String newId) {
        Parcel in = Parcel.obtain();
        Parcel out = Parcel.obtain();

        channel.writeToParcel(in, 0);
        in.setDataPosition(0);
        if (in.readByte() != 0) {
            String dummy = in.readString();
        }
        if (newId != null) {
            out.writeByte((byte) 1);
            out.writeString(newId);
        } else {
            out.writeByte((byte) 0);
        }
        out.appendFrom(in, in.dataPosition(), in.dataAvail());
        out.setDataPosition(0);
        NotificationChannel newChannel = Reflect.on(NotificationChannel.class).create(out).get();
        in.recycle();
        out.recycle();
        return newChannel;
    }

    @TargetApi(Build.VERSION_CODES.O)
    private NotificationChannel fixupNotificationChannel(NotificationChannel channel, String pkg) {
        if (channel == null)
            return null;

        String id = channel.getId();
        String newId = fixupId(id, pkg);

        // fixup group id
        String groupId = channel.getGroup();
        if (groupId != null)
            channel.setGroup(fixupId(groupId, pkg));

        return replaceNotificationChannelId(channel, newId);
    }

    @TargetApi(Build.VERSION_CODES.O)
    private NotificationChannel unFixupNotificationChannel(NotificationChannel channel, String pkg) {
        if (channel == null)
            return null;

        String id = channel.getId();
        String newId = unFixupId(id, pkg);

        // unfixup group id
        String groupId = channel.getGroup();
        channel.setGroup(unFixupId(groupId, pkg));

        return replaceNotificationChannelId(channel, newId);
    }

    @TargetApi(Build.VERSION_CODES.O)
    private List<NotificationChannel> fixupNotificationChannels(List<NotificationChannel> channels, String pkg) {
        if (channels == null || channels.size() == 0)
            return channels;

        ArrayList<NotificationChannel> newChannels = new ArrayList<>();
        for (NotificationChannel channel : channels) {
            newChannels.add(fixupNotificationChannel(channel, pkg));
        }
        return newChannels;
    }

    @TargetApi(Build.VERSION_CODES.O)
    private List<NotificationChannel> unFixupNotificationChannels(List<NotificationChannel> channels, String pkg) {
        if (channels == null || channels.size() == 0)
            return channels;

        ArrayList<NotificationChannel> newChannels = new ArrayList<>();
        for (NotificationChannel channel : channels) {
            newChannels.add(unFixupNotificationChannel(channel, pkg));
        }
        return newChannels;
    }

    @TargetApi(Build.VERSION_CODES.O)
    private NotificationChannelGroup fixupNotificationChannelGroup(NotificationChannelGroup group, String pkg) {
        if (group == null)
            return null;

        String id = group.getId();
        CharSequence name = group.getName();
        List<NotificationChannel> channels = group.getChannels();
        NotificationChannelGroup newGroup = new NotificationChannelGroup(fixupId(id, pkg), name);
        for (NotificationChannel channel : channels) {
            NotificationChannel c = fixupNotificationChannel(channel, pkg);
            Reflect.on(newGroup).call("addChannel", c);
        }
        return newGroup;
    }

    @TargetApi(Build.VERSION_CODES.O)
    private NotificationChannelGroup unFixupNotificationChannelGroup(NotificationChannelGroup group, String pkg) {
        if (group == null)
            return null;

        String id = group.getId();
        CharSequence name = group.getName();
        List<NotificationChannel> channels = group.getChannels();
        NotificationChannelGroup newGroup = new NotificationChannelGroup(unFixupId(id, pkg), name);
        for (NotificationChannel channel : channels) {
            NotificationChannel c = unFixupNotificationChannel(channel, pkg);
            Reflect.on(newGroup).call("addChannel", c);
        }
        return newGroup;
    }

    @TargetApi(Build.VERSION_CODES.O)
    private List<NotificationChannelGroup> fixupNotificationChannelGroups(List<NotificationChannelGroup> groups, String pkg) {
        if (groups == null || groups.size() == 0)
            return groups;

        ArrayList<NotificationChannelGroup> newGroups = new ArrayList<>();
        for (NotificationChannelGroup group : groups) {
            newGroups.add(fixupNotificationChannelGroup(group, pkg));
        }
        return newGroups;
    }

    @TargetApi(Build.VERSION_CODES.O)
    private List<NotificationChannelGroup> unFixupNotificationChannelGroups(List<NotificationChannelGroup> groups, String pkg) {
        if (groups == null || groups.size() == 0)
            return groups;

        ArrayList<NotificationChannelGroup> newGroups = new ArrayList<>();
        for (NotificationChannelGroup group : groups) {
            newGroups.add(unFixupNotificationChannelGroup(group, pkg));
        }
        return newGroups;
    }

    private static class NotificationInfo {
        int id;
        String tag;
        String packageName;
        int userId;

        NotificationInfo(int id, String tag, String packageName, int userId) {
            this.id = id;
            this.tag = tag;
            this.packageName = packageName;
            this.userId = userId;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof NotificationInfo) {
                NotificationInfo that = (NotificationInfo) obj;
                return that.id == id && TextUtils.equals(that.tag, tag)
                        && TextUtils.equals(packageName, that.packageName)
                        && that.userId == userId;
            }
            return super.equals(obj);
        }
    }

}
