package mirror.android.app.usage;

import mirror.RefClass;
import mirror.RefConstructor;
import mirror.RefLong;

public class StorageStats {
    public static Class TYPE;
    public static RefLong cacheBytes;
    public static RefLong codeBytes;
    public static RefConstructor ctor;
    public static RefLong dataBytes;

    static {
        StorageStats.TYPE = RefClass.load(StorageStats.class, "android.app.usage.StorageStats");
    }

    public StorageStats() {
        super();
    }
}
