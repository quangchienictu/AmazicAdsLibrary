package com.amazic.ads.event;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.amazic.ads.util.AppUtil;
import com.amazic.ads.util.SharePreferenceUtils;
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
        FirebaseAnalyticsUtil.logClickAdsEvent(context, bundle);
        FacebookEventUtils.logClickAdsEvent(context, bundle);
    }

    public static void logPaidAdImpression(Context context, AdValue adValue, String adUnitId, AdType adType) {
        Log.e("logPaidAdImpression",adValue.getCurrencyCode()+"");
        logEventWithAds(context, (float) adValue.getValueMicros(), adValue.getPrecisionType(), adUnitId, adType.toString(),adValue.getCurrencyCode());
    }

    private static void logEventWithAds(Context context, float revenue, int precision, String adUnitId, String network, String  mediationProvider) {
        Log.d(TAG, String.format(
                "Paid event of value %.0f microcents in currency USD of precision %s%n occurred for ad unit %s from ad network %s.mediation provider: %s%n",
                revenue,
                precision,
                adUnitId,
                network, mediationProvider));

        Bundle params = new Bundle(); // Log ad value in micros.
        params.putDouble("valuemicros", revenue);
        params.putString("currency", "USD");
        // These values below won’t be used in ROAS recipe.
        // But log for purposes of debugging and future reference.
        params.putInt("precision", precision);
        params.putString("adunitid", adUnitId);
        params.putString("network", network);

        // log revenue this ad
        logPaidAdImpressionValue(context, revenue / 1000000.0, precision, adUnitId, network, mediationProvider);
        FirebaseAnalyticsUtil.logEventWithAds(context, params);
        FacebookEventUtils.logEventWithAds(context, params);
        // update current tota
        // l revenue ads
        SharePreferenceUtils.updateCurrentTotalRevenueAd(context, (float) revenue);
       // logCurrentTotalRevenueAd(context, "event_current_total_revenue_ad");

        // update current total revenue ads for event paid_ad_impression_value_0.01
        AppUtil.currentTotalRevenue001Ad += revenue;
        SharePreferenceUtils.updateCurrentTotalRevenue001Ad(context, AppUtil.currentTotalRevenue001Ad);
        logTotalRevenue001Ad(context);

    }

    private static void logPaidAdImpressionValue(Context context, double value, int precision, String adunitid, String network, String mediationProvider) {
        Bundle params = new Bundle();
        params.putDouble("value", value);
        params.putString("currency", "USD");
        params.putInt("precision", precision);
        params.putString("adunitid", adunitid);
        params.putString("network", network);


        FirebaseAnalyticsUtil.logPaidAdImpressionValue(context, params);
        FacebookEventUtils.logPaidAdImpressionValue(context, params);
    }

    public static void logTotalRevenue001Ad(Context context) {
        float revenue = AppUtil.currentTotalRevenue001Ad;
        if (revenue / 1000000 >= 0.01) {
            AppUtil.currentTotalRevenue001Ad = 0;
            SharePreferenceUtils.updateCurrentTotalRevenue001Ad(context, 0);
            Bundle bundle = new Bundle();
            bundle.putFloat(FirebaseAnalytics.Param.VALUE, revenue / 1000000);
            bundle.putString(FirebaseAnalytics.Param.CURRENCY, "USD");
            FirebaseAnalyticsUtil.logTotalRevenue001Ad(context, bundle);
            FacebookEventUtils.logTotalRevenue001Ad(context, bundle);
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

    public static void logTimeLoadShowAdsInter(Context context,double timeLoad){
        Log.d(TAG, String.format(
                "Time show ads  %s",
                timeLoad));
        Bundle bundle = new Bundle();
        bundle.putString("time_show", String.valueOf(timeLoad));
        FirebaseAnalytics.getInstance(context).logEvent("event_time_show_ads_inter", bundle);
    }

}
