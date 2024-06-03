package com.amazic.ads.util.reward;

import androidx.annotation.NonNull;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.rewarded.RewardItem;

public class RewardAdCallback {

    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
    }

    public void onAdLoaded(Boolean isSuccessful) {
    }

    public void onAdDismissed() {
    }

    public void onAdFailedToShow(@NonNull AdError adError) {
    }

    public void onAdShowed() {
    }

    public void onAdClicked() {
    }

    public void onNextAction() {
    }

    public void onAdImpression() {
    }

    public void onUserEarnedReward(@NonNull RewardItem rewardItem) {
    }
}
