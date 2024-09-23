package com.amazic.ads.util;

import android.app.Activity;
import android.app.Application;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.lifecycle.ProcessLifecycleOwner;

import com.adjust.sdk.Adjust;
import com.adjust.sdk.AdjustAdRevenue;
import com.adjust.sdk.AdjustConfig;
import com.amazic.ads.callback.AdCallback;
import com.amazic.ads.dialog.LoadingAdsDialog;
import com.amazic.ads.dialog.ResumeLoadingDialog;
import com.amazic.ads.event.AdType;
import com.amazic.ads.event.AdmobEvent;
import com.amazic.ads.event.FirebaseUtil;
import com.amazic.ads.service.AdmobApi;
import com.google.android.gms.ads.AdActivity;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdValue;
import com.google.android.gms.ads.AdapterResponseInfo;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.appopen.AppOpenAd;

import java.util.ArrayList;
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
    private boolean isShowLoadingSplash = false; //kiểm tra trạng thái ad splash, ko cho load, show khi đang show loading ads splash
    private String appResumeAdId;
    private List<String> listAppResumeID = new ArrayList<>();
    private String splashAdId;

    private Activity currentActivity;

    private Application myApplication;

    private static boolean isShowingAd = false;
    private long appResumeLoadTime = 0;
    private long splashLoadTime = 0;
    private int splashTimeout = 0;

    private boolean isInitialized = false;// on  - off ad resume on app
    private boolean isAppResumeEnabled = true;
    private boolean isInterstitialShowing = false;
    private boolean enableScreenContentCallback = false; // default =  true when use splash & false after show splash
    private boolean disableAdResumeByClickAction = false;
    private final List<Class> disabledAppOpenList = new ArrayList<>();
    private Class splashActivity;
    public static boolean checkLoadResume = false;
    private Class welcomeBackClass = null;
    private boolean isTimeout = false;
    private static final int TIMEOUT_MSG = 11;
    private boolean isShowWelcomeAfterAdsResume = false;

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
    public void setSplashAdId(String splashAdId) {
        this.splashAdId = splashAdId;
    }

    /**
     * Constructor
     */
    private AppOpenManager() {
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
    boolean isUsingApi = false;

    public void init(Application application, String appOpenAdId) {
        isUsingApi = false;
        isInitialized = true;
        disableAdResumeByClickAction = false;
        this.myApplication = application;
        this.myApplication.registerActivityLifecycleCallbacks(this);
        ProcessLifecycleOwner.get().getLifecycle().addObserver(this);
        this.appResumeAdId = appOpenAdId;
//        if (!Purchase.getInstance().isPurchased(application.getApplicationContext()) &&
//                !isAdAvailable(false) && appOpenAdId != null) {
//            fetchAd(false);
//        }
    }

    public void initWelcomeBackActivity(Application application, Class welcomeBackClass) {
        isUsingApi = false;
        isInitialized = true;
        disableAdResumeByClickAction = false;
        this.myApplication = application;
        this.welcomeBackClass = welcomeBackClass;
        this.myApplication.registerActivityLifecycleCallbacks(this);
        ProcessLifecycleOwner.get().getLifecycle().addObserver(this);
//        if (!Purchase.getInstance().isPurchased(application.getApplicationContext()) &&
//                !isAdAvailable(false) && appOpenAdId != null) {
//            fetchAd(false);
//        }
    }

    public void initWelcomeBackActivity(Application application, Class welcomeBackClass, boolean isShowWelcomeAfterAdsResume) {
        isUsingApi = false;
        isInitialized = true;
        disableAdResumeByClickAction = false;
        this.myApplication = application;
        this.welcomeBackClass = welcomeBackClass;
        this.isShowWelcomeAfterAdsResume = isShowWelcomeAfterAdsResume;
        this.myApplication.registerActivityLifecycleCallbacks(this);
        ProcessLifecycleOwner.get().getLifecycle().addObserver(this);
//        if (!Purchase.getInstance().isPurchased(application.getApplicationContext()) &&
//                !isAdAvailable(false) && appOpenAdId != null) {
//            fetchAd(false);
//        }
    }

    public void initApi(Application application) {
        isUsingApi = true;
        isInitialized = true;
        disableAdResumeByClickAction = false;
        this.myApplication = application;
        this.myApplication.registerActivityLifecycleCallbacks(this);
        ProcessLifecycleOwner.get().getLifecycle().addObserver(this);
        this.listAppResumeID.clear();
        this.listAppResumeID.addAll(AdmobApi.getInstance().getListIDAppOpenResume());
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

    public boolean isInterstitialShowing() {
        return isInterstitialShowing;
    }

    public void setInterstitialShowing(boolean interstitialShowing) {
        isInterstitialShowing = interstitialShowing;
    }

    /**
     * Call disable ad resume when click a button, auto enable ad resume in next start
     */
    public void disableAdResumeByClickAction() {
        disableAdResumeByClickAction = true;
    }

    public void setDisableAdResumeByClickAction(boolean disableAdResumeByClickAction) {
        this.disableAdResumeByClickAction = disableAdResumeByClickAction;
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
    public void disableAppResumeWithActivity(@NonNull Class activityClass) {
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
        if (isAdAvailable(isSplash) || checkLoadResume) {
            return;
        }
        if (!isSplash) {
            // Log event admob
            AdmobEvent.OPEN_POSITION++;
            Bundle bundle = new Bundle();
            bundle.putInt("ad_open_position", AdmobEvent.OPEN_POSITION);
            AdmobEvent.logEvent(myApplication.getApplicationContext(), "ad_open_load", bundle);
            //end log
        }
        checkLoadResume = true;
        if (Admob.isShowAllAds)
            if (isUsingApi)
                callResumeWithListID(isSplash, listAppResumeID);
            else if (appResumeAdId != null)
                callResumeWithId(isSplash);
    }

    private void callResumeWithListID(boolean isSplash, List<String> listID) {
        List<String> _listID = new ArrayList<>(listID);
        if (listID.isEmpty()) {
            return;
        }
        String appOpenID = listID.get(0);
        Log.d("opaflpd", "==========");
        Log.d("opaflpd", "callResumeWithListID: " + appOpenID);
        AppOpenAd.AppOpenAdLoadCallback callback = new AppOpenAd.AppOpenAdLoadCallback() {
            @Override
            public void onAdLoaded(@NonNull AppOpenAd appOpenAd) {
                onAdAppOpenLoaded(appOpenAd, isSplash);
            }

            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                onAdAppOpenFailed(loadAdError, isSplash);
                _listID.remove(0);
                if (!_listID.isEmpty())
                    callResumeWithListID(isSplash, _listID);
            }
        };
        AppOpenAd.load(myApplication, isSplash ? splashAdId : appOpenID, getAdRequest(), callback);
    }

    private void callResumeWithId(boolean isSplash) {
        loadCallback =
                new AppOpenAd.AppOpenAdLoadCallback() {

                    /**
                     * Called when an app open ad has loaded.
                     *
                     * @param ad the loaded app open ad.
                     */


                    @Override
                    public void onAdLoaded(AppOpenAd ad) {
                        onAdAppOpenLoaded(ad, isSplash);
                    }


                    /**
                     * Called when an app open ad has failed to load.
                     *
                     * @param loadAdError the error.
                     */
                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        onAdAppOpenFailed(loadAdError, isSplash);
                        //end log
                    }
                };


        AdRequest request = getAdRequest();
        AppOpenAd.load(
                myApplication, isSplash ? splashAdId : appResumeAdId, request,
                AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT, loadCallback);
    }

    private void onAdAppOpenFailed(@NonNull LoadAdError loadAdError, boolean isSplash) {
        checkLoadResume = false;
        Log.d(TAG, "onAppOpenAdFailedToLoad: isSplash" + isSplash + " message " + loadAdError.getMessage());
//                        if (isSplash && fullScreenContentCallback!=null)
//                            fullScreenContentCallback.onAdDismissedFullScreenContent();
        dismissDialogLoading();
        // Log event admob
        if (!isSplash) {
            Bundle bundle = new Bundle();
            bundle.putString("ad_open_position", String.valueOf(AdmobEvent.OPEN_POSITION));
            bundle.putString("ad_error_domain", String.valueOf(loadAdError.getDomain()));
            bundle.putString("ad_error_code", String.valueOf(loadAdError.getCode()));
            bundle.putString("ad_error_message", loadAdError.getMessage());
            bundle.putString("ad_error_response", String.valueOf(loadAdError.getResponseInfo()));
            bundle.putString("internet_status", String.valueOf(isNetworkConnected(myApplication.getApplicationContext())));
            AdmobEvent.logEvent(myApplication.getApplicationContext(), "ad_inter_load_failed", bundle);
        }
    }

    private void onAdAppOpenLoaded(AppOpenAd ad, boolean isSplash) {
        checkLoadResume = false;
        Log.d(TAG, "onAppOpenAdLoaded: isSplash = " + isSplash);
        if (!isSplash) {
            // Log event admob
            AdmobEvent.OPEN_POSITION++;
            Bundle bundle = new Bundle();
            bundle.putInt("ad_open_position", AdmobEvent.OPEN_POSITION);
            AdmobEvent.logEvent(myApplication.getApplicationContext(), "ad_open_load_success", bundle);
            //end log


            AppOpenManager.this.appResumeAd = ad;
            AppOpenManager.this.appResumeAd.setOnPaidEventListener(adValue -> {
                trackRevenue(AppOpenManager.this.appResumeAd.getResponseInfo().getLoadedAdapterResponseInfo(), adValue);
            });
            AppOpenManager.this.appResumeLoadTime = (new Date()).getTime();
        } else {
            AppOpenManager.this.splashAd = ad;
            AppOpenManager.this.splashAd.setOnPaidEventListener(adValue -> {
                trackRevenue(AppOpenManager.this.splashAd.getResponseInfo().getLoadedAdapterResponseInfo(), adValue);
                FirebaseUtil.logPaidAdImpression(myApplication.getApplicationContext(),
                        adValue,
                        ad.getAdUnitId(),
                        AdType.APP_OPEN);
            });
            AppOpenManager.this.splashLoadTime = (new Date()).getTime();
        }
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
        Log.d(TAG, "onActivityStarted: " + currentActivity);
    }

    @Override
    public void onActivityResumed(Activity activity) {
        currentActivity = activity;
        Log.d(TAG, "onActivityResumed: " + currentActivity);
        if (Admob.isShowAllAds)
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
        Log.d(TAG, "onActivityDestroyed: null");
    }

    public void showAdIfAvailable(final boolean isSplash) {
        // Only show ad if there is not already an app open ad currently showing
        // and an ad is available.
        if (currentActivity == null) {
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
        if (!isShowingAd && currentActivity.getClass() != welcomeBackClass && welcomeBackClass != null && currentActivity != null) {
            currentActivity.startActivity(new Intent(currentActivity, welcomeBackClass));
            if (!this.isShowWelcomeAfterAdsResume) {
                return;
            }
        }
        if (!isShowingAd && isAdAvailable(isSplash)) {
            Log.d(TAG, "Will show ad isSplash:" + isSplash);
            if (isSplash) {
                showAdsWithLoading();
            } else if (welcomeBackClass == null) {
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
                if (splashAd != null) {
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
                                    AppOpenManager.this.appResumeAd = null;
                                    if (fullScreenContentCallback != null && enableScreenContentCallback) {
                                        fullScreenContentCallback.onAdFailedToShowFullScreenContent(adError);
                                    }
                                }

                                @Override
                                public void onAdShowedFullScreenContent() {
                                    // Log event admob
                                    AdmobEvent.OPEN_POSITION++;
                                    Bundle bundle = new Bundle();
                                    bundle.putInt("ad_open_position", AdmobEvent.OPEN_POSITION);
                                    AdmobEvent.logEvent(myApplication.getApplicationContext(), "ad_open_show", bundle);
                                    //end log
                                    if (fullScreenContentCallback != null && enableScreenContentCallback) {
                                        fullScreenContentCallback.onAdShowedFullScreenContent();
                                    }
                                    isShowingAd = true;
                                }


                                @Override
                                public void onAdClicked() {
                                    super.onAdClicked();
                                    if (currentActivity != null) {
                                        FirebaseUtil.logClickAdsEvent(currentActivity, splashAdId);
                                        if (fullScreenContentCallback != null) {
                                            fullScreenContentCallback.onAdClicked();
                                        }
                                    }
                                }
                            });
                    if (Admob.isShowAllAds)
                        splashAd.show(currentActivity);
                    else
                        fullScreenContentCallback.onAdDismissedFullScreenContent();
                }

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
        if (appResumeAd == null || currentActivity == null) {
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
//            new Handler().postDelayed(() -> {
            if (appResumeAd != null && AdsConsentManager.getConsentResult(currentActivity) && Admob.isShowAllAds) {
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
                        dismissDialogLoading();
                    }

                    @Override
                    public void onAdFailedToShowFullScreenContent(AdError adError) {
                        Log.e(TAG, "onAdFailedToShowFullScreenContent: " + adError.getMessage());
                        if (fullScreenContentCallback != null && enableScreenContentCallback) {
                            fullScreenContentCallback.onAdFailedToShowFullScreenContent(adError);
                        }

                        if (currentActivity != null && !currentActivity.isDestroyed() && dialog != null && dialog.isShowing()) {
                            Log.d(TAG, "dismiss dialog loading ad open: ");
                            try {
                                dialog.dismiss();
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
                        if (currentActivity != null) {
                            AdmobEvent.logEvent(currentActivity, "resume_appopen_view", new Bundle());
                        }
                        if (fullScreenContentCallback != null && enableScreenContentCallback) {
                            fullScreenContentCallback.onAdShowedFullScreenContent();
                        }
                        isShowingAd = true;
                    }

                    @Override
                    public void onAdClicked() {
                        super.onAdClicked();
                        if (currentActivity != null) {
                            AdmobEvent.logEvent(currentActivity, "resume_appopen_click", new Bundle());
                            FirebaseUtil.logClickAdsEvent(currentActivity, appResumeAdId);
                            if (fullScreenContentCallback != null) {
                                fullScreenContentCallback.onAdClicked();
                            }
                        }
                    }

                    @Override
                    public void onAdImpression() {
                        super.onAdImpression();
                        if (currentActivity != null) {
                            if (fullScreenContentCallback != null) {
                                fullScreenContentCallback.onAdImpression();
                            }
                        }
                    }
                });
                appResumeAd.show(currentActivity);
            } else {
                dismissDialogLoading();
            }
//            }, 1000);
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


        if (Admob.isShowAllAds)
            if (isUsingApi)
                callResumeWithListID(true, listAppResumeID);
            else if (appResumeAdId != null)
                callResumeWithId(true);

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

        if (isInterstitialShowing) {
            Log.d(TAG, "onResume: interstitial is showing");
            return;
        }

        if (disableAdResumeByClickAction) {
            Log.d(TAG, "onResume:ad resume disable ad by action");
            disableAdResumeByClickAction = false;
            return;
        }

        if (currentActivity != null)
            for (Class activity : disabledAppOpenList) {
                if (activity != null)
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

        showAdIfAvailable(false);
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    public void onStop() {
        Log.d(TAG, "onStop: app stop");
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    public void onPause() {
        Log.d(TAG, "onPause");
    }

    private void dismissDialogLoading() {
        if (dialog != null) {
            try {
                dialog.dismiss();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void showAppOpenSplash(Context context, AdCallback adCallback) {
        if (this.splashAd == null) {
            adCallback.onNextAction();
            adCallback.onAdFailedToLoad(null);
        } else {
            try {
                dialog = null;
                dialog = new LoadingAdsDialog(context);
                dialog.show();
            } catch (Exception e) {
            }
            /*this.dismissDialogLoading();
            if (this.dialog == null) {
                try {
                    LoadingAdsDialog dialog = new LoadingAdsDialog(context);
                    dialog.setCancelable(false);
                    this.dialog = dialog;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            try {
                if (this.dialog != null) {
                    this.dialog.show();
                }
            } catch (Exception e) {}*/

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (AppOpenManager.this.splashAd != null) {
                        AppOpenManager.this.splashAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                            @Override
                            public void onAdDismissedFullScreenContent() {
                                adCallback.onNextAction();
                                adCallback.onAdClosed();
                                AppOpenManager.this.splashAd = null;
                                AppOpenManager.isShowingAd = false;
                                isShowLoadingSplash = false;
                                if (dialog != null && !AppOpenManager.this.currentActivity.isDestroyed()) {
                                    try {
                                        dialog.dismiss();
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            }

                            @Override
                            public void onAdFailedToShowFullScreenContent(AdError adError) {
                                isShowLoadingSplash = true;
                                AppOpenManager.this.splashAd = null;
                                // adCallback.onNextAction();
                                adCallback.onAdFailedToShow(adError);
                                AppOpenManager.isShowingAd = false;
                                AppOpenManager.this.dismissDialogLoading();
                            }

                            @Override
                            public void onAdShowedFullScreenContent() {
                                adCallback.onAdImpression();
                                AdmobEvent.logEvent(currentActivity, "splash_appopen_view", new Bundle());
                                AppOpenManager.isShowingAd = true;
                            }

                            @Override
                            public void onAdClicked() {
                                super.onAdClicked();
                                adCallback.onAdClicked();
                                AdmobEvent.logEvent(currentActivity, "splash_appopen_click", new Bundle());
                            }
                        });

                        AppOpenManager.this.splashAd.show(AppOpenManager.this.currentActivity);
                    }
                }
            }, 800L);
        }
    }

    public void showAppOpenSplashNew(Context context, AdCallback adCallback) {
        if (this.splashAd == null) {
            adCallback.onNextAction();
            adCallback.onAdFailedToLoad(null);
        } else {
            try {
                dialog = null;
                dialog = new LoadingAdsDialog(context);
                dialog.show();
            } catch (Exception e) {
            }
            /*this.dismissDialogLoading();
            if (this.dialog == null) {
                try {
                    LoadingAdsDialog dialog = new LoadingAdsDialog(context);
                    dialog.setCancelable(false);
                    this.dialog = dialog;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            try {
                if (this.dialog != null) {
                    this.dialog.show();
                }
            } catch (Exception e) {}*/

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (AppOpenManager.this.splashAd != null) {
                        AppOpenManager.this.splashAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                            @Override
                            public void onAdDismissedFullScreenContent() {
                                adCallback.onNextAction();
                                adCallback.onAdClosed();
                                AppOpenManager.this.splashAd = null;
                                AppOpenManager.isShowingAd = false;
                                isShowLoadingSplash = false;
                                if (dialog != null && !AppOpenManager.this.currentActivity.isDestroyed()) {
                                    try {
                                        dialog.dismiss();
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            }

                            @Override
                            public void onAdFailedToShowFullScreenContent(AdError adError) {
                                isShowLoadingSplash = true;
                                adCallback.onNextAction();
                                adCallback.onAdFailedToShow(adError);
                                AppOpenManager.isShowingAd = false;
                                AppOpenManager.this.splashAd = null;
                                AppOpenManager.this.dismissDialogLoading();
                            }

                            @Override
                            public void onAdShowedFullScreenContent() {
                                adCallback.onAdImpression();
                                AdmobEvent.logEvent(currentActivity, "splash_appopen_view", new Bundle());
                                AppOpenManager.isShowingAd = true;
                            }

                            @Override
                            public void onAdClicked() {
                                super.onAdClicked();
                                adCallback.onAdClicked();
                                AdmobEvent.logEvent(currentActivity, "splash_appopen_click", new Bundle());
                            }
                        });

                        AppOpenManager.this.splashAd.show(AppOpenManager.this.currentActivity);
                    }
                }
            }, 800L);
        }
    }


    public void loadOpenAppAdSplash(Context context, String idResumeSplash, long timeDelay, long timeOut, boolean isShowAdIfReady, AdCallback adCallback) {
        this.splashAdId = idResumeSplash;
        if (!isNetworkConnected(context) || !Admob.isShowAllAds || !AdsConsentManager.getConsentResult(context)) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    adCallback.onAdFailedToLoad(null);
                    adCallback.onNextAction();
                }
            }, timeDelay);
        } else {
            long currentTimeMillis = System.currentTimeMillis();
            Runnable timeOutRunnable = () -> {
                Log.d("AppOpenManager", "getAdSplash time out");
                adCallback.onNextAction();
                isShowingAd = false;
            };
            Handler handler = new Handler();
            handler.postDelayed(timeOutRunnable, timeOut);
            AdRequest adRequest = getAdRequest();
            String adUnitId = this.splashAdId;
            AppOpenAd.AppOpenAdLoadCallback appOpenAdLoadCallback = new AppOpenAd.AppOpenAdLoadCallback() {
                @Override
                public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                    super.onAdFailedToLoad(loadAdError);
                    handler.removeCallbacks(timeOutRunnable);
                    adCallback.onAdFailedToLoad(null);
                    adCallback.onNextAction();
                }

                @Override
                public void onAdLoaded(@NonNull AppOpenAd appOpenAd) {
                    super.onAdLoaded(appOpenAd);
                    handler.removeCallbacks(timeOutRunnable);
                    AppOpenManager.this.splashAd = appOpenAd;
                    AppOpenManager.this.splashAd.setOnPaidEventListener((adValue) -> {
                        trackRevenue(AppOpenManager.this.splashAd.getResponseInfo().getLoadedAdapterResponseInfo(), adValue);
                        //log value
                    });
                    appOpenAd.setOnPaidEventListener(adValue -> {
                        trackRevenue(appOpenAd.getResponseInfo().getLoadedAdapterResponseInfo(), adValue);
                        FirebaseUtil.logPaidAdImpression(myApplication.getApplicationContext(),
                                adValue,
                                appOpenAd.getAdUnitId(),
                                AdType.APP_OPEN);
                    });
                    if (isShowAdIfReady) {
                        long elapsedTime = System.currentTimeMillis() - currentTimeMillis;
                        if (elapsedTime >= timeDelay) {
                            elapsedTime = 0L;
                        }
                        Handler handler1 = new Handler();
                        Context appOpenAdContext = context;
                        Runnable showAppOpenSplashRunnable = () -> {
                            AppOpenManager.this.showAppOpenSplash(appOpenAdContext, adCallback);
                        };
                        handler1.postDelayed(showAppOpenSplashRunnable, elapsedTime);
                    } else {
                        adCallback.onAdSplashReady();
                    }
                }
            };
            AppOpenAd.load(context, adUnitId, adRequest, AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT, appOpenAdLoadCallback);
        }

    }

    public void loadOpenAppAdSplashFloor(Context context, List<String> listIDResume, boolean isShowAdIfReady, AdCallback adCallback) {
        if (!isNetworkConnected(context) || !Admob.isShowAllAds || !AdsConsentManager.getConsentResult(context)) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    adCallback.onAdFailedToLoad(null);
                    adCallback.onNextAction();
                }
            }, 3000);
        } else {
            if (listIDResume == null) {
                adCallback.onAdFailedToLoad(null);
                adCallback.onNextAction();
                return;
            }
            if (listIDResume.isEmpty()) {
                adCallback.onAdFailedToLoad(null);
                adCallback.onNextAction();
                return;
            }
            if (listIDResume.size() > 0) {
                Log.e("AppOpenManager", "load ID :" + listIDResume.get(0));
            }
            if (listIDResume.size() < 1) {
                adCallback.onAdFailedToLoad(null);
                adCallback.onNextAction();
                return;
            }
            AdRequest adRequest = getAdRequest();
            AppOpenAd.AppOpenAdLoadCallback appOpenAdLoadCallback = new AppOpenAd.AppOpenAdLoadCallback() {
                @Override
                public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                    super.onAdFailedToLoad(loadAdError);
                    listIDResume.remove(0);
                    if (listIDResume.size() == 0) {
                        adCallback.onAdFailedToLoad(loadAdError);
                        adCallback.onNextAction();
                    } else {
                        loadOpenAppAdSplashFloor(context, listIDResume, isShowAdIfReady, adCallback);
                    }
                }

                @Override
                public void onAdLoaded(@NonNull AppOpenAd appOpenAd) {
                    super.onAdLoaded(appOpenAd);
                    AppOpenManager.this.splashAd = appOpenAd;
                    AppOpenManager.this.splashAd.setOnPaidEventListener((adValue) -> {
                        trackRevenue(AppOpenManager.this.splashAd.getResponseInfo().getLoadedAdapterResponseInfo(), adValue);
                        FirebaseUtil.logPaidAdImpression(myApplication.getApplicationContext(),
                                adValue,
                                appOpenAd.getAdUnitId(),
                                AdType.APP_OPEN);
                    });
                    if (isShowAdIfReady) {
                        AppOpenManager.this.showAppOpenSplash(context, adCallback);
                    } else {
                        adCallback.onAdSplashReady();
                    }
                }
            };
            AppOpenAd.load(context, listIDResume.get(0), adRequest, AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT, appOpenAdLoadCallback);
        }
    }

    public void onCheckShowSplashWhenFail(final AppCompatActivity activity, final AdCallback callback, int timeDelay) {
        (new Handler(activity.getMainLooper())).postDelayed(new Runnable() {
            public void run() {
                if (AppOpenManager.this.splashAd != null && !AppOpenManager.isShowingAd) {
                    Log.e("AppOpenManager", "show ad splash when show fail in background");
                    AppOpenManager.getInstance().showAppOpenSplash(activity, callback);
                }

            }
        }, (long) timeDelay);
    }

    public void onCheckShowSplashWhenFailNew(final AppCompatActivity activity, final AdCallback callback, int timeDelay) {
        (new Handler(activity.getMainLooper())).postDelayed(new Runnable() {
            public void run() {
                if (AppOpenManager.this.splashAd != null && !AppOpenManager.isShowingAd) {
                    Log.e("AppOpenManager", "show ad splash when show fail in background");
                    AppOpenManager.getInstance().showAppOpenSplashNew(activity, callback);
                }

            }
        }, (long) timeDelay);
    }

    private boolean isNetworkConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected();
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

