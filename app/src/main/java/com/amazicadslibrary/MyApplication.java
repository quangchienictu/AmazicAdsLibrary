package com.amazicadslibrary;

import com.amazic.ads.billing.AppPurchase;
import com.amazic.ads.util.AppOpenManager;
import com.amazic.ads.util.AdsApplication;
import com.amazic.ads.util.AppUtil;

import java.util.ArrayList;
import java.util.List;

public class MyApplication extends AdsApplication {
    @Override
    public void onCreate() {
        super.onCreate();
        AppOpenManager.getInstance().disableAppResumeWithActivity(Splash.class);

        List idResume = new ArrayList();
        idResume.add("1");
        idResume.add("2");
        idResume.add("3");
        idResume.add("5");
        idResume.add("ca-app-pub-3940256099942544/3419835294");
        idResume.add("6");
        idResume.add("7");
        AppOpenManager.getInstance().init(this,idResume);
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
        return null;
    }

    @Override
    public Boolean buildDebug() {
        return true;
    }

    @Override
    public Boolean enableAdsResumeFloor() {
        return false;
    }
}
