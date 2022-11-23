package com.amazic.ads.util;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import androidx.multidex.MultiDexApplication;

import java.util.List;

public abstract class AdsMultiDexApplication extends MultiDexApplication {
    @Override
    public void onCreate() {
        super.onCreate();
        AppUtil.BUILD_DEBUG = buildDebug();
        Log.i("Application", " run debug: " + AppUtil.BUILD_DEBUG );
        Admod.getInstance().initAdmod(this, getListTestDeviceId());
        if (enableAdsResume()) {
            if(Admod.isShowAllAds)
            AppOpenManager.getInstance().init(this, getOpenAppAdId());
        }
    }

    public abstract boolean enableAdsResume();

    public abstract List<String> getListTestDeviceId();

    public abstract String getOpenAppAdId();
    public abstract Boolean buildDebug();
}
