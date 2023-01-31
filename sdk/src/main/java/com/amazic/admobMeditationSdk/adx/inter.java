package com.amazic.admobMeditationSdk.adx;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.amazic.admobMeditationSdk.util.FirebaseAnalyticsUtil;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.admanager.AdManagerAdRequest;
import com.google.android.gms.ads.admanager.AdManagerInterstitialAd;
import com.google.android.gms.ads.admanager.AdManagerInterstitialAdLoadCallback;
import com.google.android.gms.ads.mediation.MediationAdRequest;
import com.google.android.gms.ads.mediation.customevent.CustomEventInterstitial;
import com.google.android.gms.ads.mediation.customevent.CustomEventInterstitialListener;

public class inter  implements CustomEventInterstitial {
    private AdManagerInterstitialAd mInterstitialAd;
    private Context mContext;
    private boolean isLoadSuccess;
    private String Tag = "SDKCustom Inter";
    @Override
    public void requestInterstitialAd(@NonNull Context context, @NonNull CustomEventInterstitialListener customEventInterstitialListener, @Nullable String s, @NonNull MediationAdRequest mediationAdRequest, @Nullable Bundle bundle) {
        Log.e(Tag, "ID :"+s );
        FirebaseAnalyticsUtil.logEventMediationAdx(context,FirebaseAnalyticsUtil.INTER);
        this.mContext = context;
        isLoadSuccess = false;
        AdManagerAdRequest adRequest = new AdManagerAdRequest.Builder().build();
        AdManagerInterstitialAd.load(context, s, adRequest, new AdManagerInterstitialAdLoadCallback() {
            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                if(customEventInterstitialListener != null){
                    customEventInterstitialListener.onAdFailedToLoad(loadAdError);
                }
            }

            @Override
            public void onAdLoaded(@NonNull AdManagerInterstitialAd interstitialAd) {
                if(customEventInterstitialListener != null){
                    customEventInterstitialListener.onAdLoaded();
                }
                isLoadSuccess = true;
                mInterstitialAd = interstitialAd;
                interstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                    @Override
                    public void onAdDismissedFullScreenContent() {
                        if(customEventInterstitialListener != null){
                            customEventInterstitialListener.onAdClosed();
                        }
                    }

                    @Override
                    public void onAdClicked() {
                        if(customEventInterstitialListener != null){
                            customEventInterstitialListener.onAdClicked();
                        }
                    }
                });
            }
        });


    }

    @Override
    public void showInterstitial() {
        mInterstitialAd.show((Activity) mContext);
    }

    @Override
    public void onDestroy() {
        if (mInterstitialAd != null) {
            mInterstitialAd = null;
        }
    }

    @Override
    public void onPause() {

    }

    @Override
    public void onResume() {

    }
}
