package com.amazic.ads.util.manager.banner;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleEventObserver;
import androidx.lifecycle.LifecycleOwner;

import com.amazic.ads.util.Admob;

public class BannerManager implements LifecycleEventObserver {
    private static final String TAG = "BannerManager";
    private boolean isReloadAds = false;
    private boolean isAlwaysReloadOnResume = false;
    private final BannerBuilder build;

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
                if (isReloadAds || isAlwaysReloadOnResume) {
                    Log.d(TAG, "onStateChanged: resume");
                    isReloadAds = false;
                    loadBanner();
                }
                break;
            case ON_DESTROY:
                this.build.getLifecycleOwner().getLifecycle().removeObserver(this);
                break;
        }
    }

    private void loadBanner() {
        Log.d(TAG, "loadBanner: " + build.getListId());
        if (Admob.isShowAllAds)
            Admob.getInstance().loadBannerFloor(build.getCurrentActivity(), build.getListId(), build.getCallBack());
        else
            Admob.getInstance().hideBanner(build.getCurrentActivity());
    }

    public void setReloadAds() {
        isReloadAds = true;
    }

    public void setAlwaysReloadOnResume() {
        isAlwaysReloadOnResume = true;
    }

}
