package com.polestar.clone.helper.compat;

import android.content.ContentProviderClient;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.SystemClock;

import com.polestar.clone.helper.utils.VLog;

/**
 * @author Lody
 */
public class ContentProviderCompat {

    public static Bundle call(Context context, Uri uri, String method, String arg, Bundle extras) {
        if (VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
            return context.getContentResolver().call(uri, method, arg, extras);
        }
        ContentProviderClient client = crazyAcquireContentProvider(context, uri);
        Bundle res = null;
        try {
            res = client.call(method, arg, extras);
        } catch (Exception e) {
            VLog.logbug("ContentProviderCompat", VLog.getStackTraceString(e));
        } finally {
            releaseQuietly(client);
        }
        return res;
    }


    private static ContentProviderClient acquireContentProviderClient(Context context, Uri uri) {
        if (VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            return context.getContentResolver().acquireUnstableContentProviderClient(uri);
        }
        return context.getContentResolver().acquireContentProviderClient(uri);
    }

    public static ContentProviderClient crazyAcquireContentProvider(Context context, Uri uri) {
        ContentProviderClient client = null;
        if (client == null) {
            int retry = 0;
            while (retry < 6 && client == null) {
                SystemClock.sleep(100);
                retry++;
                try {
                client = acquireContentProviderClient(context, uri);
                }catch (Exception e) {
                    VLog.logbug("ProviderCall", VLog.getStackTraceString(e));
                }
            }
        }
        return client;
    }

    public static ContentProviderClient crazyAcquireContentProvider(Context context, String name) {
        ContentProviderClient client = acquireContentProviderClient(context, name);
        if (client == null) {
            int retry = 0;
            while (retry < 5 && client == null) {
                SystemClock.sleep(100);
                retry++;
                try {
                client = acquireContentProviderClient(context, name);
                }catch (Exception e) {
                    VLog.logbug("ProviderCall", VLog.getStackTraceString(e));
                }

            }
        }
        return client;
    }

    private static ContentProviderClient acquireContentProviderClient(Context context, String name) {
        if (VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            return context.getContentResolver().acquireUnstableContentProviderClient(name);
        }
        return context.getContentResolver().acquireContentProviderClient(name);
    }

    public static void releaseQuietly(ContentProviderClient client) {
        if (client != null) {
            try {
                if (VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    client.close();
                } else {
                    client.release();
                }
            } catch (Exception ignored) {
            }
        }
    }
}