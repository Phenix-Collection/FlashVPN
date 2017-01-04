package com.polestar.multiaccount.widgets.locker;

import java.util.HashMap;

public class AppLockWindowManager {
    private static AppLockWindowManager sInstance;
    private HashMap<String, AppLockWindow> mWindows;

    private AppLockWindowManager() {
        mWindows = new HashMap<>();
    }

    public static AppLockWindowManager getInstance() {
        if (sInstance == null) {
            sInstance = new AppLockWindowManager();
        }

        return sInstance;
    }

    public AppLockWindow get(String app) {
        return mWindows.get(app);
    }

    public void add(String app, AppLockWindow window) {
        mWindows.put(app, window);
    }

    public AppLockWindow remove(String app) {
        return mWindows.remove(app);
    }

    public void removeAll() {
        mWindows.clear();
    }
}