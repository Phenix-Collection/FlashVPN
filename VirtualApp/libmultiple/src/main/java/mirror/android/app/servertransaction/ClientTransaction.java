package mirror.android.app.servertransaction;

import android.os.IBinder;

import mirror.RefClass;
import mirror.RefObject;

/**
 * Created by guojia on 2019/2/25.
 */

public class ClientTransaction {
    public static Class TYPE;
    public static RefObject mActivityCallbacks;
    public static RefObject<IBinder> mActivityToken;
    public static RefObject mLifecycleStateRequest;

    static {
        ClientTransaction.TYPE = RefClass.load(ClientTransaction.class, "android.app.servertransaction.ClientTransaction");
    }

    public ClientTransaction() {
        super();
    }
}
