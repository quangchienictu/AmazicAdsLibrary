package com.amazic.ads.callback;

import com.google.android.gms.ads.rewarded.RewardItem;

public interface RewardCallBackIS {
    void onEarnedReward();
    void onAdClosed();
    void onAdFailedToShow();
}
