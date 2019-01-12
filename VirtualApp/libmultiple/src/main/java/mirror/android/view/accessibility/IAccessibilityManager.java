package mirror.android.view.accessibility;

import android.os.IBinder;
import mirror.MethodParams;
import mirror.RefClass;
import mirror.RefStaticMethod;

public class IAccessibilityManager {
    static public class Stub {
        public static Class TYPE;
        @MethodParams(value={IBinder.class}) public static RefStaticMethod asInterface;

        static {
            Stub.TYPE = RefClass.load(Stub.class, "android.view.accessibility.IAccessibilityManager$Stub");
        }

        public Stub() {
            super();
        }
    }

    public static Class TYPE;

    static {
        IAccessibilityManager.TYPE = RefClass.load(IAccessibilityManager.class, "android.view.accessibility.IAccessibilityManager");
    }

    public IAccessibilityManager() {
        super();
    }
}

