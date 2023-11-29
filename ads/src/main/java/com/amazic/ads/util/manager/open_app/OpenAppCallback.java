package com.amazic.ads.util.manager.open_app;

import androidx.annotation.NonNull;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.appopen.AppOpenAd;

public class OpenAppCallback {
    public void onNextAction() {
    }

    public void onAdClosed() {
    }

    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
    }

    public void onAdFailedToShow(@NonNull AdError adError) {
    }

    public void onAdClicked() {
    }

    public void onAdImpression() {
    }

    public void onAdShowed() {
    }

    public void onAdLoaded() {
    }
}
