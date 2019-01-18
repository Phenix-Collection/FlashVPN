package mirror.android.app;


import android.app.Notification;
import android.graphics.drawable.Icon;

import mirror.RefClass;
import mirror.RefObject;

public class NotificationM {
    // fix anti-virus Ikarus AndroidOS.AdDisplay.AdLock
    public static Class TYPE;
    public static RefObject mLargeIcon;
    public static RefObject mSmallIcon;

    static {
        NotificationM.TYPE = RefClass.load(NotificationM.class, Notification.class);
    }
}