package com.amazic.ads.util.manager.native_ad;

import android.app.Activity;
import android.util.Log;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleEventObserver;
import androidx.lifecycle.LifecycleOwner;

import com.adjust.sdk.Adjust;
import com.adjust.sdk.AdjustAdRevenue;
import com.adjust.sdk.AdjustConfig;
import com.amazic.ads.callback.NativeCallback;
import com.amazic.ads.util.Admob;
import com.amazic.ads.util.AdsConsentManager;
import com.amazic.ads.util.NetworkUtil;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdValue;
import com.google.android.gms.ads.AdapterResponseInfo;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.VideoOptions;
import com.google.android.gms.ads.nativead.NativeAdOptions;

import java.util.ArrayList;
import java.util.List;

public class NativeManager implements LifecycleEventObserver {
    enum State {LOADING, LOADED}

    private static final String TAG = "NativeManager";
    final NativeBuilder builder;
    private final Activity currentActivity;
    private final LifecycleOwner lifecycleOwner;
    private boolean isReloadAds = false;
    private boolean isAlwaysReloadOnResume = false;
    private boolean isShowLoadingNative = true;
    State state = State.LOADED;
    private FrameLayout flAd = null;
    private int idLayoutShimmer = 0;
    private int idLayoutNative = 0;

    public NativeManager(@NonNull Activity currentActivity, LifecycleOwner lifecycleOwner, NativeBuilder builder) {
        this.builder = builder;
        this.currentActivity = currentActivity;
        this.lifecycleOwner = lifecycleOwner;
        this.lifecycleOwner.getLifecycle().addObserver(this);
    }

    public NativeManager(@NonNull Activity currentActivity, LifecycleOwner lifecycleOwner, NativeBuilder builder, FrameLayout flAd, int idLayoutShimmer, int idLayoutNative) {
        this.builder = builder;
        this.currentActivity = currentActivity;
        this.lifecycleOwner = lifecycleOwner;
        this.lifecycleOwner.getLifecycle().addObserver(this);
        this.flAd = flAd;
        this.idLayoutShimmer = idLayoutShimmer;
        this.idLayoutNative = idLayoutNative;
    }

    @Override
    public void onStateChanged(@NonNull LifecycleOwner source, @NonNull Lifecycle.Event event) {
        switch (event) {
            case ON_CREATE:
                Log.d(TAG, "onStateChanged: ON_CREATE");
                loadNative(true);
                break;
            case ON_RESUME:
                if (isReloadAds || isAlwaysReloadOnResume) {
                    Log.d(TAG, "onStateChanged: resume");
                    isReloadAds = false;
                    loadNative(isShowLoadingNative);
                }
                break;
            case ON_DESTROY:
                this.lifecycleOwner.getLifecycle().removeObserver(this);
                break;
        }
    }

    private void loadNative(boolean isShowLoading) {
        if (Admob.isShowAllAds) {
            if (isShowLoading)
                builder.showLoading();
            List<String> listID = new ArrayList<>(builder.listIdAd);

            if (this.state != NativeManager.State.LOADING) {
                this.state = NativeManager.State.LOADING;
                loadNativeFloor(listID);
            }
        } else {
            builder.hideAd();
        }
    }

    private void loadNativeFloor(@NonNull List<String> listID) {
        if (Admob.isShowAllAds && !listID.isEmpty() && NetworkUtil.isNetworkActive(this.currentActivity) && AdsConsentManager.getConsentResult(this.currentActivity)) {
            Log.d(TAG, "loadNativeFloor: " + listID.get(0));
            NativeCallback callback = this.builder.getCallback();
            AdLoader adLoader = (new AdLoader.Builder(this.currentActivity, listID.get(0))).forNativeAd((nativeAd) -> {
                Log.d(TAG, "showAd: ");
                this.state = State.LOADED;
                nativeAd.setOnPaidEventListener(adValue -> {
                    if (nativeAd.getResponseInfo() != null)
                        trackRevenue(nativeAd.getResponseInfo().getLoadedAdapterResponseInfo(), adValue);
                });
                callback.onNativeAdLoaded(nativeAd);
                Log.d("TAG", "loadNativeFloor1: " + nativeAd.getResponseInfo().getMediationAdapterClassName());
                Log.d("TAG", "loadNativeFloor2: " + nativeAd.getResponseInfo().getAdapterResponses());
                Log.d("TAG", "loadNativeFloor3: " + nativeAd.getResponseInfo().getLoadedAdapterResponseInfo().getAdSourceName());
                Log.d("TAG", "loadNativeFloor4: " + nativeAd.getResponseInfo().getLoadedAdapterResponseInfo().getAdapterClassName());
                Log.d("TAG", "loadNativeFloor5: " + nativeAd.getResponseInfo().getLoadedAdapterResponseInfo().getAdapterClassName());
                if (nativeAd.getResponseInfo().getMediationAdapterClassName().toString().toLowerCase().contains("facebook") && this.flAd != null && this.idLayoutShimmer != 0 && this.idLayoutNative != 0) {
                    this.builder.setLayoutAdsMeta(currentActivity, flAd, idLayoutShimmer, idLayoutNative);
                    Log.d(TAG, "loadNativeFloor: case mediation facebook");
                }
                this.builder.showAd();
                Admob.getInstance().pushAdsToViewCustom(nativeAd, this.builder.nativeAdView);
            }).withAdListener(new AdListener() {
                public void onAdFailedToLoad(@NonNull LoadAdError adError) {
                    listID.remove(0);
                    Log.d(TAG, "onAdFailedToLoad: " + adError.getMessage());
                    Log.d(TAG, "listID: " + listID);
                    if (!listID.isEmpty()) {
                        NativeManager.this.loadNativeFloor(listID);
                    } else {
                        NativeManager.this.state = State.LOADED;
                        NativeManager.this.builder.getCallback().onAdFailedToLoad();
                        NativeManager.this.builder.hideAd();
                    }

                }

                @Override
                public void onAdClicked() {
                    super.onAdClicked();
                    callback.onAdClicked();
                }

                public void onAdImpression() {
                    super.onAdImpression();
                    NativeManager.this.state = State.LOADED;
                    Log.d(TAG, "onAdImpression: ");
                }
            }).withNativeAdOptions((new NativeAdOptions.Builder()).setVideoOptions((new VideoOptions.Builder()).setStartMuted(true).build()).build()).build();
            adLoader.loadAd(this.getAdRequest());
        } else {
            this.state = NativeManager.State.LOADED;
            this.builder.getCallback().onAdFailedToLoad();
            this.builder.hideAd();
        }
    }

    public void setReloadAds() {
        isReloadAds = true;
    }

    public void reloadAdNow() {
        loadNative(isShowLoadingNative);
    }

    public void setAlwaysReloadOnResume(boolean isAlwaysReloadOnResume) {
        this.isAlwaysReloadOnResume = isAlwaysReloadOnResume;
    }

    public AdRequest getAdRequest() {
        AdRequest.Builder builder = new AdRequest.Builder();
        return builder.build();
    }

    public void setShowLoadingNative(boolean showLoadingNative) {
        isShowLoadingNative = showLoadingNative;
    }

    //push adjust
    private void trackRevenue(@Nullable AdapterResponseInfo loadedAdapterResponseInfo, AdValue adValue) {
        String adName = "";
        if (loadedAdapterResponseInfo != null)
            adName = loadedAdapterResponseInfo.getAdSourceName();
        double valueMicros = adValue.getValueMicros() / 1000000d;
        Log.d("AdjustRevenue", "adName: " + adName + " - valueMicros: " + valueMicros);
        // send ad revenue info to Adjust
        AdjustAdRevenue adRevenue = new AdjustAdRevenue(AdjustConfig.AD_REVENUE_ADMOB);
        adRevenue.setRevenue(valueMicros, adValue.getCurrencyCode());
        adRevenue.setAdRevenueNetwork(adName);
        Adjust.trackAdRevenue(adRevenue);
    }
}
