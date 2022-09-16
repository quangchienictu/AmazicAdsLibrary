package com.amazic.ads.util;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;

import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ProcessLifecycleOwner;

import com.amazic.ads.R;
import com.amazic.ads.callback.InterCallback;
import com.amazic.ads.callback.RewardCallback;
import com.amazic.ads.dialog.LoadingAdsDialog;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.gms.ads.rewarded.RewardItem;
import com.ironsource.mediationsdk.ISBannerSize;
import com.ironsource.mediationsdk.IronSource;
import com.ironsource.mediationsdk.IronSourceBannerLayout;
import com.ironsource.mediationsdk.integration.IntegrationHelper;
import com.ironsource.mediationsdk.logger.IronSourceError;
import com.ironsource.mediationsdk.model.Placement;
import com.ironsource.mediationsdk.sdk.BannerListener;
import com.ironsource.mediationsdk.sdk.InterstitialListener;
import com.ironsource.mediationsdk.sdk.RewardedVideoListener;

public class AppIronSource {
    private static final String TAG = "AppIronSource";
    private static AppIronSource instance;
    private IronSourceBannerLayout mIronSourceBannerLayout;
    private LoadingAdsDialog dialog;
    private boolean openActivityAfterShowInterAds = false;
    public boolean isShowInter = true;
    public boolean isShowBanner = true;
    public long timeLimit = 0;
    private boolean isTimeout; // xử lý timeout show ads
    private Handler handlerTimeout;
    private Runnable rdTimeout;
    private boolean isShowAllAds = true;

    public static AppIronSource getInstance() {
        if (instance == null) {
            instance = new AppIronSource();
        }
        return instance;
    }


    public void init(Activity activity, String app_key) {
        IntegrationHelper.validateIntegration(activity);
        String userId = IronSource.getAdvertiserId(activity);
        IronSource.setUserId(userId);
        IronSource.init(activity, app_key);
    }

    public void init(Activity activity, String app_key, boolean isDebug) {
        IntegrationHelper.validateIntegration(activity);
        String userId = IronSource.getAdvertiserId(activity);
        IronSource.setUserId(userId);
        IronSource.init(activity, app_key);
        IronSource.setAdaptersDebug(isDebug);
    }

    public void initBanner(Activity activity, String app_key, boolean isDebug) {
        IntegrationHelper.validateIntegration(activity);
        String userId = IronSource.getAdvertiserId(activity);
        IronSource.setUserId(userId);
        IronSource.init(activity, app_key, IronSource.AD_UNIT.BANNER);
        IronSource.setAdaptersDebug(isDebug);
    }


    public void setOpenActivityAfterShowInterAds(boolean openActivityAfterShowInterAds) {
        this.openActivityAfterShowInterAds = openActivityAfterShowInterAds;
    }

    public void onResume(Activity activity) {
        IronSource.onResume(activity);
    }

    public void onPause(Activity activity) {
        IronSource.onPause(activity);
    }


    /**
     * Set tắt toàn bộ ads trong project
     **/
    public void setShowAllAds(boolean isShowAllAds){
        this.isShowAllAds = isShowAllAds;
    }


    public void destroyBanner() {
        if (mIronSourceBannerLayout == null)
            return;

        Log.i(TAG, "destroyBanner");
        IronSource.destroyBanner(mIronSourceBannerLayout);
        mIronSourceBannerLayout = null;
    }


    /**
     * Load quảng cáo Banner Trong Activity
     */
    public void loadBanner(final Activity mActivity) {
        final FrameLayout adContainer = mActivity.findViewById(R.id.banner_container);
        final ShimmerFrameLayout containerShimmer = mActivity.findViewById(R.id.shimmer_container_banner);
        if(isShowAllAds){
            destroyBanner();
            loadBanner(mActivity, adContainer, containerShimmer);
        }else{
            adContainer.removeAllViews();
            containerShimmer.setVisibility(View.GONE);
        }

    }

    public void loadBanner(Activity activity, FrameLayout mBannerParentLayout, final ShimmerFrameLayout containerShimmer) {
        if (isShowBanner) {
            //show shimmer loading
            containerShimmer.setVisibility(View.VISIBLE);
            containerShimmer.startShimmer();

            ISBannerSize size = ISBannerSize.BANNER;
            // instantiate IronSourceBanner object, using the IronSource.createBanner API
            mIronSourceBannerLayout = IronSource.createBanner(activity, size);

            // add IronSourceBanner to your container
            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT);
            mBannerParentLayout.addView(mIronSourceBannerLayout, 0, layoutParams);

            if (mIronSourceBannerLayout != null) {
                // set the banner listener
                mIronSourceBannerLayout.setBannerListener(new BannerListener() {
                    @Override
                    public void onBannerAdLoaded() {
                        Log.d(TAG, "onBannerAdLoaded");
                        // since banner container was "gone" by default, we need to make it visible as soon as the banner is ready
                        containerShimmer.stopShimmer();
                        containerShimmer.setVisibility(View.GONE);
                        mBannerParentLayout.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onBannerAdLoadFailed(IronSourceError error) {
                        Log.d(TAG, "onBannerAdLoadFailed" + " " + error);
                        containerShimmer.stopShimmer();
                        mBannerParentLayout.setVisibility(View.GONE);
                        containerShimmer.setVisibility(View.GONE);
                    }

                    @Override
                    public void onBannerAdClicked() {
                        Log.d(TAG, "onBannerAdClicked");
                        FirebaseAnalyticsUtil.logClickAdsEventIS(activity, "Banner");
                        FirebaseAnalyticsUtil.logClickAdsISEventByActivity(activity, FirebaseAnalyticsUtil.BANNER);
                        setTimeLimitBanner();
                        containerShimmer.stopShimmer();
                        mBannerParentLayout.setVisibility(View.GONE);
                        containerShimmer.setVisibility(View.GONE);
                    }

                    @Override
                    public void onBannerAdScreenPresented() {
                        Log.d(TAG, "onBannerAdScreenPresented");
                    }

                    @Override
                    public void onBannerAdScreenDismissed() {
                        Log.d(TAG, "onBannerAdScreenDismissed");
                    }

                    @Override
                    public void onBannerAdLeftApplication() {
                        Log.d(TAG, "onBannerAdLeftApplication");
                    }
                });

                // load ad into the created banner
                IronSource.loadBanner(mIronSourceBannerLayout);
            } else {
                Log.e(TAG, "loadBanner :IronSource.createBanner returned null");
                containerShimmer.stopShimmer();
                mBannerParentLayout.setVisibility(View.GONE);
                containerShimmer.setVisibility(View.GONE);
            }
        } else {
            containerShimmer.stopShimmer();
            mBannerParentLayout.setVisibility(View.GONE);
            containerShimmer.setVisibility(View.GONE);
        }

    }


    public void loadSplashInterstitial(Activity activity, InterCallback adListener, int timeOut) {
        if(!isShowAllAds){
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    adListener.onAdClosed();
                    return;
                }
            },3000);
        }else {
            isTimeout = false;
            if (timeOut > 0) {
                handlerTimeout = new Handler();
                rdTimeout = new Runnable() {
                    @Override
                    public void run() {
                        Log.e(TAG, "loadSplashInterstitalAds: on timeout");
                        isTimeout = true;
                        if (IronSource.isInterstitialReady()) {
                            showInterstitial(activity, adListener);
                            return;
                        }
                        if (adListener != null) {
                            adListener.onAdClosed();
                            loadInterAds();
                        }
                    }
                };
                handlerTimeout.postDelayed(rdTimeout, timeOut);
            }

            IronSource.setInterstitialListener(new InterstitialListener() {
                @Override
                public void onInterstitialAdReady() {

                    if (!isTimeout)
                        showInterstitial(activity, adListener);

                    Log.i(TAG, "onInterstitialAdReady: ");
                }

                @Override
                public void onInterstitialAdLoadFailed(IronSourceError ironSourceError) {
                    if (handlerTimeout != null && rdTimeout != null) {
                        handlerTimeout.removeCallbacks(rdTimeout);
                    }
                    Log.e(TAG, "onInterstitialAdLoadFailed: " + ironSourceError.getErrorMessage());
                    adListener.onAdFailedToLoadIs();
                }

                @Override
                public void onInterstitialAdOpened() {
                    Log.i(TAG, "onInterstitialAdOpened: ");

                }

                @Override
                public void onInterstitialAdClosed() {
                    Log.i(TAG, "onInterstitialAdClosed : ");
                    adListener.onAdClosed();
                    if (AppOpenManager.getInstance().isInitialized()) {
                        AppOpenManager.getInstance().enableAppResume();
                    }
                }

                @Override
                public void onInterstitialAdShowSucceeded() {
                    Log.i(TAG, "onInterstitialAdShowSucceeded: ");
                }

                @Override
                public void onInterstitialAdShowFailed(IronSourceError ironSourceError) {
                    Log.i(TAG, "onInterstitialAdShowFailed: ");
                    if (AppOpenManager.getInstance().isInitialized()) {
                        AppOpenManager.getInstance().enableAppResume();
                    }
                    loadInterAds();
                }

                @Override
                public void onInterstitialAdClicked() {
                    adListener.onAdClicked();
                    FirebaseAnalyticsUtil.logClickAdsISEventByActivity(activity,FirebaseAnalyticsUtil.INTER);
                    Log.i(TAG, "inter splash click ");
                }
            });
            IronSource.loadInterstitial();
        }
    }

    public void loadInterstitial(Activity activity, InterCallback adListener) {
        if (!isInterstitialReady()) {
            IronSource.setInterstitialListener(new InterstitialListener() {
                @Override
                public void onInterstitialAdReady() {
                    adListener.onAdLoaded();
                    Log.d(TAG, "onInterstitialAdReady: 1");
                }

                @Override
                public void onInterstitialAdLoadFailed(IronSourceError ironSourceError) {
                    adListener.onAdFailedToLoadIs();
                    Log.d(TAG, "onInterstitialAdLoadFailed: 1" + ironSourceError.getErrorMessage());
                }

                @Override
                public void onInterstitialAdOpened() {
                    Log.d(TAG, "onInterstitialAdOpened: 1");

                }

                @Override
                public void onInterstitialAdClosed() {
                    Log.d(TAG, "onInterstitialAdClosed: 1");
                    adListener.onAdClosed();
                    if (AppOpenManager.getInstance().isInitialized()) {
                        AppOpenManager.getInstance().enableAppResume();
                    }
                    loadInterAds();
                }

                @Override
                public void onInterstitialAdShowSucceeded() {
                    Log.d(TAG, "onInterstitialAdShowSucceeded: 1");
                }

                @Override
                public void onInterstitialAdShowFailed(IronSourceError ironSourceError) {
                    Log.d(TAG, "onInterstitialAdShowFailed: 1");
                    loadInterAds();
                }

                @Override
                public void onInterstitialAdClicked() {
                    FirebaseAnalyticsUtil.logClickAdsEventIS(activity, "Interstitial");
                    FirebaseAnalyticsUtil.logClickAdsISEventByActivity(activity, FirebaseAnalyticsUtil.INTER);
                    Log.d(TAG, "onInterstitialAdClicked: loadInterstitiale");
                    adListener.onAdClicked();
                    setTimeLimitInter();
                }
            });
            IronSource.loadInterstitial();
        } else {
            adListener.onAdLoaded();
        }



    }

    public void showInterstitial() {
        if (IronSource.isInterstitialReady()) {
            IronSource.showInterstitial();
        }
    }

    public void showInterstitial(Context context, InterCallback adListener) {
        if(!isShowAllAds){
            adListener.onAdClosed();
            return;
        }
        if (isInterstitialReady()) {
            if (handlerTimeout != null && rdTimeout != null) { // cancel check timeout
                handlerTimeout.removeCallbacks(rdTimeout);
            }
            if (IronSource.isInterstitialReady()) {
                if (AppOpenManager.getInstance().isInitialized()) {
                    AppOpenManager.getInstance().disableAppResume();
                }
                IronSource.setInterstitialListener(new InterstitialListener() {
                    @Override
                    public void onInterstitialAdReady() {
                        adListener.onAdLoaded();
                        Log.d(TAG, "onInterstitialAdReady: 2");
                    }

                    @Override
                    public void onInterstitialAdLoadFailed(IronSourceError ironSourceError) {
                        adListener.onAdFailedToLoadIs();
                        Log.d(TAG, "onInterstitialAdLoadFailed: " + ironSourceError.getErrorMessage());
                    }

                    @Override
                    public void onInterstitialAdOpened() {
                        Log.d(TAG, "onInterstitialAdOpened: 2");
                        if (AppOpenManager.getInstance().isInitialized()) {
                            AppOpenManager.getInstance().disableAppResume();
                        }
                        if(!openActivityAfterShowInterAds){
                            if (dialog != null && !((Activity) context).isDestroyed())
                                dialog.dismiss();
                        }
                    }

                    @Override
                    public void onInterstitialAdClosed() {
                        Log.d(TAG, "onInterstitialAdClosed: 2");
                        if (AppOpenManager.getInstance().isInitialized()) {
                            AppOpenManager.getInstance().enableAppResume();
                        }
                        try {
                            if (dialog != null && !((Activity) context).isDestroyed())
                                dialog.dismiss();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        if (!openActivityAfterShowInterAds) {
                            adListener.onAdClosed();
                        }
                        loadInterAds();
                    }

                    @Override
                    public void onInterstitialAdShowSucceeded() {
                        Log.d(TAG, "onInterstitialAdShowSucceeded: 2");
                        if (AppOpenManager.getInstance().isInitialized()) {
                            AppOpenManager.getInstance().disableAppResume();
                        }
                    }

                    @Override
                    public void onInterstitialAdShowFailed(IronSourceError ironSourceError) {
                        Log.d(TAG, "onInterstitialAdShowFailed: 2");
                        /*if (AppOpenManager.getInstance().isInitialized()) {
                            AppOpenManager.getInstance().enableAppResume();
                        }*/
                        loadInterAds();
                    }

                    @Override
                    public void onInterstitialAdClicked() {
                        adListener.onAdClicked();
                        Log.d(TAG, "onInterstitialAdClicked: showInterstitialx");
                        FirebaseAnalyticsUtil.logClickAdsEventIS(context, "Interstitial_Splash");
                        FirebaseAnalyticsUtil.logClickAdsISEventByActivity(context, FirebaseAnalyticsUtil.INTER);
                        setTimeLimitInter();
                    }
                });
                if (ProcessLifecycleOwner.get().getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.RESUMED)) {
                    Log.d(TAG, "LoadingAdsDialog");
                    try {
                        dialog = new LoadingAdsDialog(context);
                        try {
                            dialog.show();
                        } catch (Exception e) {
                            adListener.onAdClosed();
                            return;
                        }
                    } catch (Exception e) {
                        dialog = null;
                        e.printStackTrace();
                    }
                    new Handler().postDelayed(() -> {
                        if (AppOpenManager.getInstance().isInitialized()) {
                            AppOpenManager.getInstance().disableAppResume();
                        }

                        if (openActivityAfterShowInterAds && adListener != null) {
                            adListener.onAdClosed();
                            if (AppOpenManager.getInstance().isInitialized()) {
                                AppOpenManager.getInstance().disableAppResume();
                            }
                        }
                        IronSource.showInterstitial();
                    }, 800);

                }

            }
        } else {
            adListener.onAdClosed();
        }
    }

    public boolean isInterstitialReady() {
        return IronSource.isInterstitialReady();
    }

    public void loadAndShowRewards(RewardCallback rewardCallback){
        if(!isShowAllAds){
            rewardCallback.onAdFailedToShow(0);
            return;
        }

        if (AppOpenManager.getInstance().isInitialized()) {
            AppOpenManager.getInstance().disableAppResume();
        }
        IronSource.setRewardedVideoListener(new RewardedVideoListener() {
            @Override
            public void onRewardedVideoAdOpened() {

            }

            @Override
            public void onRewardedVideoAdClosed() {
                rewardCallback.onAdClosed();
                if (AppOpenManager.getInstance().isInitialized()) {
                    AppOpenManager.getInstance().enableAppResume();
                }
            }

            @Override
            public void onRewardedVideoAvailabilityChanged(boolean b) {

            }

            @Override
            public void onRewardedVideoAdStarted() {

            }

            @Override
            public void onRewardedVideoAdEnded() {

            }

            @Override
            public void onRewardedVideoAdRewarded(Placement placement) {
                rewardCallback.onEarnedReward(null);
            }

            @Override
            public void onRewardedVideoAdShowFailed(IronSourceError ironSourceError) {
                rewardCallback.onAdFailedToShow(1);
                if (AppOpenManager.getInstance().isInitialized()) {
                    AppOpenManager.getInstance().enableAppResume();
                }
            }

            @Override
            public void onRewardedVideoAdClicked(Placement placement) {

            }
        });

        if (IronSource.isRewardedVideoAvailable()){
            IronSource.showRewardedVideo();
        }
        else{
            rewardCallback.onAdClosed();
        }
    }

    private void setTimeLimitInter() {
        if (timeLimit > 1000) {
            isShowInter = false;
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    isShowInter = true;
                }
            }, timeLimit);
        }
    }

    private void setTimeLimitBanner() {
        if (timeLimit > 1000) {
            isShowBanner = false;
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    isShowBanner = true;
                }
            }, timeLimit);
        }
    }

    public void setTimeLimit(long timeLimit) {
        this.timeLimit = timeLimit;
    }

    public long getTimeLimit() {
        return this.timeLimit;
    }
    public void loadInterAds(){
        if(!IronSource.isInterstitialReady()){
            IronSource.loadInterstitial();
        }
    }
}
