package com.brianhoang.recordvideo.utils;

import android.util.Log;

import java.util.Timer;
import java.util.TimerTask;

public class ProgressUpdate {
    public static final String TAG = "ProgressUpdate";
    private TimerTask timerTask;

    private Timer timer;
    private int intervalUpdate;

    public ProgressUpdate(TimerTask timerTask, int interval) {
        timer = new Timer();
        this.timerTask = timerTask;
        intervalUpdate = interval;
    }

    public synchronized void startUpdate() {
        Log.e(TAG, "startUpdates");
        timer.scheduleAtFixedRate(timerTask, 0, intervalUpdate);
    }

    public synchronized void stopUpdate() {
        Log.e(TAG, "stopUpdates");
        timer.cancel();
        timerTask.cancel();
    }
}
