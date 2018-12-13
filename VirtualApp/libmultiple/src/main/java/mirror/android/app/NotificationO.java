

/**
 * Created by guojia on 2018/12/13.
 */
package mirror.android.app;

        import android.app.Notification;
        import mirror.RefClass;
        import mirror.RefObject;

public class NotificationO {
    public static Class TYPE;
    public static RefObject mChannelId;

    static {
        NotificationO.TYPE = RefClass.load(NotificationO.class, Notification.class);
    }

    public NotificationO() {
        super();
    }
}
