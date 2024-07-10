package com.amazic.ads.util.detect_test_ad;

import android.content.Context;
import android.content.SharedPreferences;

public class DetectTestAd {
    public static String testAd = "Test Ad";
    public static DetectTestAd INSTANCE;

    private boolean showAllAds = false;

    public void setShowAds() {
        showAllAds = true;
    }

    public static DetectTestAd getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new DetectTestAd();
        }
        return INSTANCE;
    }

    public void detectedTestAd(boolean showAds, Context context) {
        SharedPreferences.Editor editor = context.getSharedPreferences("MY_PRE", Context.MODE_PRIVATE).edit();
        editor.putBoolean(testAd, showAds);
        editor.apply();
    }

    public boolean isTestAd(Context context) {
        return context.getSharedPreferences("MY_PRE", Context.MODE_PRIVATE).getBoolean(testAd, false) || !showAllAds;
    }
}
