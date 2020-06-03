package com.brianhoang.recordvideo.utils;

import android.util.Log;

import java.util.Timer;
import java.util.TimerTask;

public class ProgressUpdate {
    public static final String TAG = "ProgressUpdate";

    private Timer timer;

    public synchronized void startUpdate(TimerTask timerTask, int interval) {
        timer = new Timer();
        Log.e(TAG, "startUpdates");
        timer.scheduleAtFixedRate(timerTask, 0, interval);
    }

    public synchronized void stopUpdate() {
        Log.e(TAG, "stopUpdates");
        timer.cancel();
    }
}
