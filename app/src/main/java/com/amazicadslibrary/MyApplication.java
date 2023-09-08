package com.amazicadslibrary;

import androidx.multidex.MultiDexApplication;

import com.amazic.ads.event.AppsflyerEvent;
import com.amazic.ads.util.AdsMultiDexApplication;
import com.amazic.ads.util.AppOpenManager;
import com.amazic.ads.util.AdsApplication;

import java.util.List;

public class MyApplication extends AdsMultiDexApplication {
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
