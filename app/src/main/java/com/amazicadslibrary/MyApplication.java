package com.amazicadslibrary;

import androidx.annotation.NonNull;

import com.amazic.ads.event.AppsflyerEvent;
import com.amazic.ads.util.Admob;
import com.amazic.ads.util.AdsApplication;
import com.amazic.ads.util.AppOpenManager;

import java.util.List;

public class MyApplication extends AdsApplication {
    @Override
    public void onCreate() {
        super.onCreate();
        AppOpenManager.getInstance().disableAppResumeWithActivity(Splash.class);
        AppsflyerEvent.getInstance().init(this, "1233", true);
    }

    @Override
    public boolean setCallRemoteConfig() {
        return true;
    }

    @NonNull
    @Override
    public String getAppTokenAdjust() {
        return "null";
    }

    @NonNull
    @Override
    public String getFacebookID() {
        return "null";
    }

    @Override
    public Boolean buildDebug() {
        return true;
    }
}
