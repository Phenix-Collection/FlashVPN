package mirror.com.android.internal.app;

import android.os.IBinder;
import mirror.MethodParams;
import mirror.RefClass;
import mirror.RefStaticMethod;

public class IBatteryStats {
    public static  class Stub {
        public static Class TYPE =RefClass.load(Stub.class, "com.android.internal.app.IBatteryStats$Stub");
        @MethodParams(value={IBinder.class}) public static RefStaticMethod asInterface;

    }

    public static Class TYPE = RefClass.load(IBatteryStats.class, "com.android.internal.app.IBatteryStats");
}

