package com.polestar.clone.server.am;

import android.content.ComponentName;
import android.content.Intent;
import android.os.IBinder;

/**
 * @author Lody
 */

/* package */ class ActivityRecord {
	public TaskRecord task;
	public ComponentName component;
	public ComponentName caller;
	public IBinder token;
	public int userId;
	public ProcessRecord process;
	public int launchMode;
	public int flags;
	public boolean marked;
	public String affinity;
	public Intent intent;

	public ActivityRecord(TaskRecord task, ComponentName component, ComponentName caller, IBinder token,
						  int userId, ProcessRecord process, int launchMode, int flags, String affinity, Intent intent) {
		this.task = task;
		this.component = component;
		this.caller = caller;
		this.token = token;
		this.userId = userId;
		this.process = process;
		this.launchMode = launchMode;
		this.flags = flags;
		this.affinity = affinity;
		this.intent = intent;
	}

}
