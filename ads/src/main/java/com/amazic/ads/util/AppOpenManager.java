package com.amazic.ads.util;

import android.app.Activity;
import android.app.Application;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.lifecycle.ProcessLifecycleOwner;

import com.amazic.ads.dialog.ResumeLoadingDialog;
import com.google.android.gms.ads.AdActivity;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.appopen.AppOpenAd;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class AppOpenManager implements Application.ActivityLifecycleCallbacks, LifecycleObserver {
    private static final String TAG = "AppOpenManager";
    public static final String AD_UNIT_ID_TEST = "ca-app-pub-3940256099942544/3419835294";

    private static volatile AppOpenManager INSTANCE;
    private AppOpenAd appResumeAd = null;
    private AppOpenAd splashAd = null;
    private AppOpenAd.AppOpenAdLoadCallback loadCallback;
    private FullScreenContentCallback fullScreenContentCallback;

    private String appResumeAdId;
    private String splashAdId;

    private Activity currentActivity;

    private Application myApplication;

    private static boolean isShowingAd = false;
    private long appResumeLoadTime = 0;
    private long splashLoadTime = 0;
    private int splashTimeout = 0;

    private boolean isInitialized = false;// on  - off ad resume on app
    private boolean isAppResumeEnabled = true;
    private boolean enableScreenContentCallback = false; // default =  true when use splash & false after show splash

    private final List<Class> disabledAppOpenList;
    private Class splashActivity;
    private boolean isShowResume = true;
    private boolean isTimeout = false;
    private static final int TIMEOUT_MSG = 11;

    private Handler timeoutHandler;
//            = new Handler(msg -> {
//        if (msg.what == TIMEOUT_MSG) {
//
//                Log.e(TAG, "timeout load ad ");
//                isTimeout = true;
//                enableScreenContentCallback = false;
//                if (fullScreenContentCallback != null) {
//                    fullScreenContentCallback.onAdDismissedFullScreenContent();
//                }
//
//        }
//        return false;
//    });

    /**
     * Constructor
     */
    private AppOpenManager() {
        disabledAppOpenList = new ArrayList<>();
    }

    public static synchronized AppOpenManager getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new AppOpenManager();
        }
        return INSTANCE;
    }

    /**
     * Init AppOpenManager
     *
     * @param application
     */
    public void init(Application application, String appOpenAdId) {
        isInitialized = true;
        this.myApplication = application;
        this.myApplication.registerActivityLifecycleCallbacks(this);
        ProcessLifecycleOwner.get().getLifecycle().addObserver(this);
        this.appResumeAdId = appOpenAdId;
//        if (!Purchase.getInstance().isPurchased(application.getApplicationContext()) &&
//                !isAdAvailable(false) && appOpenAdId != null) {
//            fetchAd(false);
//        }
    }

    public boolean isInitialized() {
        return isInitialized;
    }


    public void setInitialized(boolean initialized) {
        isInitialized = initialized;
    }

    public void setEnableScreenContentCallback(boolean enableScreenContentCallback) {
        this.enableScreenContentCallback = enableScreenContentCallback;
    }

    /**
     * Check app open ads is showing
     *
     * @return
     */
    public boolean isShowingAd() {
        return isShowingAd;
    }

    /**
     * Disable app open app on specific activity
     *
     * @param activityClass
     */
    public void disableAppResumeWithActivity(Class activityClass) {
        Log.d(TAG, "disableAppResumeWithActivity: " + activityClass.getName());
        disabledAppOpenList.add(activityClass);
    }

    public void enableAppResumeWithActivity(Class activityClass) {
        Log.d(TAG, "enableAppResumeWithActivity: " + activityClass.getName());
        disabledAppOpenList.remove(activityClass);
    }

    public void disableAppResume() {
        isAppResumeEnabled = false;
    }

    public void enableAppResume() {
        isAppResumeEnabled = true;
    }

    public void setSplashActivity(Class splashActivity, String adId, int timeoutInMillis) {
        this.splashActivity = splashActivity;
        splashAdId = adId;
        this.splashTimeout = timeoutInMillis;
    }

    public void setAppResumeAdId(String appResumeAdId) {
        this.appResumeAdId = appResumeAdId;
    }

    public void setFullScreenContentCallback(FullScreenContentCallback callback) {
        this.fullScreenContentCallback = callback;
    }

    public void removeFullScreenContentCallback() {
        this.fullScreenContentCallback = null;
    }

    /**
     * Request an ad
     */
    public void fetchAd(final boolean isSplash) {
        Log.d(TAG, "fetchAd: isSplash = " + isSplash);
        if (isAdAvailable(isSplash)) {
            return;
        }

        loadCallback =
                new AppOpenAd.AppOpenAdLoadCallback() {

                    /**
                     * Called when an app open ad has loaded.
                     *
                     * @param ad the loaded app open ad.
                     */


                    @Override
                    public void onAdLoaded(AppOpenAd ad) {
                        Log.d(TAG, "onAppOpenAdLoaded: isSplash = " + isSplash);
                        if (!isSplash) {
                            AppOpenManager.this.appResumeAd = ad;
                            AppOpenManager.this.appResumeLoadTime = (new Date()).getTime();
                        } else {
                            AppOpenManager.this.splashAd = ad;
                            AppOpenManager.this.splashLoadTime = (new Date()).getTime();
                        }


                    }


                    /**
                     * Called when an app open ad has failed to load.
                     *
                     * @param loadAdError the error.
                     */
                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        Log.d(TAG, "onAppOpenAdFailedToLoad: isSplash" + isSplash + " message " + loadAdError.getMessage());
//                        if (isSplash && fullScreenContentCallback!=null)
//                            fullScreenContentCallback.onAdDismissedFullScreenContent();
                    }


                };

        AdRequest request = getAdRequest();
        AppOpenAd.load(
                myApplication, isSplash ? splashAdId : appResumeAdId, request,
                AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT, loadCallback);
    }



    /**
     * Creates and returns ad request.
     */
    private AdRequest getAdRequest() {
        return new AdRequest.Builder().build();
    }

    private boolean wasLoadTimeLessThanNHoursAgo(long loadTime, long numHours) {
        long dateDifference = (new Date()).getTime() - loadTime;
        long numMilliSecondsPerHour = 3600000;
        return (dateDifference < (numMilliSecondsPerHour * numHours));
    }

    /**
     * Utility method that checks if ad exists and can be shown.
     */
    public boolean isAdAvailable(boolean isSplash) {
        long loadTime = isSplash ? splashLoadTime : appResumeLoadTime;
        boolean wasLoadTimeLessThanNHoursAgo = wasLoadTimeLessThanNHoursAgo(loadTime, 4);
        Log.d(TAG, "isAdAvailable: " + wasLoadTimeLessThanNHoursAgo);
        return (isSplash ? splashAd != null : appResumeAd != null)
                && wasLoadTimeLessThanNHoursAgo;
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
    }

    @Override
    public void onActivityStarted(Activity activity) {
        currentActivity = activity;
    }

    @Override
    public void onActivityResumed(Activity activity) {
        currentActivity = activity;
        Log.d(TAG, "onActivityResumed: ");
        if (splashActivity == null) {
            if (!activity.getClass().getName().equals(AdActivity.class.getName())) {
                Log.d(TAG, "onActivityResumed 1: with " + activity.getClass().getName());
                fetchAd(false);
            }
        } else {
            if (!activity.getClass().getName().equals(splashActivity.getName()) && !activity.getClass().getName().equals(AdActivity.class.getName())) {
                Log.d(TAG, "onActivityResumed 2: with " + activity.getClass().getName());
                fetchAd(false);
            }
        }
    }

    @Override
    public void onActivityStopped(Activity activity) {
    }

    @Override
    public void onActivityPaused(Activity activity) {
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {
    }

    @Override
    public void onActivityDestroyed(Activity activity) {
        currentActivity = null;
    }

    public void showAdIfAvailable(final boolean isSplash) {
        // Only show ad if there is not already an app open ad currently showing
        // and an ad is available.
        if (currentActivity == null ) {
            if (fullScreenContentCallback != null && enableScreenContentCallback) {
                fullScreenContentCallback.onAdDismissedFullScreenContent();
            }
            return;
        }

        Log.d(TAG, "showAdIfAvailable: " + ProcessLifecycleOwner.get().getLifecycle().getCurrentState());
        Log.d(TAG, "showAd isSplash: " + isSplash);
        if (!ProcessLifecycleOwner.get().getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED)) {
            Log.d(TAG, "showAdIfAvailable: return");
            if (fullScreenContentCallback != null && enableScreenContentCallback) {
                fullScreenContentCallback.onAdDismissedFullScreenContent();
            }

            return;
        }

        if (!isShowingAd && isAdAvailable(isSplash)) {
            Log.d(TAG, "Will show ad isSplash:" + isSplash);
            if (isSplash) {
                showAdsWithLoading();
            } else {
                showResumeAds();
            }

        } else {
            Log.d(TAG, "Ad is not ready");
            if (!isSplash) {
                fetchAd(false);
            }
        }
    }

    private void showAdsWithLoading() {
        if (ProcessLifecycleOwner.get().getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED)) {
            Dialog dialog = null;
            try {
                dialog = new ResumeLoadingDialog(currentActivity);
                try {
                    dialog.show();
                } catch (Exception e) {
                    if (fullScreenContentCallback != null && enableScreenContentCallback) {
                        fullScreenContentCallback.onAdDismissedFullScreenContent();
                    }
                    return;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            final Dialog finalDialog = dialog;
            new Handler().postDelayed(() -> {
                splashAd.setFullScreenContentCallback(
                        new FullScreenContentCallback() {
                            @Override
                            public void onAdDismissedFullScreenContent() {
                                // Set the reference to null so isAdAvailable() returns false.
                                appResumeAd = null;
                                if (fullScreenContentCallback != null && enableScreenContentCallback) {
                                    fullScreenContentCallback.onAdDismissedFullScreenContent();
                                    enableScreenContentCallback = false;
                                }
                                isShowingAd = false;
                                fetchAd(true);
                            }

                            @Override
                            public void onAdFailedToShowFullScreenContent(AdError adError) {
                                if (fullScreenContentCallback != null && enableScreenContentCallback) {
                                    fullScreenContentCallback.onAdFailedToShowFullScreenContent(adError);
                                }
                            }

                            @Override
                            public void onAdShowedFullScreenContent() {
                                isShowingAd = true;
                                splashAd = null;
                            }


                            @Override
                            public void onAdClicked() {
                                super.onAdClicked();

                            }
                        });
                splashAd.show(currentActivity);
                if (currentActivity != null && !currentActivity.isDestroyed() && finalDialog != null && finalDialog.isShowing()) {
                    Log.d(TAG, "dismiss dialog loading ad open: ");
                    try {
                        finalDialog.dismiss();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }, 800);
        }
    }

    Dialog dialog = null;

    private void showResumeAds() {
        if (appResumeAd == null || currentActivity == null ) {
            return;
        }

        if (ProcessLifecycleOwner.get().getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED)) {

            try {
                dismissDialogLoading();
                dialog = new ResumeLoadingDialog(currentActivity);
                try {
                    dialog.show();
                } catch (Exception e) {
                    if (fullScreenContentCallback != null && enableScreenContentCallback) {
                        fullScreenContentCallback.onAdDismissedFullScreenContent();

                    }
                    return;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            final Dialog finalDialog = dialog;
            new Handler().postDelayed(() -> {
                if (appResumeAd != null) {
                    appResumeAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                        @Override
                        public void onAdDismissedFullScreenContent() {
                            // Set the reference to null so isAdAvailable() returns false.
                            appResumeAd = null;
                            if (fullScreenContentCallback != null && enableScreenContentCallback) {
                                fullScreenContentCallback.onAdDismissedFullScreenContent();
                            }
                            isShowingAd = false;
                            fetchAd(false);

                            if (currentActivity != null && !currentActivity.isDestroyed() && finalDialog != null && finalDialog.isShowing()) {
                                Log.d(TAG, "dismiss dialog loading ad open: ");
                                try {
                                    finalDialog.dismiss();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }

                        @Override
                        public void onAdFailedToShowFullScreenContent(AdError adError) {
                            Log.e(TAG, "onAdFailedToShowFullScreenContent: " + adError.getMessage());
                            if (fullScreenContentCallback != null && enableScreenContentCallback) {
                                fullScreenContentCallback.onAdFailedToShowFullScreenContent(adError);
                            }

                            if (currentActivity != null && !currentActivity.isDestroyed() && finalDialog != null && finalDialog.isShowing()) {
                                Log.d(TAG, "dismiss dialog loading ad open: ");
                                try {
                                    finalDialog.dismiss();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                            appResumeAd = null;
                            isShowingAd = false;
                            fetchAd(false);
                        }

                        @Override
                        public void onAdShowedFullScreenContent() {
                            isShowingAd = true;
                            appResumeAd = null;
                        }

                        @Override
                        public void onAdClicked() {
                            super.onAdClicked();
                            if(Admod.getInstance().getTimeLimit()>1000){
                                setTimeLimitResume();
                            }
                        }
                    });
                    appResumeAd.show(currentActivity);
                }
            }, 1000);
        }
    }

    public void loadAndShowSplashAds(final String adId) {
        isTimeout = false;
        enableScreenContentCallback = true;
        if (currentActivity != null) {
            if (fullScreenContentCallback != null && enableScreenContentCallback) {
                fullScreenContentCallback.onAdDismissedFullScreenContent();
            }
            return;
        }

//        if (isAdAvailable(true)) {
//            showAdIfAvailable(true);
//            return;
//        }

        loadCallback =
                new AppOpenAd.AppOpenAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull AppOpenAd appOpenAd) {
                        Log.d(TAG, "onAppOpenAdLoaded: splash");

                        timeoutHandler.removeCallbacks(runnableTimeout);

                        if (isTimeout) {
                            Log.e(TAG, "onAppOpenAdLoaded: splash timeout");
//                            if (fullScreenContentCallback != null) {
//                                fullScreenContentCallback.onAdDismissedFullScreenContent();
//                                enableScreenContentCallback = false;
//                            }
                        } else {
                            AppOpenManager.this.splashAd = appOpenAd;
                            splashLoadTime = new Date().getTime();

                            showAdIfAvailable(true);
                        }


                    }

                    /**
                     * Called when an app open ad has failed to load.
                     *
                     * @param loadAdError the error.
                     */
                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        Log.e(TAG, "onAppOpenAdFailedToLoad: splash " + loadAdError.getMessage());
                        if (isTimeout) {
                            Log.e(TAG, "onAdFailedToLoad: splash timeout");
                            return;
                        }
                        if (fullScreenContentCallback != null && enableScreenContentCallback) {
                            fullScreenContentCallback.onAdDismissedFullScreenContent();
                            enableScreenContentCallback = false;
                        }
                    }
                };
        AdRequest request = getAdRequest();
        AppOpenAd.load(
                myApplication, splashAdId, request,
                AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT, loadCallback);

        if (splashTimeout > 0) {
            timeoutHandler = new Handler();
            timeoutHandler.postDelayed(runnableTimeout, splashTimeout);
        }
    }

    Runnable runnableTimeout = new Runnable() {
        @Override
        public void run() {
            Log.e(TAG, "timeout load ad ");
            isTimeout = true;
            enableScreenContentCallback = false;
            if (fullScreenContentCallback != null) {
                fullScreenContentCallback.onAdDismissedFullScreenContent();
            }
        }
    };

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    public void onResume() {
        if (!isAppResumeEnabled) {
            Log.d(TAG, "onResume: app resume is disabled");
            return;
        }
        if(!isShowResume){
            return;
        }
        for (Class activity : disabledAppOpenList) {
            if (activity.getName().equals(currentActivity.getClass().getName())) {
                Log.d(TAG, "onStart: activity is disabled");
                return;
            }
        }

        if (splashActivity != null && splashActivity.getName().equals(currentActivity.getClass().getName())) {
            String adId = splashAdId;
            if (adId == null) {
                Log.e(TAG, "splash ad id must not be null");
            }
            Log.d(TAG, "onStart: load and show splash ads");
            loadAndShowSplashAds(adId);
            return;
        }

        Log.d(TAG, "onStart: show resume ads");
        showAdIfAvailable(false);
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    public void onStop() {
        Log.d(TAG, "onStop: app stop");

    }

    private void dismissDialogLoading() {
        if (dialog != null && dialog.isShowing()) {
            try {
                dialog.dismiss();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void setTimeLimitResume() {
        if (Admod.getInstance().getTimeLimit() > 1000) {
            isShowResume = false;
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    isShowResume = true;
                }
            }, Admod.getInstance().getTimeLimit());
        }

    }
}

