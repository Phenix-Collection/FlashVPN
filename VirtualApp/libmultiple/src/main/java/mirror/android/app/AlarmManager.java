package mirror.android.app;

import mirror.RefClass;
import mirror.RefInt;
import mirror.RefObject;

/**
 * Created by guojia on 2019/3/20.
 */

public class AlarmManager {
    public static Class TYPE;
    public static RefObject mService;
    public static RefInt mTargetSdkVersion;

    static {
        AlarmManager.TYPE = RefClass.load(AlarmManager.class, android.app.AlarmManager.class);
    }

    public AlarmManager() {
        super();
    }
}
