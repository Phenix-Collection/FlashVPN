package com.polestar.clone.server.notification;

import android.annotation.TargetApi;
import android.app.Notification;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.RemoteViews;

import com.polestar.clone.BitmapUtils;
import com.polestar.clone.client.core.VirtualCore;
import com.polestar.clone.client.ipc.VNotificationManager;
import com.polestar.clone.helper.compat.NotificationChannelCompat;
import com.polestar.clone.helper.utils.Reflect;
import com.polestar.clone.helper.utils.VLog;
import com.polestar.clone.os.VUserHandle;

import mirror.android.app.NotificationM;
import mirror.android.app.NotificationO;

import static com.polestar.clone.os.VEnvironment.getPackageResourcePath;

/**
 * @author 247321543
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
/* package */ class NotificationCompatCompatV21 extends NotificationCompatCompatV14 {

    private static final String TAG = NotificationCompatCompatV21.class.getSimpleName();

    NotificationCompatCompatV21() {
        super();
    }
    private String fixupChannelId(String id, String pkg) {
        // TODO check the id length
        String newId =  pkg + "@";
        if (id != null)
            newId += id;
        return newId;
    }
    @Override
    public boolean dealNotification(int id, Notification notification, String packageName) {
        Context appContext = getAppContext(packageName);
        if(Build.VERSION.SDK_INT >= 26 ) {
            VLog.d(TAG,"dealNotification " + notification);
            if ((TextUtils.isEmpty(notification.getChannelId()))) {
                NotificationO.mChannelId.set(notification, NotificationChannelCompat.DEFAULT_CHANNEL_ID);
            } else {
                String channelId = (String) NotificationO.mChannelId.get(notification);
//                try {
                    NotificationO.mChannelId.set(notification, fixupChannelId(channelId, packageName));
//                }catch (Throwable ex) {
//                    ex.printStackTrace();
//                }

            }
        }
        return appContext == null? false: (resolveRemoteViews(appContext, packageName, notification)
                || resolveRemoteViews(appContext, packageName, notification.publicVersion));
    }

    private boolean resolveRemoteViews(Context appContext, String packageName, Notification notification) {
        if (notification == null) {
            return false;
        }
        String sourcePath = null;
        PackageInfo packageInfo = getPackageInfo(packageName);
        ApplicationInfo host = getHostContext().getApplicationInfo();
        if (packageInfo != null) {
            sourcePath = packageInfo.applicationInfo.sourceDir;
        }
        if (TextUtils.isEmpty(sourcePath)) {
            sourcePath = getPackageResourcePath(packageName).getAbsolutePath();
        }

        //Fix RemoteViews
        getNotificationFixer().fixNotificationRemoteViews(appContext, notification);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getNotificationFixer().fixIcon(notification.getSmallIcon(), appContext, packageInfo != null);
            getNotificationFixer().fixIcon(notification.getLargeIcon(), appContext, packageInfo != null);
        } else {
            getNotificationFixer().fixIconImage(appContext.getResources(), notification.contentView, false, notification);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (notification.getSmallIcon() == null) {
                NotificationM.mSmallIcon.set(notification,
                        Icon.createWithBitmap(BitmapUtils.getCustomIcon(VirtualCore.get().getContext(),
                                appContext.getPackageName(), VUserHandle.myUserId())));
            }
        } else {
            notification.icon = host.icon;
        }

        ApplicationInfo proxyApplicationInfo = new ApplicationInfo(host);

        proxyApplicationInfo.packageName = packageName;
        proxyApplicationInfo.publicSourceDir = sourcePath;
        proxyApplicationInfo.sourceDir = sourcePath;

        fixApplicationInfo(notification.tickerView, proxyApplicationInfo);
        fixApplicationInfo(notification.contentView, proxyApplicationInfo);
        fixApplicationInfo(notification.bigContentView, proxyApplicationInfo);
        fixApplicationInfo(notification.headsUpContentView, proxyApplicationInfo);
        Bundle bundle = Reflect.on(notification).get("extras");
        if (bundle != null) {
            bundle.putParcelable(EXTRA_BUILDER_APPLICATION_INFO, proxyApplicationInfo);
        }
        return true;
    }

    private ApplicationInfo getApplicationInfo(Notification notification) {
        ApplicationInfo ai = getApplicationInfo(notification.tickerView);
        if (ai != null) {
            return ai;
        }
        ai = getApplicationInfo(notification.contentView);
        if (ai != null) {
            return ai;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            ai = getApplicationInfo(notification.bigContentView);
            if (ai != null) {
                return ai;
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ai = getApplicationInfo(notification.headsUpContentView);
            if (ai != null) {
                return ai;
            }
        }
        return null;
    }

    private ApplicationInfo getApplicationInfo(RemoteViews remoteViews) {
        if (remoteViews != null) {
            return mirror.android.widget.RemoteViews.mApplication.get(remoteViews);
        }
        return null;
    }

    private void fixApplicationInfo(RemoteViews remoteViews, ApplicationInfo ai) {
        if (remoteViews != null) {
            mirror.android.widget.RemoteViews.mApplication.set(remoteViews, ai);
        }
    }
}
