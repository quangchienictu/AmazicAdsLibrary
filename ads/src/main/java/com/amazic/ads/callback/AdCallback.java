package com.amazic.ads.callback;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAd;

public class AdCallback {

    public void onNextAction() {
    }

    public void onAdClosed() {
    }


    public void onAdFailedToLoad(@Nullable LoadAdError i) {
    }

    public void onAdFailedToShow(@Nullable AdError adError) {
    }

    public void onAdLeftApplication() {
    }


    public void onAdLoaded() {
    }

    public void onAdSplashReady() {
    }

    public void onInterstitialLoad(@Nullable InterstitialAd interstitialAd) {

    }

    public void onAdClicked() {
    }

    public void onAdImpression() {
    }

    public void onRewardAdLoaded(RewardedAd rewardedAd) {
    }

    public void onRewardAdLoaded(RewardedInterstitialAd rewardedAd) {
    }


    public void onUnifiedNativeAdLoaded(@NonNull NativeAd unifiedNativeAd) {

    }

    public void onInterstitialShow() {

    }
}
