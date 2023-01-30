package com.amazic.ads.callback;

import com.google.android.gms.ads.nativead.NativeAd;

public class NativeCallback {
    public void onNativeAdLoaded(NativeAd nativeAd){};
    public void onAdFailedToLoad(){};
    public void onEarnRevenue(Double Revenue){}
}
