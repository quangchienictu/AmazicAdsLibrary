package com.amazicadslibrary;

import com.amazic.ads.billing.AppPurchase;
import com.amazic.ads.event.AppsflyerEvent;
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
        AppsflyerEvent.getInstance().init(this, "1233",true);

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
        return "ca-app-pub-3940256099942544/3419835294";
    }

    @Override
    public Boolean buildDebug() {
        return true;
    }


}
