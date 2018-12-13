package com.polestar.welive;

import android.annotation.TargetApi;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;

import com.polestar.booster.Booster;
import com.polestar.booster.BoosterLog;

/**
 * Created by guojia on 2018/12/12.
 */
@TargetApi(21)
public class WeLiveJobService extends JobService {
    public WeLiveJobService() {
        super();
    }

    public boolean onStartJob(JobParameters jobParameters) {
        BoosterLog.log("onStartJob " + jobParameters);
        String src = null;
        switch(jobParameters.getJobId()) {
            case 1: {
                src = ("job_periodic");
                break;
            }
            case 2: {
                src = ("job_charging");
                break;
            }
            case 3: {
                src = ("job_idle");
                break;
            }
            case 4: {
                src = ("job_network");
                break;
            }
        }
        Booster.wake(this, src);
        jobFinished(jobParameters, false);
        return false;
    }

    public boolean onStopJob(JobParameters arg2) {
        return false;
    }
}
