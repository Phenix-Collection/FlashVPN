package mirror.android.view;

import mirror.MethodReflectParams;
import mirror.RefClass;
import mirror.RefMethod;

/**
 * Created by guojia on 2019/3/20.
 */

public class DisplayAdjustments {
    public static Class Class;
    @MethodReflectParams(value={"android.content.res.CompatibilityInfo"}) public static RefMethod setCompatibilityInfo;

    static {
        DisplayAdjustments.Class = RefClass.load(DisplayAdjustments.class, "android.view.DisplayAdjustments");
    }

    public DisplayAdjustments() {
        super();
    }
}
