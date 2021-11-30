package com.amazic.ads.callback;

import com.google.android.gms.ads.rewarded.RewardItem;

public interface RewardCallback {
    void onEarnedReward(RewardItem rewardItem);
    void onAdClosed();
    void onAdFailedToShow(int codeError  );
}
