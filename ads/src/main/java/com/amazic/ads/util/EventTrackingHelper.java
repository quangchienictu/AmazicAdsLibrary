package com.amazic.ads.util;

import android.content.Context;
import android.os.Bundle;

import com.google.firebase.analytics.FirebaseAnalytics;

public class EventTrackingHelper {
    public static void logEvent(Context context, String eventName) {
        if (context == null) {
            return;
        }
        FirebaseAnalytics firebaseAnalytics = FirebaseAnalytics.getInstance(context);
        Bundle bundle = new Bundle();
        firebaseAnalytics.logEvent(eventName, bundle);
    }

    public static void logEventWithAParam(Context context, String eventName, String param, String value) {
        if (context == null) {
            return;
        }
        FirebaseAnalytics firebaseAnalytics = FirebaseAnalytics.getInstance(context);
        Bundle bundle = new Bundle();
        bundle.putString(param, value);
        firebaseAnalytics.logEvent(eventName, bundle);
    }

    public static void logEventWithMultipleParams(Context context, String eventName, Bundle bundle) {
        if (context == null) {
            return;
        }
        FirebaseAnalytics firebaseAnalytics = FirebaseAnalytics.getInstance(context);
        firebaseAnalytics.logEvent(eventName, bundle);
    }

    public static String rate_aoa_inter_splash = "rate_aoa_inter_splash";
    public static String inter_splash = "inter_splash";
    public static String open_splash = "open_splash";
    public static String banner_splash = "banner_splash";
    public static String native_language = "native_language";
    public static String native_language_impression = "native_language_impression";
    public static String native_language_click = "native_language_click";
    public static String native_intro = "native_intro";
    public static String native_intro_full = "native_intro_full";
    public static String inter_intro = "inter_intro";
    public static String native_permission = "native_permission";
    public static String native_interest = "native_interest";
    public static String native_wb = "native_wb";
    public static String open_resume = "open_resume";
    public static String splash_open = "splash_open";
    public static String inter_splash_tracking = "inter_splash_tracking";
    public static String open_splash_tracking = "open_splash_tracking";
    public static String splash_detail = "splash_detail";
    public static String ump = "ump";
    public static String organic = "organic";
    public static String haveinternet = "haveinternet";
    public static String showallad = "showallad";
    public static String idcheck = "idcheck";
    public static String interremote = "interremote";
    public static String openremote = "openremote";
    public static String aoavalue = "aoavalue";
    public static String inter_splash_id_timeout = "inter_splash_id_timeout";
    public static String inter_splash_true = "inter_splash_true";
    public static String open_splash_true = "open_splash_true";
    public static String inter_splash_impression = "inter_splash_impression";
    public static String open_splash_impression = "open_splash_impression";
    public static String inter_splash_click = "inter_splash_click";
    public static String open_splash_click = "open_splash_click";
    public static String inter_splash_showad_time = "inter_splash_showad_time";
    public static String showad_time = "showad_time";
}
