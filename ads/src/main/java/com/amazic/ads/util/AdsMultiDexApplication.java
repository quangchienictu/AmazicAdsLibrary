package com.amazic.ads.util;

import android.util.Log;

import androidx.multidex.MultiDexApplication;

import com.amazic.ads.applovin.AppOpenMax;

import java.util.List;

public abstract class AdsMultiDexApplication extends MultiDexApplication {
    @Override
    public void onCreate() {
        super.onCreate();
        AppUtil.BUILD_DEBUG = buildDebug();

        Log.i("Application", " run debug: " + AppUtil.BUILD_DEBUG);
        Admob.getInstance().initAdmod(this, getListTestDeviceId());
        if(!enableAdsResumeFloor()){
            if(enableAdsResume()) {
                AppOpenManager.getInstance().init(this, getResumeAdId());
            }
        }else{
            AppOpenManager.getInstance().init(this, "");
        }
    }

    public abstract boolean enableAdsResume();

    public abstract List<String> getListTestDeviceId();

    public abstract String getResumeAdId();
    public abstract Boolean buildDebug();
    public abstract Boolean AppOpenIsAdmob();
    public abstract Boolean enableAdsResumeFloor();
}
