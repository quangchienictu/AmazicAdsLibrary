package com.amazic.ads.util.manager.inter_ad;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import com.amazic.ads.callback.InterCallback;
import com.amazic.ads.service.AdmobApi;
import com.amazic.ads.util.Admob;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.interstitial.InterstitialAd;

import java.util.HashMap;
import java.util.Map;

public class InterManager {
    private static final String TAG = "InterManager";
    private static final Map<String, InterstitialAd> listInter = new HashMap<>();

    public static void loadInterAds(Context context, String adsKey) {
        if (listInter.get(adsKey) == null) {
            Admob.getInstance().loadInterAdsFloor(context, AdmobApi.getInstance().getListIDByName(adsKey), new InterCallback() {
                @Override
                public void onAdLoadSuccess(InterstitialAd interstitialAd) {
                    super.onAdLoadSuccess(interstitialAd);
                    listInter.put(adsKey, interstitialAd);
                    Log.d(TAG, "onAdLoaded: " + listInter);
                }
            });
        } else {
            Log.d(TAG, "Inter already loaded. (inter != null)");
        }
    }

    public static void showInterAds(Activity activity, String adsKey, InterCallback interCallback, boolean isReloadInterAfterShow) {
        Admob.getInstance().showInterAds(activity, listInter.get(adsKey), new InterCallback() {
            @Override
            public void onNextAction() {
                super.onNextAction();
                interCallback.onNextAction();
                listInter.put(adsKey, null);
                if (isReloadInterAfterShow) {
                    loadInterAds(activity, adsKey);
                }
                Log.d(TAG, "onNextAction: " + listInter);
            }

            @Override
            public void onAdClicked() {
                super.onAdClicked();
                interCallback.onAdClicked();
            }

            @Override
            public void onAdImpression() {
                super.onAdImpression();
                interCallback.onAdImpression();
            }

            @Override
            public void onAdClosed() {
                super.onAdClosed();
                interCallback.onAdClosed();
            }

            @Override
            public void onAdFailedToLoad(LoadAdError i) {
                super.onAdFailedToLoad(i);
                interCallback.onAdFailedToLoad(i);
            }

            @Override
            public void onAdFailedToShow(AdError adError) {
                super.onAdFailedToShow(adError);
                interCallback.onAdFailedToShow(adError);
            }

            @Override
            public void onAdLeftApplication() {
                super.onAdLeftApplication();
                interCallback.onAdLeftApplication();
            }

            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                interCallback.onAdLoaded();
            }

            @Override
            public void onAdLoadSuccess(InterstitialAd interstitialAd) {
                super.onAdLoadSuccess(interstitialAd);
                interCallback.onAdLoadSuccess(interstitialAd);
            }

            @Override
            public void onEarnRevenue(Double Revenue) {
                super.onEarnRevenue(Revenue);
                interCallback.onEarnRevenue(Revenue);
            }

            @Override
            public void onInterDismiss() {
                super.onInterDismiss();
                interCallback.onInterDismiss();
            }

            @Override
            public void onLoadInter() {
                super.onLoadInter();
                interCallback.onLoadInter();
            }
        }, adsKey);
    }
}
