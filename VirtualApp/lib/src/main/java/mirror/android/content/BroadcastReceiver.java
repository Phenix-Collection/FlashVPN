package mirror.android.content;


import mirror.RefClass;
import mirror.RefMethod;

/**
 * Created by doriscoco on 2017/2/21.
 */

public class BroadcastReceiver {
    public static Class<?> TYPE = RefClass.load(BroadcastReceiver.class, android.content.BroadcastReceiver.class);
    public static RefMethod<android.content.BroadcastReceiver.PendingResult> getPendingResult;
}
