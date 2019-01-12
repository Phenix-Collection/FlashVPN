package mirror.com.android.internal.telephony;

import mirror.RefClass;

/**
 * Created by guojia on 2019/1/12.
 */

public class IHwTelephony {
    static public class Stub {
        public static Class TYPE;

        static {
            Stub.TYPE = RefClass.load(ITelephony.Stub.class, "com.android.internal.telephony.IHwTelephony$Stub");
        }

        public Stub() {
            super();
        }
    }

    public static Class TYPE;

    static {
        IHwTelephony.TYPE = RefClass.load(ITelephony.class, "com.android.internal.telephony.IHwTelephony");
    }

    public IHwTelephony() {
        super();
    }
}
