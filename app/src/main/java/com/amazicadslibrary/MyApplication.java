package com.amazicadslibrary;

import com.amazic.ads.billing.AppPurchase;
import com.amazic.ads.util.AppOpenManager;
import com.amazic.ads.util.AdsApplication;
import com.amazic.ads.util.AppUtil;

import java.util.List;

public class MyApplication extends AdsApplication {
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
        return getString(R.string.Admob_app_open_ad_id);
    }

    @Override
    public Boolean buildDebug() {
        return true;
    }
}
