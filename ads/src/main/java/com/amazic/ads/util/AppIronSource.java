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
import com.amazic.ads.dialog.LoadingAdsDialog;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.ironsource.mediationsdk.ISBannerSize;
import com.ironsource.mediationsdk.IronSource;
import com.ironsource.mediationsdk.IronSourceBannerLayout;
import com.ironsource.mediationsdk.integration.IntegrationHelper;
import com.ironsource.mediationsdk.logger.IronSourceError;
import com.ironsource.mediationsdk.sdk.BannerListener;
import com.ironsource.mediationsdk.sdk.InterstitialListener;

public class AppIronSource {
    private static final String TAG = "AppIronSource";
    private static AppIronSource instance;
    private IronSourceBannerLayout mIronSourceBannerLayout;
    private LoadingAdsDialog dialog;
    private boolean openActivityAfterShowInterAds = false;

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

    public void destroyBanner() {
        if (mIronSourceBannerLayout == null)
            return;

        Log.i(TAG, "destroyBanner");
        IronSource.destroyBanner(mIronSourceBannerLayout);
        mIronSourceBannerLayout = null;
    }


    /**
     * Load quảng cáo Banner Trong Activity
     *
     * @param mActivity
     */
    public void loadBanner(final Activity mActivity) {
        destroyBanner();
        final FrameLayout adContainer = mActivity.findViewById(R.id.banner_container);
        final ShimmerFrameLayout containerShimmer = mActivity.findViewById(R.id.shimmer_container_banner);
        loadBanner(mActivity, adContainer, containerShimmer);
    }


    public void loadBanner(Activity activity, FrameLayout mBannerParentLayout, final ShimmerFrameLayout containerShimmer) {

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
    }

    public void loadBannerForPlacement(final Activity mActivity, String placementName) {
        destroyBanner();
        final FrameLayout adContainer = mActivity.findViewById(R.id.banner_container);
        final ShimmerFrameLayout containerShimmer = mActivity.findViewById(R.id.shimmer_container_banner);
        loadBannerForPlacement(mActivity, placementName, adContainer, containerShimmer);
    }

    public void loadBannerForPlacement(Activity activity, String placementName, FrameLayout mBannerParentLayout, final ShimmerFrameLayout containerShimmer) {

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
            IronSource.loadBanner(mIronSourceBannerLayout, placementName);
        } else {
            Log.e(TAG, "loadBanner :IronSource.createBanner returned null");
            containerShimmer.stopShimmer();
            mBannerParentLayout.setVisibility(View.GONE);
            containerShimmer.setVisibility(View.GONE);
        }
    }

    private boolean isTimeout; // xử lý timeout show ads
    private Handler handlerTimeout;
    private Runnable rdTimeout;

    public void loadSplashInterstitial(Activity activity, InterCallback adListener, int timeOut) {
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
                Log.i(TAG, "onInterstitialAdClosed: ");
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
            }

            @Override
            public void onInterstitialAdClicked() {
                adListener.onAdClicked();
                Log.i(TAG, "onInterstitialAdClicked: ");
            }
        });
        IronSource.loadInterstitial();
    }

    public void loadInterstitial(Activity activity, InterCallback adListener) {
        IronSource.setInterstitialListener(new InterstitialListener() {
            @Override
            public void onInterstitialAdReady() {
                adListener.onAdLoaded();
                Log.d(TAG, "onInterstitialAdReady: ");
            }

            @Override
            public void onInterstitialAdLoadFailed(IronSourceError ironSourceError) {
                adListener.onAdFailedToLoadIs();
                Log.d(TAG, "onInterstitialAdLoadFailed: " + ironSourceError.getErrorMessage());
            }

            @Override
            public void onInterstitialAdOpened() {
                Log.d(TAG, "onInterstitialAdOpened: ");

            }

            @Override
            public void onInterstitialAdClosed() {
                Log.d(TAG, "onInterstitialAdClosed: ");
                adListener.onAdClosed();
                if (AppOpenManager.getInstance().isInitialized()) {
                    AppOpenManager.getInstance().enableAppResume();
                }
            }

            @Override
            public void onInterstitialAdShowSucceeded() {
                Log.d(TAG, "onInterstitialAdShowSucceeded: ");
            }

            @Override
            public void onInterstitialAdShowFailed(IronSourceError ironSourceError) {
                Log.d(TAG, "onInterstitialAdShowFailed: ");
            }

            @Override
            public void onInterstitialAdClicked() {
                adListener.onAdClicked();
                Log.d(TAG, "onInterstitialAdClicked: ");
            }
        });
        IronSource.loadInterstitial();
    }

    public void showInterstitial() {
        if (IronSource.isInterstitialReady()) {
            //show the interstitial
            IronSource.showInterstitial();
        }
    }

    public void showInterstitial(Context context, InterCallback adListener) {
        if (handlerTimeout != null && rdTimeout != null) { // cancel check timeout
            handlerTimeout.removeCallbacks(rdTimeout);
        }
        if (IronSource.isInterstitialReady()) {
            IronSource.setInterstitialListener(new InterstitialListener() {
                @Override
                public void onInterstitialAdReady() {
                    adListener.onAdLoaded();
                    Log.d(TAG, "onInterstitialAdReady: ");
                }

                @Override
                public void onInterstitialAdLoadFailed(IronSourceError ironSourceError) {
                    adListener.onAdFailedToLoadIs();
                    Log.d(TAG, "onInterstitialAdLoadFailed: " + ironSourceError.getErrorMessage());
                }

                @Override
                public void onInterstitialAdOpened() {
                    Log.d(TAG, "onInterstitialAdOpened: ");

                }

                @Override
                public void onInterstitialAdClosed() {
                    Log.d(TAG, "onInterstitialAdClosed: ");
                    try {
                        if (dialog != null && !((Activity) context).isDestroyed())
                            dialog.dismiss();
                        if (AppOpenManager.getInstance().isInitialized()) {
                            AppOpenManager.getInstance().enableAppResume();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    if (!openActivityAfterShowInterAds) {
                        adListener.onAdClosed();
                    }
                }

                @Override
                public void onInterstitialAdShowSucceeded() {
                    Log.d(TAG, "onInterstitialAdShowSucceeded: ");
                }

                @Override
                public void onInterstitialAdShowFailed(IronSourceError ironSourceError) {
                    Log.d(TAG, "onInterstitialAdShowFailed: ");
                    if (AppOpenManager.getInstance().isInitialized()) {
                        AppOpenManager.getInstance().enableAppResume();
                    }
                }

                @Override
                public void onInterstitialAdClicked() {
                    adListener.onAdClicked();
                    Log.d(TAG, "onInterstitialAdClicked: ");
                }
            });
            if (ProcessLifecycleOwner.get().getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.RESUMED)) {
                try {
                    if (dialog != null && dialog.isShowing())
                        dialog.dismiss();
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
                    }
                    IronSource.showInterstitial();
                }, 800);

            }

        }
    }

    public void showInterstitialForPlacements(String placementName) {
        if (IronSource.isInterstitialReady()) {
            //show the interstitial
            Log.d(TAG, "showInterstitialForPlacements: " + IronSource.isInterstitialPlacementCapped(placementName));
            IronSource.showInterstitial(placementName);
        }
    }

    public void showInterstitialForPlacements(Context context, String placementName, InterCallback adListener) {
        if (handlerTimeout != null && rdTimeout != null) { // cancel check timeout
            handlerTimeout.removeCallbacks(rdTimeout);
        }
        if (IronSource.isInterstitialReady()) {
            IronSource.setInterstitialListener(new InterstitialListener() {
                @Override
                public void onInterstitialAdReady() {
                    adListener.onAdLoaded();
                    Log.d(TAG, "onInterstitialAdReady: ");
                }

                @Override
                public void onInterstitialAdLoadFailed(IronSourceError ironSourceError) {
                    adListener.onAdFailedToLoadIs();
                    Log.d(TAG, "onInterstitialAdLoadFailed: " + ironSourceError.getErrorMessage());
                }

                @Override
                public void onInterstitialAdOpened() {
                    Log.d(TAG, "onInterstitialAdOpened: ");

                }

                @Override
                public void onInterstitialAdClosed() {
                    Log.d(TAG, "onInterstitialAdClosed: ");
                    try {
                        if (dialog != null && !((Activity) context).isDestroyed())
                            dialog.dismiss();
                        if (AppOpenManager.getInstance().isInitialized()) {
                            AppOpenManager.getInstance().enableAppResume();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    if (!openActivityAfterShowInterAds) {
                        adListener.onAdClosed();
                    }
                }

                @Override
                public void onInterstitialAdShowSucceeded() {
                    Log.d(TAG, "onInterstitialAdShowSucceeded: ");
                }

                @Override
                public void onInterstitialAdShowFailed(IronSourceError ironSourceError) {
                    Log.d(TAG, "onInterstitialAdShowFailed: ");
                    if (AppOpenManager.getInstance().isInitialized()) {
                        AppOpenManager.getInstance().enableAppResume();
                    }
                }

                @Override
                public void onInterstitialAdClicked() {
                    adListener.onAdClicked();
                    Log.d(TAG, "onInterstitialAdClicked: ");
                }
            });
            if (ProcessLifecycleOwner.get().getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.RESUMED)) {
                try {
                    if (dialog != null && dialog.isShowing())
                        dialog.dismiss();
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
                    }
                    IronSource.showInterstitial();
                }, 800);

            }

        }
    }

    public boolean isInterstitialReady() {
        return IronSource.isInterstitialReady();
    }

    public void loadAndShowInter(Activity activity, int timeOut, InterCallback adListener) {
        try {
            if (dialog != null && dialog.isShowing())
                dialog.dismiss();
            dialog = new LoadingAdsDialog(activity);
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
        isTimeout = false;
        if (timeOut > 0) {
            handlerTimeout = new Handler();
            rdTimeout = new Runnable() {
                @Override
                public void run() {
                    Log.e(TAG, "loadSplashInterstitalAds: on timeout");
                    isTimeout = true;
                    if (IronSource.isInterstitialReady()) {
                        loadAndShowInterstitial(activity, adListener, dialog);
                        return;
                    }
                    if (adListener != null) {
                        adListener.onAdClosed();
                    }
                }
            };
            handlerTimeout.postDelayed(rdTimeout, timeOut);
        }

        IronSource.setInterstitialListener(new InterstitialListener() {
            @Override
            public void onInterstitialAdReady() {

                if (!isTimeout)
                    loadAndShowInterstitial(activity, adListener, dialog);

                Log.i(TAG, "onInterstitialAdReady xxx: ");
            }

            @Override
            public void onInterstitialAdLoadFailed(IronSourceError ironSourceError) {
                if (handlerTimeout != null && rdTimeout != null) {
                    handlerTimeout.removeCallbacks(rdTimeout);
                }
                dialog.dismiss();
                Log.e(TAG, "onInterstitialAdLoadFailed xxx: " + ironSourceError.getErrorMessage());
                adListener.onAdFailedToLoadIs();
            }

            @Override
            public void onInterstitialAdOpened() {
                Log.i(TAG, "onInterstitialAdOpened xxx: ");

            }

            @Override
            public void onInterstitialAdClosed() {
                dialog.dismiss();
                Log.i(TAG, "onInterstitialAdClosed: ");
                adListener.onAdClosed();
                if (AppOpenManager.getInstance().isInitialized()) {
                    AppOpenManager.getInstance().enableAppResume();
                }
            }

            @Override
            public void onInterstitialAdShowSucceeded() {
                dialog.dismiss();
                Log.i(TAG, "onInterstitialAdShowSucceeded xxx: ");
            }

            @Override
            public void onInterstitialAdShowFailed(IronSourceError ironSourceError) {
                dialog.dismiss();
                if (AppOpenManager.getInstance().isInitialized()) {
                    AppOpenManager.getInstance().enableAppResume();
                }
                Log.i(TAG, "onInterstitialAdShowFailed xxx: ");
            }

            @Override
            public void onInterstitialAdClicked() {
                dialog.dismiss();
                adListener.onAdClicked();
                Log.i(TAG, "onInterstitialAdClicked xxx: ");
            }
        });
        IronSource.loadInterstitial();
    }


    private void loadAndShowInterstitial(Context context, InterCallback adListener, Dialog dialog) {
        if (handlerTimeout != null && rdTimeout != null) { // cancel check timeout
            handlerTimeout.removeCallbacks(rdTimeout);
        }
        if (IronSource.isInterstitialReady()) {
            IronSource.setInterstitialListener(new InterstitialListener() {
                @Override
                public void onInterstitialAdReady() {
                    adListener.onAdLoaded();
                    Log.d(TAG, "onInterstitialAdReady: ");
                }

                @Override
                public void onInterstitialAdLoadFailed(IronSourceError ironSourceError) {
                    dialog.dismiss();
                    adListener.onAdFailedToLoadIs();
                }

                @Override
                public void onInterstitialAdOpened() {
                    Log.d(TAG, "onInterstitialAdOpened: ");
                }

                @Override
                public void onInterstitialAdClosed() {
                    Log.d(TAG, "onInterstitialAdClosed: ");
                    try {
                        dialog.dismiss();
                        if (AppOpenManager.getInstance().isInitialized()) {
                            AppOpenManager.getInstance().enableAppResume();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    if (!openActivityAfterShowInterAds) {
                        adListener.onAdClosed();
                    }
                }

                @Override
                public void onInterstitialAdShowSucceeded() {
                    try {
                        dialog.dismiss();
                    } catch (Exception e) {

                    }

                    Log.d(TAG, "onInterstitialAdShowSucceeded: ");
                }

                @Override
                public void onInterstitialAdShowFailed(IronSourceError ironSourceError) {
                    Log.d(TAG, "onInterstitialAdShowFailed: ");
                    if (AppOpenManager.getInstance().isInitialized()) {
                        AppOpenManager.getInstance().enableAppResume();
                    }
                }

                @Override
                public void onInterstitialAdClicked() {
                    adListener.onAdClicked();
                    Log.d(TAG, "onInterstitialAdClicked: ");
                }
            });
            if (ProcessLifecycleOwner.get().getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.RESUMED)) {
                new Handler().postDelayed(() -> {
                    if (AppOpenManager.getInstance().isInitialized()) {
                        AppOpenManager.getInstance().disableAppResume();
                    }

                    if (openActivityAfterShowInterAds && adListener != null) {
                        adListener.onAdClosed();
                    }
                    IronSource.showInterstitial();
                }, 800);

            }

        }
    }

    private void loadAndShowInterstitial(Context context, String placemenName, InterCallback adListener, Dialog dialog) {
        if (handlerTimeout != null && rdTimeout != null) { // cancel check timeout
            handlerTimeout.removeCallbacks(rdTimeout);
        }
        if (IronSource.isInterstitialReady()) {
            IronSource.setInterstitialListener(new InterstitialListener() {
                @Override
                public void onInterstitialAdReady() {
                    adListener.onAdLoaded();
                    Log.d(TAG, "onInterstitialAdReady: ");
                }

                @Override
                public void onInterstitialAdLoadFailed(IronSourceError ironSourceError) {
                    dialog.dismiss();
                    adListener.onAdFailedToLoadIs();
                }

                @Override
                public void onInterstitialAdOpened() {
                    Log.d(TAG, "onInterstitialAdOpened: ");
                }

                @Override
                public void onInterstitialAdClosed() {
                    Log.d(TAG, "onInterstitialAdClosed: ");
                    try {
                        dialog.dismiss();
                        if (AppOpenManager.getInstance().isInitialized()) {
                            AppOpenManager.getInstance().enableAppResume();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    if (!openActivityAfterShowInterAds) {
                        adListener.onAdClosed();
                    }
                }

                @Override
                public void onInterstitialAdShowSucceeded() {
                    try {
                        dialog.dismiss();
                    } catch (Exception e) {

                    }

                    Log.d(TAG, "onInterstitialAdShowSucceeded: ");
                }

                @Override
                public void onInterstitialAdShowFailed(IronSourceError ironSourceError) {
                    Log.d(TAG, "onInterstitialAdShowFailed: ");
                    if (AppOpenManager.getInstance().isInitialized()) {
                        AppOpenManager.getInstance().enableAppResume();
                    }
                }

                @Override
                public void onInterstitialAdClicked() {
                    adListener.onAdClicked();
                    Log.d(TAG, "onInterstitialAdClicked: ");
                }
            });
            if (ProcessLifecycleOwner.get().getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.RESUMED)) {
                new Handler().postDelayed(() -> {
                    if (AppOpenManager.getInstance().isInitialized()) {
                        AppOpenManager.getInstance().disableAppResume();
                    }

                    if (openActivityAfterShowInterAds && adListener != null) {
                        adListener.onAdClosed();
                    }
                    IronSource.showInterstitial(placemenName);
                }, 800);
            }

        }
    }
}
