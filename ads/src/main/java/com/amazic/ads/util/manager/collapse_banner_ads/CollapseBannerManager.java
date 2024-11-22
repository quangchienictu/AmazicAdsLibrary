package com.amazic.ads.util.manager.collapse_banner_ads;

import android.app.Activity;
import android.content.Context;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleEventObserver;
import androidx.lifecycle.LifecycleOwner;

import com.amazic.ads.util.Admob;
import com.google.android.gms.ads.AdView;

public class CollapseBannerManager implements LifecycleEventObserver {
    private static final String TAG = "CollapseBannerManager";
    private final CollapseBannerBuilder builder;
    private Activity currentActivity;
    private final LifecycleOwner lifecycleOwner;
    private boolean isReloadAds = false;
    private boolean isAlwaysReloadOnResume = false;
    private long intervalReloadBanner = 0;
    private boolean isStop = false;
    private CountDownTimer countDownTimer;
    private Context context;
    private int adWidth;
    private FrameLayout frContainer;
    private boolean isLoadBannerFragment = false;
    private AdView adView;

    public void setIntervalReloadBanner(long intervalReloadBanner) {
        if (intervalReloadBanner > 0) {
            this.intervalReloadBanner = intervalReloadBanner;
            countDownTimer = new CountDownTimer(this.intervalReloadBanner, 1000) {
                @Override
                public void onTick(long l) {

                }

                @Override
                public void onFinish() {
                    if (isLoadBannerFragment) {
                        loadCollapseBannerFragment(frContainer);
                    } else {
                        loadCollapseBanner(frContainer);
                    }
                }
            };
        }
    }

    public CollapseBannerManager(@NonNull Activity currentActivity, FrameLayout frContainer, LifecycleOwner lifecycleOwner, CollapseBannerBuilder builder) {
        this.isLoadBannerFragment = false;
        this.builder = builder;
        this.currentActivity = currentActivity;
        this.frContainer = frContainer;
        this.lifecycleOwner = lifecycleOwner;
        this.lifecycleOwner.getLifecycle().addObserver(this);
    }

    public CollapseBannerManager(Context context, int adWidth, FrameLayout frContainer, LifecycleOwner lifecycleOwner, CollapseBannerBuilder builder) {
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
                    loadCollapseBannerFragment(frContainer);
                } else {
                    loadCollapseBanner(frContainer);
                }
                break;
            case ON_RESUME:
                if (countDownTimer != null && isStop) {
                    countDownTimer.start();
                }
                String valueLog = isStop + " && " + (isReloadAds || isAlwaysReloadOnResume);
                Log.d(TAG, "onStateChanged: resume\n" + valueLog);
                if (isStop && (isReloadAds || isAlwaysReloadOnResume)) {
                    isReloadAds = false;
                    if (isLoadBannerFragment) {
                        loadCollapseBannerFragment(frContainer);
                    } else {
                        loadCollapseBanner(frContainer);
                    }
                }
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

    private void loadCollapseBanner(FrameLayout frContainer) {
        Log.d(TAG, "loadBanner: " + builder.getListId());
        if (Admob.isShowAllAds) {
            if (adView != null) {
                adView.destroy();
            }
            adView = Admob.getInstance().loadCollapsibleBannerFloorWithReload(currentActivity, builder.getListId(), builder.getBannerGravity(), builder.getCallBack(), () -> {
                if (countDownTimer != null) {
                    Log.d(TAG, "loadCollapseBannerxxx: ");
                    countDownTimer.cancel();
                    countDownTimer.start();
                }
            });
        } else {
            frContainer.setVisibility(View.GONE);
        }
    }

    private void loadCollapseBannerFragment(FrameLayout frContainer) {
        Log.d(TAG, "loadBanner: " + builder.getListId());
        if (Admob.isShowAllAds) {
            if (adView != null) {
                adView.destroy();
            }
            adView = Admob.getInstance().loadCollapsibleBannerFloorWithReload(context, adWidth, frContainer, builder.getListId(), builder.getBannerGravity(), builder.getCallBack(), () -> {
                if (countDownTimer != null) {
                    countDownTimer.cancel();
                    countDownTimer.start();
                }
            });
        } else {
            frContainer.setVisibility(View.GONE);
        }
    }


    public void setReloadAds() {
        isReloadAds = true;
    }

    public void reloadAdNow() {
        if (isLoadBannerFragment) {
            loadCollapseBannerFragment(frContainer);
        } else {
            loadCollapseBanner(frContainer);
        }
    }

    public void setAlwaysReloadOnResume(boolean isAlwaysReloadOnResume) {
        this.isAlwaysReloadOnResume = isAlwaysReloadOnResume;
    }
}
