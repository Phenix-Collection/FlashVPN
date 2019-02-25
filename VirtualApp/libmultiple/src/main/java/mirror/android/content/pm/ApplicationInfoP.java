package mirror.android.content.pm;

import android.content.pm.ApplicationInfo;
import mirror.RefClass;
import mirror.RefMethod;

public class ApplicationInfoP {
    public static Class TYPE;
    public static RefMethod setHiddenApiEnforcementPolicy;

    static {
        ApplicationInfoP.TYPE = RefClass.load(ApplicationInfoP.class, ApplicationInfo.class);
    }

    public ApplicationInfoP() {
        super();
    }
}