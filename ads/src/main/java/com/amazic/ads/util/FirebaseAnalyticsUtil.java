package com.amazic.ads.util;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.google.firebase.analytics.FirebaseAnalytics;

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

}
