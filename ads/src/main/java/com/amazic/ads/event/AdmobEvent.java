package com.amazic.ads.event;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.amazic.ads.util.Admob;
import com.google.firebase.analytics.FirebaseAnalytics;

public class AdmobEvent {
    public static int OPEN_POSITION=0;
    private static final String TAG = "AdmobEvent";
    public static void logEvent(Context context, String nameEvent,Bundle params) {
        Log.e(TAG,nameEvent);
        FirebaseAnalytics.getInstance(context).logEvent(nameEvent, params);
    }
}
