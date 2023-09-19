package com.amazic.ads.callback;

import com.google.android.gms.ads.LoadAdError;

public class BannerCallBack {
    public void onEarnRevenue(Double Revenue){}
    public void onAdFailedToLoad(LoadAdError loadAdError){}
    public void onAdLoadSuccess(){}
    public void onAdClicked(){}
    public void onAdImpression(){}
}