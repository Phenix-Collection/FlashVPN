package com.lody.virtual.client.hook.delegate;


import android.content.Intent;

import android.app.Activity;

public interface ComponentDelegate {

    ComponentDelegate EMPTY = new ComponentDelegate() {

        @Override
        public void beforeActivityCreate(Activity activity) {
            // Empty
        }

        @Override
        public void beforeActivityResume(String pkg) {
            // Empty
        }

        @Override
        public void beforeActivityPause(String pkg) {
            // Empty
        }

        @Override
        public void beforeActivityDestroy(Activity activity) {
            // Empty
        }

        @Override
        public void onSendBroadcast(Intent intent) {
            // Empty
        }

        @Override
        public boolean isNotificationEnabled(String pkg) {
            return false;
        }

        @Override
        public void reloadLockerSetting(String key, boolean adFree) {

        }
    };

    void beforeActivityCreate(Activity activity);

    void beforeActivityResume(String pkg);

    void beforeActivityPause(String pkg);

    void beforeActivityDestroy(Activity activity);

    void onSendBroadcast(Intent intent);

    boolean isNotificationEnabled(String pkg);

    void reloadLockerSetting(String newKey, boolean adFree);
}
