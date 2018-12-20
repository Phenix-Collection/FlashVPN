package com.polestar.clone.client.hook.providers;

import android.content.ContentValues;
import android.net.Uri;

import com.polestar.clone.helper.utils.VLog;

import com.polestar.clone.client.core.VirtualCore;
import com.polestar.clone.client.hook.base.MethodBox;

import java.lang.reflect.InvocationTargetException;

/**
 * @author Lody
 */

class DownloadProviderHook extends ExternalProviderHook {

    private static final String TAG = DownloadProviderHook.class.getSimpleName();

    private static final String COLUMN_NOTIFICATION_PACKAGE = "notificationpackage";
    private static final String COLUMN_IS_PUBLIC_API = "is_public_api";
    private static final String COLUMN_OTHER_UID = "otheruid";
    private static final String COLUMN_COOKIE_DATA = "cookiedata";
    private static final String COLUMN_NOTIFICATION_CLASS = "notificationclass";
    private static final String INSERT_KEY_PREFIX = "http_header_";


    private static final String[] ENFORCE_REMOVE_COLUMNS = {
            COLUMN_OTHER_UID,
            COLUMN_NOTIFICATION_CLASS
    };

    DownloadProviderHook(Object base) {
        super(base);
    }

    @Override
    public Uri insert(MethodBox methodBox, Uri url, ContentValues initialValues) throws InvocationTargetException {
        if (initialValues.containsKey(COLUMN_NOTIFICATION_PACKAGE)) {
            initialValues.put(COLUMN_NOTIFICATION_PACKAGE, VirtualCore.get().getHostPkg());
        }
        if (initialValues.containsKey(COLUMN_COOKIE_DATA)) {
            String cookie = initialValues.getAsString(COLUMN_COOKIE_DATA);
            initialValues.remove(COLUMN_COOKIE_DATA);
            // retrieve the next free INSERT_KEY_PREFIX
            int headerIndex = 0;
            while (initialValues.containsKey(INSERT_KEY_PREFIX + headerIndex)) {
                headerIndex++;
            }
            // add the cookie
            initialValues.put(INSERT_KEY_PREFIX + headerIndex, "Cookie" + ": " + cookie);
        }
        if (!initialValues.containsKey(COLUMN_IS_PUBLIC_API)) {
            initialValues.put(COLUMN_IS_PUBLIC_API, true);
        }
        for (String column : ENFORCE_REMOVE_COLUMNS) {
            if (initialValues.containsKey(column)) {
                initialValues.remove(column);
            }
        }
        try {
            return super.insert(methodBox, url, initialValues);
        } catch (Exception e) {
            VLog.logbug("DownloadProviderHook", VLog.getStackTraceString(e));
            return new Uri.Builder().appendPath("0").build();
        }
    }
}
