package com.amazic.ads.util.manager.banner;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleEventObserver;
import androidx.lifecycle.LifecycleOwner;

import com.amazic.ads.callback.BannerCallBack;
import com.amazic.ads.util.Admob;
import com.google.android.gms.ads.LoadAdError;

public class BannerManager implements LifecycleEventObserver {
    enum State {LOADING, LOADED}

    private static final String TAG = "BannerManager";
    private boolean isReloadAds = false;
    private boolean isAlwaysReloadOnResume = false;
    private final BannerBuilder build;
    State state = State.LOADED;
    private boolean isOnStop = false;

    public BannerManager(BannerBuilder build) {
        this.build = build;
        this.build.getLifecycleOwner().getLifecycle().addObserver(this);
    }

    @Override
    public void onStateChanged(@NonNull LifecycleOwner source, @NonNull Lifecycle.Event event) {
        switch (event) {
            case ON_CREATE:
                Log.d(TAG, "onStateChanged: ON_CREATE");
                loadBanner();
                break;
            case ON_RESUME:
                if (isOnStop && (isReloadAds || isAlwaysReloadOnResume)) {
                    Log.d(TAG, "onStateChanged: resume");
                    isReloadAds = false;
                    loadBanner();
                }
                isOnStop = false;
                break;
            case ON_STOP:
                isOnStop = true;
            case ON_DESTROY:
                this.build.getLifecycleOwner().getLifecycle().removeObserver(this);
                break;
        }
    }

    private void loadBanner() {
        Log.d(TAG, "loadBanner: " + build.getListId());
        if (Admob.isShowAllAds) {
            if (state == State.LOADING)
                return;
            state = State.LOADING;
            BannerCallBack bannerCallBack = new BannerCallBack() {
                public void onEarnRevenue(Double Revenue) {
                    build.getCallBack().onEarnRevenue(Revenue);
                }

                public void onAdFailedToLoad(LoadAdError loadAdError) {
                    state = State.LOADED;
                    build.getCallBack().onAdFailedToLoad(loadAdError);
                }

                public void onAdLoadSuccess() {
                    state = State.LOADED;
                    build.getCallBack().onAdLoadSuccess();
                }

                public void onAdClicked() {
                    build.getCallBack().onAdClicked();
                }

                public void onAdImpression() {
                    state = State.LOADED;
                    build.getCallBack().onAdImpression();
                }
            };
            Admob.getInstance().loadBannerFloor(build.getCurrentActivity(), build.getListId(), bannerCallBack);
        } else
            Admob.getInstance().hideBanner(build.getCurrentActivity());
    }

    public void setReloadAds() {
        isReloadAds = true;
    }

    public void reloadAdNow() {
        loadBanner();
    }

    public void setAlwaysReloadOnResume() {
        isAlwaysReloadOnResume = true;
    }

}
