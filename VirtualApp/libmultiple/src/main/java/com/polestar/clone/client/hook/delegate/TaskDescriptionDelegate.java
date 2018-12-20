package com.polestar.clone.client.hook.delegate;

import android.app.ActivityManager;

public interface TaskDescriptionDelegate {
    public ActivityManager.TaskDescription getTaskDescription(ActivityManager.TaskDescription oldTaskDescription);
}
