package com.example.chist.testprojectmosru.Application;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;

/**
 * Created by 1 on 27.02.2017.
 */
public class LaunchApplication extends Application implements Application.ActivityLifecycleCallbacks {

    private static LaunchApplication _instance;
    private static boolean isActivityVisible;
    private static Context ctxActivity = null;

    public static LaunchApplication getInstance() {
        if(_instance == null)
            _instance = new LaunchApplication();
        return _instance;
    }


    @Override public void onActivityCreated(Activity activity, Bundle savedInstanceState) {}
    @Override public void onActivityPaused(Activity activity) {}
    @Override public void onActivityResumed(Activity activity) {}
    @Override public void onActivitySaveInstanceState(Activity activity, Bundle outState) {}
    @Override public void onActivityDestroyed(Activity activity) {}

    @Override
    public void onActivityStarted(Activity activity) {
        isActivityVisible = true;
        ctxActivity = activity;
    }

    @Override
    public void onActivityStopped(Activity activity) {
        isActivityVisible = false;
        ctxActivity = null;
    }

    public boolean isActivityVisible() {
        return isActivityVisible;
    }
}
