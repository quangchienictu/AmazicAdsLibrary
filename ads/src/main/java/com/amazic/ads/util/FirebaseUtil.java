package com.amazic.ads.util;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.ads.AdValue;
import com.google.firebase.analytics.FirebaseAnalytics;

public class FirebaseUtil {
    private static final String TAG = "FirebaseAnalyticsUtil";

    public static void logClickAdsEvent(Context context, String adUnitId) {
        Log.d(TAG, String.format(
                "User click ad for ad unit %s.",
                adUnitId));
        Bundle bundle = new Bundle();
        bundle.putString("ad_unit_id", adUnitId);
        FirebaseAnalytics.getInstance(context).logEvent("event_user_click_ads", bundle);
    }

    public static void logPaidAdImpression(Context context, AdValue adValue, String adUnitId, String mediationAdapterClassName) {
        logEventWithAds(context, (float) adValue.getValueMicros(), adValue.getPrecisionType(), adUnitId, mediationAdapterClassName);
    }

    private static void logEventWithAds(Context context, float revenue, int precision, String adUnitId, String network) {
        Log.d(TAG, String.format(
                "Paid event of value %.0f microcents in currency USD of precision %s%n occurred for ad unit %s from ad network %s.",
                revenue,
                precision,
                adUnitId,
                network));

        Bundle params = new Bundle(); // Log ad value in micros.
        params.putDouble("valuemicros", revenue);
        params.putString("currency", "USD");
        params.putInt("precision", precision);
        params.putString("adunitid", adUnitId);
        params.putString("network", network);

        FirebaseAnalytics.getInstance(context).logEvent("paid_ad_impression", params);

    }

}
