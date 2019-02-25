package mirror.android.app.servertransaction;

import android.content.Intent;

import mirror.RefClass;
import mirror.RefObject;

public class LaunchActivityItem {
    public static Class TYPE;
    public static RefObject mInfo;
    public static RefObject<Intent> mIntent;

    static {
        LaunchActivityItem.TYPE = RefClass.load(LaunchActivityItem.class, "android.app.servertransaction.LaunchActivityItem");
    }

    public LaunchActivityItem() {
        super();
    }
}
