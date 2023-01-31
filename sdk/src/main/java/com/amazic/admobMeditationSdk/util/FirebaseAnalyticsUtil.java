package com.amazic.admobMeditationSdk.util;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.google.firebase.analytics.FirebaseAnalytics;

public class FirebaseAnalyticsUtil {
    public static String INTER = "INTER";
    public static String BANNER = "BANNER";
    public static String NATIVE = "NATIVE";
    private static final String TAG = "FirebaseAnalyticsUtil";

    public static void logEventMediationAdmob(Context context, String adsType) {
        Bundle bundle = new Bundle();
        Log.e(TAG,"Mediation Admob :"+adsType);
        FirebaseAnalytics.getInstance(context).logEvent("MediationAdmob: "+adsType, bundle);
    }

    public static void logEventMediationAdx(Context context, String adsType) {
        Bundle bundle = new Bundle();
        Log.e(TAG,"MediationAdmob Adx :"+adsType);
        FirebaseAnalytics.getInstance(context).logEvent("MediationAdx: "+adsType, bundle);
    }
}
