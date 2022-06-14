package com.amazic.ads.util;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.google.firebase.analytics.FirebaseAnalytics;

public class FirebaseAnalyticsUtil {
    private static final String TAG = "FirebaseAnalyticsUtil";
    public static void logClickAdsEventIS(Context context, String adsName) {
        Log.d(TAG, String.format(
                "User click ad for ad unit %s.",
                adsName));
        Bundle bundle = new Bundle();
        bundle.putString("ad_type", adsName);
        FirebaseAnalytics.getInstance(context).logEvent("event_user_click_ads_is", bundle);
    }
}
