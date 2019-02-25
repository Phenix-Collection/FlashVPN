package mirror.android.content.pm;

import mirror.MethodReflectParams;
import mirror.RefClass;
import mirror.RefStaticMethod;

public class PackageParserPie {
    public static Class TYPE;
    @MethodReflectParams(value={"android.content.pm.PackageParser$Package", "boolean"}) public static RefStaticMethod collectCertificates;

    static {
        PackageParserPie.TYPE = RefClass.load(PackageParserPie.class, "android.content.pm.PackageParser");
    }

    public PackageParserPie() {
        super();
    }
}