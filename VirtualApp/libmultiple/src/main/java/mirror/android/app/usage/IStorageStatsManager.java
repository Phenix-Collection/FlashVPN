package mirror.android.app.usage;

import android.os.IBinder;
import mirror.MethodParams;
import mirror.RefClass;
import mirror.RefStaticMethod;

public class IStorageStatsManager {
    public static class Stub {
        @MethodParams(value={IBinder.class}) public static RefStaticMethod asInterface;

        public static Class TYPE = RefClass.load(IStorageStatsManager.Stub.class, "android.app.usage.IStorageStatsManager$Stub");

        public Stub() {
            super();
        }
    }

    public static Class TYPE = RefClass.load(IStorageStatsManager.class, "android.app.usage.IStorageStatsManager");}