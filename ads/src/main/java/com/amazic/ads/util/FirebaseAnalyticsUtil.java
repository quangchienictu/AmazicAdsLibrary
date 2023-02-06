package com.amazic.ads.util;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.ironsource.mediationsdk.impressionData.ImpressionData;

public class FirebaseAnalyticsUtil {
    public static String INTER = "INTER";
    public static String BANNER = "BANNER";
    public static String NATIVE = "NATIVE";
    public static String RESUME = "RESUME";
    private static final String TAG = "FirebaseAnalyticsUtil";
    public static void logClickAdsEventIS(Context context, String adsName) {
        Bundle bundle = new Bundle();
        bundle.putString("ad_type", adsName);
        FirebaseAnalytics.getInstance(context).logEvent("event_user_click_ads_is", bundle);
    }
    public static void logClickAdsEventAdmob(Context context) {
        Bundle bundle = new Bundle();
        FirebaseAnalytics.getInstance(context).logEvent("event_user_click_ads_admob", bundle);
    }


    public static void logClickAdsEventByActivity(Context context, String type) {
        if(context!=null){
            String nameActivity = context.getClass().getSimpleName();
            String nameEvent = "click_ads_"+type+"_screen__"+nameActivity;
            Log.e(TAG, "logClickAdsEventByActivity: "+nameEvent);
            Bundle bundle = new Bundle();
            FirebaseAnalytics.getInstance(context).logEvent(nameEvent, bundle);
        }
    }

    public static void logClickAdsISEventByActivity(Context context, String type) {
        if(context!=null){
            String nameActivity = context.getClass().getSimpleName();
            String nameEvent = "click_ads_IS_"+type+"_screen__"+nameActivity;
            Log.e(TAG, "logClickAdsEventByActivity: "+nameEvent);
            Bundle bundle = new Bundle();
            FirebaseAnalytics.getInstance(context).logEvent(nameEvent, bundle);
        }
    }

    public static void logPaidAdImpression(Context context,ImpressionData adValue){
        logEventWithAds(context, adValue.getRevenue(), 0, adValue.getAdUnit(), adValue.getAdNetwork(), "ironSource");
    }
    private static void logEventWithAds(Context context, double revenue, int precision, String adUnitId, String network, String mediation) {
        Log.d(TAG, String.format(
                "Paid event of value %.0f microcents in currency USD of precision %s%n occurred for ad unit %s from ad network %s.mediation provider: %s%n",
                revenue,
                precision,
                adUnitId,
                network, mediation));

        Bundle params = new Bundle(); // Log ad value in micros.
        params.putDouble("valuemicros", revenue);
        params.putString("currency", "USD");
        params.putInt("precision", precision);
        params.putString("adunitid", adUnitId);
        params.putString("network", network);


        FirebaseAnalytics.getInstance(context).logEvent("amazic_IS_paid_ad_impression", params);

        SharePreferenceUtils.updateCurrentTotalRevenueAd(context, (float) revenue);

        // update current total revenue ads for event paid_ad_impression_value_0.01
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
            bundle.putFloat("value", revenue / 1000000);
            FirebaseAnalytics.getInstance(context).logEvent("amazic_IS_daily_ad_revenue", bundle);
        }
    }
}
