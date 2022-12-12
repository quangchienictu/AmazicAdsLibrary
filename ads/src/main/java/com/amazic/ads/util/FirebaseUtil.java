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
        Log.e("logPaidAdImpression",adValue.getCurrencyCode()+"");
        logEventWithAds(context, (float) adValue.getValueMicros(), adValue.getPrecisionType(), adUnitId, mediationAdapterClassName,adValue.getCurrencyCode());
    }

    private static void logEventWithAds(Context context, float revenue, int precision, String adUnitId, String network,String CurrencyCode) {
        Log.d(TAG, String.format(
                "Paid event of value %.0f microcents in currency USD of precision %s%n occurred for ad unit %s from ad network %s.",
                revenue,
                precision,
                adUnitId,
                network));

        Bundle params = new Bundle(); // Log ad value in micros.
        params.putDouble("valuemicros", revenue/1000000.0);
        params.putString("currency", CurrencyCode);
        params.putInt("precision", precision);
        params.putString("adunitid", adUnitId);
        params.putString("network", network);

        // log revenue this a ad
        FirebaseAnalytics.getInstance(context).logEvent("paid_ad_impression_admob", params);

        //update revenue local
        AppUtil.currentTotalRevenue001Ad += revenue;
        SharePreferenceUtils.updateCurrentTotalRevenue001Ad(context, AppUtil.currentTotalRevenue001Ad);
        logTotalRevenue001Ad(context);
    }

    public static void logTotalRevenue001Ad(Context context) {
        float revenue = AppUtil.currentTotalRevenue001Ad;
        if (revenue / 1000000 >= 0.01) {
            AppUtil.currentTotalRevenue001Ad = 0;
            SharePreferenceUtils.updateCurrentTotalRevenue001Ad(context, 0);
            Bundle bundle = new Bundle();
            bundle.putFloat(FirebaseAnalytics.Param.VALUE, revenue / 1000000);
            bundle.putString(FirebaseAnalytics.Param.CURRENCY, "USD");
            FirebaseAnalytics.getInstance(context).logEvent("Daily_Ads_Revenue", bundle);
        }
    }

    public static void logTimeLoadAdsSplash(Context context,int timeLoad){
        Log.d(TAG, String.format(
                "Time load ads splash %s.",
                timeLoad));
        Bundle bundle = new Bundle();
        bundle.putString("time_load", String.valueOf(timeLoad));
        FirebaseAnalytics.getInstance(context).logEvent("event_time_load_ads_splash", bundle);
    }

}
