package com.amazic.ads.callback;

import com.google.android.gms.ads.nativead.NativeAd;

public interface NativeCallback {
    void onNativeAdLoaded(NativeAd nativeAd);
}
