package mirror.com.android.internal.app;

import android.os.IBinder;

import mirror.MethodParams;
import mirror.RefClass;
import mirror.RefStaticMethod;

/**
 * Created by guojia on 2019/3/30.
 */

public class ISmtOpsService {
    public static class Stub {
        public static Class TYPE =RefClass.load(Stub.class, "com.android.internal.app.ISmtOpsService$Stub");

        @MethodParams(value={IBinder.class}) public static RefStaticMethod asInterface;
    }

    public static Class TYPE = RefClass.load(ISmtOpsService.class, "com.android.internal.app.ISmtOpsService");
}
