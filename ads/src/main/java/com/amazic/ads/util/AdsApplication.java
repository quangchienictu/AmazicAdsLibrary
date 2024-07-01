package com.amazic.ads.util;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.adjust.sdk.Adjust;
import com.adjust.sdk.AdjustConfig;
import com.adjust.sdk.LogLevel;

public abstract class AdsApplication extends Application implements Application.ActivityLifecycleCallbacks {
    private static final String TAG = "AdsApplication";

    @Override
    public void onCreate() {
        super.onCreate();
        AppUtil.BUILD_DEBUG = buildDebug();
        Log.i("Application", " run debug: " + AppUtil.BUILD_DEBUG);
        setUpAdjust();
        Admob.getInstance().setContext(this);
        registerActivityLifecycleCallbacks(this);
    }

    private void setUpAdjust() {
        String environment;
        environment = AdjustConfig.ENVIRONMENT_PRODUCTION;
        AdjustConfig config = new AdjustConfig(this, getAppTokenAdjust(), environment);
        config.setLogLevel(LogLevel.VERBOSE);
        config.setFbAppId(getFacebookID());
        config.setDefaultTracker(getAppTokenAdjust());
        config.setSendInBackground(true);
        Adjust.onCreate(config);
        // Enable the SDK
        Adjust.setEnabled(true);
    }

    @Override
    public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {

    }

    @Override
    public void onActivityStarted(@NonNull Activity activity) {

    }

    @Override
    public void onActivityResumed(@NonNull Activity activity) {
        Adjust.onResume();
    }

    @Override
    public void onActivityPaused(@NonNull Activity activity) {
        Adjust.onPause();

    }

    @Override
    public void onActivityStopped(@NonNull Activity activity) {

    }

    @Override
    public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {

    }

    @Override
    public void onActivityDestroyed(@NonNull Activity activity) {

    }

    @NonNull
    public abstract String getAppTokenAdjust();

    @NonNull
    public abstract String getFacebookID();

    public abstract Boolean buildDebug();
}
