package com.amazic.admobMeditationSdk.admob;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.amazic.admobMeditationSdk.util.FirebaseAnalyticsUtil;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.mediation.NativeMediationAdRequest;
import com.google.android.gms.ads.mediation.customevent.CustomEventNative;
import com.google.android.gms.ads.mediation.customevent.CustomEventNativeListener;

public class nativeAds implements CustomEventNative {
    private String Tag = "SDKCustom Native";
    @Override
    public void requestNativeAd(@NonNull Context context,
                                @NonNull CustomEventNativeListener customEventNativeListener,
                                @Nullable String serverParameter,
                                @NonNull NativeMediationAdRequest nativeMediationAdRequest,
                                @Nullable Bundle bundle) {

        Log.e(Tag, "ID :"+serverParameter );
        FirebaseAnalyticsUtil.logEventMediationAdmob(context,FirebaseAnalyticsUtil.NATIVE);
        AdLoader adLoader = new AdLoader.Builder(context, serverParameter)
                .withAdListener(new AdListener() {
                    @Override
                    public void onAdLoaded() {
                        if(customEventNativeListener != null){
                            customEventNativeListener.onAdLoaded(null);
                        }
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        if(customEventNativeListener != null){
                            customEventNativeListener.onAdFailedToLoad(loadAdError);
                        }
                    }

                    @Override
                    public void onAdClicked() {
                        if(customEventNativeListener != null){
                            customEventNativeListener.onAdClicked();
                        }
                    }
                })
                .build();

        // Begin a request.
        adLoader.loadAd( new AdRequest.Builder().build());
    }


    @Override
    public void onResume() {
    }

    @Override
    public void onDestroy() {
    }

    @Override
    public void onPause() {
    }
}
