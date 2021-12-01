package com.amazic.ads.util;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.util.Log;

import java.util.List;

public abstract class AdsApplication extends Application{
    @Override
    public void onCreate() {
        super.onCreate();
        Admod.getInstance().initAdmod(this, getListTestDeviceId());
        if(enableAdsResume()) {
            AppOpenManager.getInstance().initAdmod(this, getResumeAdId());
        }
    }
    public abstract boolean enableAdsResume();

    public abstract List<String> getListTestDeviceId();

    public abstract String getResumeAdId();
}
