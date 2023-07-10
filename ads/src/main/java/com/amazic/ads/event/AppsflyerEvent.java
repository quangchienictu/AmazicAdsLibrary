package com.amazic.ads.event;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.amazic.ads.util.Admob;
import com.amazic.ads.util.AppOpenManager;
import com.appsflyer.AppsFlyerLib;
import com.appsflyer.adrevenue.AppsFlyerAdRevenue;
import com.appsflyer.adrevenue.adnetworks.generic.MediationNetwork;
import com.appsflyer.adrevenue.adnetworks.generic.Scheme;
import com.google.android.gms.ads.AdValue;

import java.util.Currency;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class AppsflyerEvent {
    private static final String TAG = "AppsflyerEvent";
    private static AppsflyerEvent appsflyerEvent;
    public static boolean enableTrackingRevenue = false;
    private Context context;
    public AppsflyerEvent() {
    }
    public static synchronized AppsflyerEvent getInstance() {
        if (appsflyerEvent == null) {
            appsflyerEvent = new AppsflyerEvent();
        }
        return appsflyerEvent;
    }

    public void init(Application context, String devKey,boolean enableTrackingRevenue) {
        initDebug(context, devKey,false);
        this.enableTrackingRevenue = enableTrackingRevenue;
    }
    public void initDebug(Application context, String devKey, boolean enableDebugLog) {
        this.context = context;
        AppsFlyerLib.getInstance().init(devKey, null, context);
        AppsFlyerLib.getInstance().start(context);

        AppsFlyerAdRevenue.Builder afRevenueBuilder = new AppsFlyerAdRevenue.Builder(context);
        AppsFlyerAdRevenue.initialize(afRevenueBuilder.build());
        AppsFlyerLib.getInstance().setDebugLog(enableDebugLog);
    }

    public static void pushTrackEventAdmob(AdValue adValue, String idAd, AdType adType) {
        Log.e(TAG, "logPaidAdImpression  enableAppsflyer:"+enableTrackingRevenue+ " --- value: "+adValue.getValueMicros() / 1000000.0 + " -- adType: " +adType.toString());
        if (enableTrackingRevenue) {
            Map<String, String> customParams = new HashMap<>();
            customParams.put(Scheme.AD_UNIT, idAd);
            customParams.put(Scheme.AD_TYPE, adType.toString());
            AppsFlyerAdRevenue.logAdRevenue(
                    "Admob",
                    MediationNetwork.googleadmob,
                    Currency.getInstance(Locale.US),
                    adValue.getValueMicros() / 1000000.0,
                    customParams
            );
        }
    }
}
