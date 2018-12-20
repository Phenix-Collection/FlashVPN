package com.polestar.clone.server;

import android.app.job.JobInfo;
import android.app.job.JobWorkItem;

 /**
  * IPC interface that supports the app-facing {@link #JobScheduler} api.
  */
interface IJobScheduler {
    int schedule(in JobInfo job);
    int enqueue(in JobInfo job, in JobWorkItem work);
    void cancel(int jobId);
    void cancelAll();
    List<JobInfo> getAllPendingJobs();
}
