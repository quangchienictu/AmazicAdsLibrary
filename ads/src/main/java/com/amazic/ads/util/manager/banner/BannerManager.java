package com.amazic.ads.util.manager.banner;

import android.app.Activity;
import android.content.Context;
import android.os.CountDownTimer;
import android.util.Log;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleEventObserver;
import androidx.lifecycle.LifecycleOwner;

import com.amazic.ads.util.Admob;
import com.google.android.gms.ads.AdRequest;

public class BannerManager implements LifecycleEventObserver {
    enum State {LOADING, LOADED}

    private static final String TAG = "NativeManager";
    private final BannerBuilder builder;
    private Activity currentActivity;
    private final LifecycleOwner lifecycleOwner;
    private boolean isReloadAds = false;
    private boolean isAlwaysReloadOnResume = false;
    private boolean isShowLoadingBanner = true;
    State state = State.LOADED;
    private long intervalReloadBanner = 0;
    private boolean isStop = false;
    private CountDownTimer countDownTimer;
    private boolean isStopReload = false;
    private Context context;
    private int adWidth;
    private FrameLayout frContainer;
    private boolean isLoadBannerFragment = false;

    public void notReloadInNextResume() {
        isStopReload = true;
    }

    public void setIntervalReloadBanner(long intervalReloadBanner) {
        if (intervalReloadBanner > 0)
            this.intervalReloadBanner = intervalReloadBanner;
        countDownTimer = new CountDownTimer(this.intervalReloadBanner, 1000) {
            @Override
            public void onTick(long l) {

            }

            @Override
            public void onFinish() {
                if (isLoadBannerFragment) {
                    loadBannerFragment();
                } else {
                    loadBanner();
                }
            }
        };
    }

    public BannerManager(@NonNull Activity currentActivity, LifecycleOwner lifecycleOwner, BannerBuilder builder) {
        this.isLoadBannerFragment = false;
        this.builder = builder;
        this.currentActivity = currentActivity;
        this.lifecycleOwner = lifecycleOwner;
        this.lifecycleOwner.getLifecycle().addObserver(this);
    }

    public BannerManager(Context context, int adWidth, FrameLayout frContainer, LifecycleOwner lifecycleOwner, BannerBuilder builder) {
        this.isLoadBannerFragment = true;
        this.builder = builder;
        this.context = context;
        this.adWidth = adWidth;
        this.frContainer = frContainer;
        this.lifecycleOwner = lifecycleOwner;
        this.lifecycleOwner.getLifecycle().addObserver(this);
    }

    @Override
    public void onStateChanged(@NonNull LifecycleOwner source, @NonNull Lifecycle.Event event) {
        switch (event) {
            case ON_CREATE:
                Log.d(TAG, "onStateChanged: ON_CREATE");
                if (isLoadBannerFragment) {
                    loadBannerFragment();
                } else {
                    loadBanner();
                }
                break;
            case ON_RESUME:
                if (countDownTimer != null && isStop) {
                    countDownTimer.start();
                }
                String valueLog = isStop + " && " + (isReloadAds || isAlwaysReloadOnResume) + " && " + !isStopReload;
                Log.d(TAG, "onStateChanged: resume\n" + valueLog);
                if (isStop && (isReloadAds || isAlwaysReloadOnResume) && !isStopReload) {
                    isReloadAds = false;
                    if (isLoadBannerFragment) {
                        loadBannerFragment();
                    } else {
                        loadBanner();
                    }
                }
                isStopReload = false;
                isStop = false;
                break;
            case ON_PAUSE:
                Log.d(TAG, "onStateChanged: ON_PAUSE");
                isStop = true;
                if (countDownTimer != null) {
                    countDownTimer.cancel();
                }
                break;
            case ON_DESTROY:
                Log.d(TAG, "onStateChanged: ON_DESTROY");
                this.lifecycleOwner.getLifecycle().removeObserver(this);
                break;
        }
    }

    private void loadBanner() {
        Log.d(TAG, "loadBanner: " + builder.getListId());
        if (Admob.isShowAllAds) {
            Admob.getInstance().loadBannerFloor(currentActivity, builder.getListId());
        } else {
            Admob.getInstance().hideBanner(currentActivity);
        }
    }

    private void loadBannerFragment() {
        Log.d(TAG, "loadBanner: " + builder.getListId());
        if (Admob.isShowAllAds) {
            Admob.getInstance().loadBannerFloor(context, adWidth, frContainer, builder.getListId());
        } else {
            Admob.getInstance().hideBanner(currentActivity);
        }
    }


    public void setReloadAds() {
        isReloadAds = true;
    }

    public void reloadAdNow() {
        if (isLoadBannerFragment) {
            loadBannerFragment();
        } else {
            loadBanner();
        }
    }

    public void setAlwaysReloadOnResume(boolean isAlwaysReloadOnResume) {
        this.isAlwaysReloadOnResume = isAlwaysReloadOnResume;
    }

    public AdRequest getAdRequest() {
        AdRequest.Builder builder = new AdRequest.Builder();
        return builder.build();
    }
}
