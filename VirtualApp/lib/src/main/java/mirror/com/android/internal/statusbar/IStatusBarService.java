package mirror.com.android.internal.statusbar;

import android.os.IBinder;
import android.os.IInterface;

import mirror.MethodParams;
import mirror.RefClass;
import mirror.RefStaticMethod;

/**
 * Created by guojia on 2016/12/18.
 */

public class IStatusBarService {
    public static Class<?> TYPE = RefClass.load(IStatusBarService.class, "com.android.internal.statusbar.IStatusBarService");
    public static class Stub {
        public static Class<?> TYPE = RefClass.load(IStatusBarService.Stub.class, "com.android.internal.statusbar.IStatusBarService$Stub");
        @MethodParams({IBinder.class})
        public static RefStaticMethod<IInterface> asInterface;
    }
}
