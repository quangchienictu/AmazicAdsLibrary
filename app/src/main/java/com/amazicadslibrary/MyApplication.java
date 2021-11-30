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
        //        AppOpenManager.getInstance().setSplashActivity(SplashActivity.class, AppOpenManager.AD_UNIT_ID_TEST, 10000);
        AppOpenManager.getInstance().disableAppResumeWithActivity(Splash.class);
        Admod.getInstance().setOpenActivityAfterShowInterAds(true);
//        Admod.getInstance().setNumToShowAds(3,3);
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
    public String getOpenAppAdId() {
        return AppOpenManager.AD_UNIT_ID_TEST;
    }
}
