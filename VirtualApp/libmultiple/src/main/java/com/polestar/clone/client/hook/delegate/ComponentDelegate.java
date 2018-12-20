package com.polestar.clone.client.hook.delegate;


import android.app.Application;
import android.content.Intent;

import android.app.Activity;

public interface ComponentDelegate {

    ComponentDelegate EMPTY = new ComponentDelegate() {
        @Override
        public boolean handleStartActivity(String name) {
            return false;
        }

        @Override
        public void beforeActivityCreate(Activity activity) {
            // Empty
        }

        @Override
        public void beforeActivityResume(String pkg, int userId) {
            // Empty
        }

        @Override
        public void beforeActivityPause(String pkg, int userId) {
            // Empty
        }

        @Override
        public void beforeActivityDestroy(Activity activity) {
            // Empty
        }

        @Override
        public void afterActivityCreate(Activity activity) {
            // Empty
        }

        @Override
        public void afterActivityResume(Activity activity) {
            // Empty
        }

        @Override
        public void afterActivityPause(Activity activity) {
            // Empty
        }

        @Override
        public void afterActivityDestroy(Activity activity) {
            // Empty
        }

        @Override
        public void onSendBroadcast(Intent intent) {
            // Empty
        }

        @Override
        public void beforeApplicationCreate(Application application) {
            // Empty
        }

        @Override
        public void afterApplicationCreate(Application application) {
            // Empty
        }

        @Override
        public boolean isNotificationEnabled(String pkg, int userId) {
            return false;
        }

        @Override
        public void reloadSetting(String lockKey, boolean adFree, long lockInterval, boolean quickSwitch) {

        }
    };

    void beforeApplicationCreate(Application application);

    void afterApplicationCreate(Application application);

    void beforeActivityCreate(Activity activity);

    void beforeActivityResume(String pkg, int userId);

    void beforeActivityPause(String pkg, int userId);

    void beforeActivityDestroy(Activity activity);

    void afterActivityCreate(Activity activity);

    void afterActivityResume(Activity activity);

    void afterActivityPause(Activity activity);

    void afterActivityDestroy(Activity activity);

    void onSendBroadcast(Intent intent);

    boolean isNotificationEnabled(String pkg, int userId);

    void reloadSetting(String lockKey, boolean adFree, long lockInterval, boolean quickSwitch);

    boolean handleStartActivity(String name);
}
