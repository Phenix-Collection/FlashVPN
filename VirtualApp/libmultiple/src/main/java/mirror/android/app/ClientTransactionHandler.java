package mirror.android.app;

import android.os.IBinder;
import mirror.MethodParams;
import mirror.RefClass;
import mirror.RefMethod;

public class ClientTransactionHandler {
    public static Class TYPE;
    @MethodParams(value={IBinder.class}) public static RefMethod getActivityClient;

    static {
        ClientTransactionHandler.TYPE = RefClass.load(ClientTransactionHandler.class, "android.app.ClientTransactionHandler");
    }

    public ClientTransactionHandler() {
        super();
    }
}
