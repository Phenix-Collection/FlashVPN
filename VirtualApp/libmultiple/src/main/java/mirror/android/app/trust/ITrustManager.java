package mirror.android.app.trust;

import android.os.IBinder;
import android.os.IInterface;

import mirror.MethodParams;
import mirror.RefClass;
import mirror.RefStaticMethod;

/**
 * Created by guojia on 2019/4/1.
 */

public class ITrustManager {
    public static Class<?> TYPE = RefClass.load(ITrustManager.class, "android.app.trust.ITrustManager");

    public static class  Stub {
        public static Class<?> TYPE = RefClass.load(ITrustManager.Stub.class, "android.app.trust.ITrustManager$Stub");
        @MethodParams({IBinder.class})
        public static RefStaticMethod<IInterface> asInterface;
    }
}
