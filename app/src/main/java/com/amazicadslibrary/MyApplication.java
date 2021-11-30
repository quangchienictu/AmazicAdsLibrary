package com.amazicadslibrary;

import android.app.Application;

import com.amazic.ads.util.Admod;
import com.amazic.ads.util.AppOpenManager;
import com.amazic.ads.util.AsdApplication;

import java.util.List;

public class MyApplication extends AsdApplication {
    @Override
    public void onCreate() {
        super.onCreate();
        AppOpenManager.getInstance().disableAppResumeWithActivity(Splash.class);
    }

    @Override
    public boolean enableAdsResume() {
        return true;
    }

    @Override
    public List<String> getListTestDeviceId() {
        return null;
    }

    @Override
    public String getResumeAdId() {
        return getString(R.string.admod_app_open_ad_id);
    }
}
