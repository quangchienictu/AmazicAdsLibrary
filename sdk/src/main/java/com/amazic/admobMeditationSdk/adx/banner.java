package com.amazic.admobMeditationSdk.adx;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.amazic.admobMeditationSdk.util.FirebaseAnalyticsUtil;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.admanager.AdManagerAdRequest;
import com.google.android.gms.ads.admanager.AdManagerAdView;
import com.google.android.gms.ads.mediation.MediationAdRequest;
import com.google.android.gms.ads.mediation.customevent.CustomEventBanner;
import com.google.android.gms.ads.mediation.customevent.CustomEventBannerListener;

public class banner implements CustomEventBanner
{
    private String Tag = "SDKCustom Banner";
    private AdManagerAdView adView;
    @Override
    public void requestBannerAd(@NonNull Context context, @NonNull CustomEventBannerListener customEventBannerListener, @Nullable String s, @NonNull AdSize adSize, @NonNull MediationAdRequest mediationAdRequest, @Nullable Bundle bundle) {
        Log.e(Tag, "ID :"+s );
        FirebaseAnalyticsUtil.logEventMediationAdx(context,FirebaseAnalyticsUtil.BANNER);
        adView = new AdManagerAdView(context);
        adView.setAdUnitId(s);
        adView.setAdSize(adSize);
        adView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        adView.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                super.onAdClosed();
                if(customEventBannerListener != null){
                    customEventBannerListener.onAdClosed();
                }
            }

            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                super.onAdFailedToLoad(loadAdError);
                if(customEventBannerListener != null){
                    customEventBannerListener.onAdFailedToLoad(loadAdError);
                }
            }

            @Override
            public void onAdOpened() {
                super.onAdOpened();
                if(customEventBannerListener != null){
                    customEventBannerListener.onAdOpened();
                }
            }

            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                if(customEventBannerListener != null){
                    customEventBannerListener.onAdLoaded(adView);
                }
            }

            @Override
            public void onAdClicked() {
                super.onAdClicked();
                if(customEventBannerListener != null){
                    customEventBannerListener.onAdClicked();
                }
            }

        });
        adView.loadAd(new AdManagerAdRequest.Builder().build());
    }

    @Override
    public void onDestroy() {
    }

    @Override
    public void onPause() {
    }

    @Override
    public void onResume() {

    }

}
