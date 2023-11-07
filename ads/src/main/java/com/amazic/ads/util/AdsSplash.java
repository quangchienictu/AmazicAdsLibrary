package com.amazic.ads.util;

import android.app.Activity;
import android.util.Log;

import com.amazic.ads.callback.AdCallback;
import com.amazic.ads.callback.InterCallback;
import com.amazic.ads.service.AdmobApi;

import java.util.Random;

public class AdsSplash {
    private static final String TAG = "AdsSplash";
    static AdsSplash INSTANCE;
    boolean isShowOpenSplash = true;

    public static AdsSplash getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new AdsSplash();
        }
        return INSTANCE;
    }

    public boolean isShowOpenSplash() {
        return isShowOpenSplash;
    }

    public void setShowOpenSplash(int countRateOpenSplash) {
        isShowOpenSplash = isShowOpenSplash(countRateOpenSplash);
    }


    boolean isShowOpenSplash(int countRateOpenSplash) {
        int value = new Random().nextInt(100) + 1;
        Log.d(TAG, "isShowOpenSplash: " + value);
        return value < countRateOpenSplash;
    }

    public void showAdsSplashApi(Activity activity, AdCallback openCallback, InterCallback interCallback) {
        if (isShowOpenSplash)
            AdmobApi.getInstance().loadOpenAppAdSplashFloor(activity, openCallback);
        else
            AdmobApi.getInstance().loadInterAdSplashFloor(activity, 3000, 20000, interCallback, true);
    }
}
