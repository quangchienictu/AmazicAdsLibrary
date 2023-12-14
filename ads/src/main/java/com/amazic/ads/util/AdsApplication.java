package com.amazic.ads.util;

import android.app.Application;
import android.util.Log;

import com.amazic.ads.service.AdmobApi;

import java.util.List;

public abstract class AdsApplication extends Application{
    @Override
    public void onCreate() {
        super.onCreate();
        AppUtil.BUILD_DEBUG = buildDebug();
        Log.i("Application", " run debug: " + AppUtil.BUILD_DEBUG);
//        Admob.getInstance().initAdmod(this, getListTestDeviceId());
//        if(enableAdsResume()) {
//            AppOpenManager.getInstance().init(this, getResumeAdId());
//        }
    }
    public abstract boolean enableAdsResume();

    public abstract List<String> getListTestDeviceId();

    public abstract String getResumeAdId();
    public abstract Boolean buildDebug();
}
