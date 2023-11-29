package com.amazic.ads.util.manager.native_ad;

import android.app.Activity;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleEventObserver;
import androidx.lifecycle.LifecycleOwner;

import com.amazic.ads.util.Admob;
import com.amazic.ads.util.NetworkUtil;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
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

    public NativeManager(@NonNull Activity currentActivity, LifecycleOwner lifecycleOwner, NativeBuilder builder) {
        this.builder = builder;
        this.currentActivity = currentActivity;
        this.lifecycleOwner = lifecycleOwner;
        this.lifecycleOwner.getLifecycle().addObserver(this);
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
        if (isShowLoading)
            builder.showLoading();
        List<String> listID = new ArrayList<>(builder.listIdAd);
        Log.d(TAG, "loadNative: " + builder.listIdAd);
        loadNativeFloor(listID);
    }

    private void loadNativeFloor(@NonNull List<String> listID) {
        if (!Admob.isShowAllAds || listID.isEmpty() || !NetworkUtil.isNetworkActive(currentActivity)) {
            builder.getCallback().onAdFailedToLoad();
            builder.hideAd();
            return;
        }
        state = State.LOADING;
        Log.d(TAG, "loadNativeFloor: " + listID.get(0));
        AdLoader adLoader = new AdLoader.Builder(currentActivity, listID.get(0))
                .forNativeAd(nativeAd -> {
                    // Show the ad.
                    Log.d(TAG, "showAd: ");
                    state = State.LOADED;
                    builder.showAd();
                    builder.getCallback().onNativeAdLoaded(nativeAd);
                    Admob.getInstance().pushAdsToViewCustom(nativeAd, builder.nativeAdView);
                })
                .withAdListener(new AdListener() {
                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError adError) {
                        Log.d(TAG, "onAdFailedToLoad: " + adError.getMessage());
                        if (listID.size() > 1) {
                            listID.remove(0);
                            loadNativeFloor(listID);
                        } else {
                            state = State.LOADED;
                            builder.getCallback().onAdFailedToLoad();
                            builder.hideAd();
                        }
                    }

                    @Override
                    public void onAdImpression() {
                        super.onAdImpression();
                        state = State.LOADED;
                        Log.d(TAG, "onAdImpression: ");
                    }
                })
                .withNativeAdOptions(new NativeAdOptions.Builder()
                        .setVideoOptions(new VideoOptions.Builder()
                                .setStartMuted(true).build())
                        .build())
                .build();
        adLoader.loadAd(getAdRequest());
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
}
