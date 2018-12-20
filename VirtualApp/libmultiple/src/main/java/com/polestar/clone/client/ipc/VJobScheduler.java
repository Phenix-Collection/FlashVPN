package com.polestar.clone.client.ipc;

import android.app.job.JobInfo;
import android.app.job.JobWorkItem;
import android.os.IBinder;
import android.os.RemoteException;

import com.polestar.clone.client.core.VirtualCore;
import com.polestar.clone.client.env.VirtualRuntime;
import com.polestar.clone.server.IJobScheduler;

import java.util.List;

/**
 * @author Lody
 */

public class VJobScheduler {

    private static final VJobScheduler sInstance = new VJobScheduler();

    private IJobScheduler mRemote;

    public static VJobScheduler get() {
        return sInstance;
    }

    public IJobScheduler getRemote() {
        if (mRemote == null ||
                (!mRemote.asBinder().isBinderAlive() && !VirtualCore.get().isVAppProcess())) {
            synchronized (this) {
                Object remote = getRemoteInterface();
                mRemote = LocalProxyUtils.genProxy(IJobScheduler.class, remote);
            }
        }
        return mRemote;
    }

    public void clearRemoteInterface() {
        mRemote = null;
    }

    private Object getRemoteInterface() {
        final IBinder binder = ServiceManagerNative.getService(ServiceManagerNative.JOB);
        return IJobScheduler.Stub.asInterface(binder);
    }

    public int schedule(JobInfo job) {
        try {
            return getRemote().schedule(job);
        } catch (RemoteException e) {
            return VirtualRuntime.crash(e);
        }
    }

    public List<JobInfo> getAllPendingJobs() {
        try {
            return getRemote().getAllPendingJobs();
        } catch (RemoteException e) {
            return VirtualRuntime.crash(e);
        }
    }

    public void cancelAll() {
        try {
            getRemote().cancelAll();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void cancel(int jobId) {
        try {
            getRemote().cancel(jobId);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public int enqueue(JobInfo job, JobWorkItem work) {
        try {
            return getRemote().enqueue(job, work);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return 0;
    }
}
