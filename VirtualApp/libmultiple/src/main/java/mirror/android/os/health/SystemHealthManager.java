package mirror.android.os.health;

import android.annotation.TargetApi;
import mirror.RefClass;
import mirror.RefObject;

@TargetApi(value=24) public class SystemHealthManager {
    public static Class TYPE = RefClass.load(SystemHealthManager.class, android.os.health.SystemHealthManager.class);
    public static RefObject mBatteryStats;
}

