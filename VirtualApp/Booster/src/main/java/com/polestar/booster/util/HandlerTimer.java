package com.polestar.booster.util;


import android.os.Handler;

public class HandlerTimer {
    final Handler mHandler;
    final Task mTask;
    final long mInterval;
    boolean mRunning = false;
    final Runnable mTimerTask = new Runnable() {
        public void run() {
            boolean finish = false;

            try {
                finish = HandlerTimer.this.mTask != null? HandlerTimer.this.mTask.run():true;
            } finally {
                if(!finish) {
                    HandlerTimer.this.mHandler.postDelayed(this, HandlerTimer.this.mInterval);
                }

                HandlerTimer.this.mRunning = !finish;
            }

        }
    };

    public HandlerTimer(Handler handler, Task task, long interval) {
        this.mHandler = handler;
        this.mTask = task;
        this.mInterval = interval;
    }

    public void start(long triggerTime) {
        this.mHandler.removeCallbacks(this.mTimerTask);
        this.mHandler.postDelayed(this.mTimerTask, triggerTime);
        this.mRunning = true;
    }

    public void stop() {
        this.mHandler.removeCallbacks(this.mTimerTask);
        this.mRunning = false;
    }

    public boolean running() {
        return this.mRunning;
    }

    public interface Task {
        boolean run();
    }
}
