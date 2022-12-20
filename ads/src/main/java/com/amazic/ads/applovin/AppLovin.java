package com.amazic.ads.applovin;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.FrameLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ProcessLifecycleOwner;
import androidx.recyclerview.widget.RecyclerView;


import com.amazic.ads.R;
import com.amazic.ads.applovin.AppLovinCallback;
import com.amazic.ads.billing.AppPurchase;
import com.amazic.ads.callback.InterCallback;
import com.amazic.ads.dialog.LoadingAdsDialog;
import com.amazic.ads.util.AppOpenManager;
import com.amazic.ads.util.FirebaseUtil;
import com.applovin.mediation.MaxAd;
import com.applovin.mediation.MaxAdListener;
import com.applovin.mediation.MaxAdViewAdListener;
import com.applovin.mediation.MaxError;
import com.applovin.mediation.MaxReward;
import com.applovin.mediation.MaxRewardedAdListener;
import com.applovin.mediation.ads.MaxAdView;
import com.applovin.mediation.ads.MaxInterstitialAd;
import com.applovin.mediation.ads.MaxRewardedAd;
import com.applovin.mediation.nativeAds.MaxNativeAdListener;
import com.applovin.mediation.nativeAds.MaxNativeAdLoader;
import com.applovin.mediation.nativeAds.MaxNativeAdView;
import com.applovin.mediation.nativeAds.MaxNativeAdViewBinder;
import com.applovin.mediation.nativeAds.adPlacer.MaxAdPlacer;
import com.applovin.mediation.nativeAds.adPlacer.MaxAdPlacerSettings;
import com.applovin.mediation.nativeAds.adPlacer.MaxRecyclerAdapter;
import com.applovin.sdk.AppLovinSdk;
import com.facebook.shimmer.ShimmerFrameLayout;

import java.util.Calendar;

public class AppLovin {
    private static final String TAG = "AppLovin";
    private static AppLovin instance;
    private int currentClicked = 0;
    private String nativeId;
    private int numShowAds = 3;

    private int maxClickAds = 100;
    private Handler handlerTimeout;
    private Runnable rdTimeout;
    private LoadingAdsDialog dialog;
    private boolean isTimeout; // xử lý timeout show ads

    public boolean isShowLoadingSplash = false;  //kiểm tra trạng thái ad splash, ko cho load, show khi đang show loading ads splash
    boolean isTimeDelay = false; //xử lý delay time show ads, = true mới show ads
    private Context context;
//    private AppOpenAd appOpenAd = null;
//    private static final String SHARED_PREFERENCE_NAME = "ads_shared_preference";

//    private final Map<String, AppOpenAd> appOpenAdMap = new HashMap<>();

    private MaxInterstitialAd interstitialSplash;
    private MaxInterstitialAd interstitialAd;
    MaxNativeAdView nativeAdView;

    public static AppLovin getInstance() {
        if (instance == null) {
            instance = new AppLovin();
            instance.isShowLoadingSplash = false;
        }
        return instance;
    }

    public void init(Context context, AppLovinCallback adCallback) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            String processName = Application.getProcessName();
            String packageName = context.getPackageName();
            if (!packageName.equals(processName)) {
                WebView.setDataDirectorySuffix(processName);
            }
        }
        AppLovinSdk.getInstance(context).setMediationProvider("max");
        AppLovinSdk.initializeSdk(context, configuration -> {
            // AppLovin SDK is initialized, start loading ads
            Log.d(TAG, "init: applovin success");
            adCallback.initAppLovinSuccess();
        });
        this.context = context;
    }

    public void init(Context context, AppLovinCallback adCallback, Boolean enableDebug) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            String processName = Application.getProcessName();
            String packageName = context.getPackageName();
            if (!packageName.equals(processName)) {
                WebView.setDataDirectorySuffix(processName);
            }
        }
        if (enableDebug)
            AppLovinSdk.getInstance(context).showMediationDebugger();
        AppLovinSdk.getInstance(context).setMediationProvider("max");
        AppLovinSdk.initializeSdk(context, configuration -> {
            // AppLovin SDK is initialized, start loading ads
            Log.d(TAG, "init: applovin success");
            adCallback.initAppLovinSuccess();
        });
        this.context = context;
    }

    public void setNumShowAds(int numShowAds) {
        this.numShowAds = numShowAds;
    }

    public void setNumToShowAds(int numShowAds, int currentClicked) {
        this.numShowAds = numShowAds;
        this.currentClicked = currentClicked;
    }

    public MaxInterstitialAd getInterstitialSplash() {
        return interstitialSplash;
    }

    /**
     * Load quảng cáo Full tại màn SplashActivity
     * Sau khoảng thời gian timeout thì load ads và callback về cho View
     *
     * @param context
     * @param id
     * @param timeOut    : thời gian chờ ads, timeout <= 0 tương đương với việc bỏ timeout
     * @param timeDelay  : thời gian chờ show ad từ lúc load ads
     * @param adListener
     */
    public void loadSplashInterstitialAds(final Context context, String id, long timeOut, long timeDelay, AppLovinCallback adListener) {
        isTimeDelay = false;
        isTimeout = false;
        AppOpenMax.getInstance().disableAppResume();
        Log.i(TAG, "loadSplashInterstitialAds  start time loading:"
                + Calendar.getInstance().getTimeInMillis()
                + " ShowLoadingSplash:" + isShowLoadingSplash);

        if (AppPurchase.getInstance().isPurchased(context)) {
            if (adListener != null) {
                adListener.onAdClosed();
            }
            return;
        }

        interstitialSplash = getInterstitialAds(context, id);
        new Handler().postDelayed(() -> {
            //check delay show ad splash
            if (interstitialSplash != null && interstitialSplash.isReady()) {
                Log.i(TAG, "loadSplashInterstitialAds:show ad on delay ");
                onShowSplash((Activity) context, adListener);
                return;
            }
            Log.i(TAG, "loadSplashInterstitialAds: delay validate");
            isTimeDelay = true;
        }, timeDelay);

        if (timeOut > 0) {
            handlerTimeout = new Handler();
            rdTimeout = () -> {
                Log.e(TAG, "loadSplashInterstitialAds: on timeout");
                isTimeout = true;
                if (interstitialSplash != null && interstitialSplash.isReady()) {
                    Log.i(TAG, "loadSplashInterstitialAds:show ad on timeout ");
                    onShowSplash((Activity) context, adListener);
                    return;
                }
                if (adListener != null) {
                    adListener.onAdClosed();
                    isShowLoadingSplash = false;
                }
            };
            handlerTimeout.postDelayed(rdTimeout, timeOut);
        }

        isShowLoadingSplash = true;

        interstitialSplash.setListener(new MaxAdListener() {
            @Override
            public void onAdLoaded(MaxAd ad) {
                Log.e(TAG, "loadSplashInterstitialAds end time loading success: "
                        + Calendar.getInstance().getTimeInMillis()
                        + " time limit:" + isTimeout);
                if (isTimeout)
                    return;
                if (isTimeDelay) {
                    onShowSplash((Activity) context, adListener);
                    Log.i(TAG, "loadSplashInterstitialAds: show ad on loaded ");
                }
            }

            @Override
            public void onAdDisplayed(MaxAd ad) {

            }

            @Override
            public void onAdHidden(MaxAd ad) {

            }

            @Override
            public void onAdClicked(MaxAd ad) {
                FirebaseUtil.logClickAdsEvent(context, ad.getAdUnitId());
            }

            @Override
            public void onAdLoadFailed(String adUnitId, MaxError error) {
                AppOpenManager.getInstance().enableAppResume();
                AppOpenMax.getInstance().enableAppResume();
                Log.e(TAG, "onAdLoadFailed: " + error.getMessage());
                if (isTimeout)
                    return;
                if (adListener != null) {
                    if (handlerTimeout != null && rdTimeout != null) {
                        handlerTimeout.removeCallbacks(rdTimeout);
                    }
                    Log.e(TAG, "loadSplashInterstitialAds: load fail " + error.getMessage());
                    adListener.onAdFailedToLoad(error);
                }
            }

            @Override
            public void onAdDisplayFailed(MaxAd ad, MaxError error) {

            }
        });
    }

    /**
     * Load quảng cáo Full tại màn SplashActivity
     * Sau khoảng thời gian timeout thì load ads và callback về cho View
     *
     * @param context
     * @param id
     * @param timeOut    : thời gian chờ ads, timeout <= 0 tương đương với việc bỏ timeout
     * @param timeDelay  : thời gian chờ show ad từ lúc load ads
     * @param adListener
     */
    public void loadSplashInterstitialAds(final Context context, String id, long timeOut, long timeDelay, boolean showSplashIfReady, AppLovinCallback adListener) {
        isTimeDelay = false;
        isTimeout = false;
        Log.i(TAG, "loadSplashInterstitialAds  start time loading:"
                + Calendar.getInstance().getTimeInMillis()
                + " ShowLoadingSplash:" + isShowLoadingSplash);

        if (AppPurchase.getInstance().isPurchased(context)) {
            if (adListener != null) {
                adListener.onAdClosed();
            }
            return;
        }

        interstitialSplash = getInterstitialAds(context, id);
        new Handler().postDelayed(() -> {
            //check delay show ad splash
            if (interstitialSplash != null && interstitialSplash.isReady()) {
                Log.i(TAG, "loadSplashInterstitialAds:show ad on delay ");
                if (showSplashIfReady)
                    onShowSplash((Activity) context, adListener);
                else
                    adListener.onAdSplashReady();
                return;
            }
            Log.i(TAG, "loadSplashInterstitialAds: delay validate");
            isTimeDelay = true;
        }, timeDelay);

        if (timeOut > 0) {
            handlerTimeout = new Handler();
            rdTimeout = () -> {
                Log.e(TAG, "loadSplashInterstitialAds: on timeout");
                isTimeout = true;
                if (interstitialSplash != null && interstitialSplash.isReady()) {
                    Log.i(TAG, "loadSplashInterstitialAds:show ad on timeout ");
                    if (showSplashIfReady)
                        onShowSplash((Activity) context, adListener);
                    else
                        adListener.onAdSplashReady();

                    return;
                }
                if (adListener != null) {
                    adListener.onAdClosed();
                    isShowLoadingSplash = false;
                }
            };
            handlerTimeout.postDelayed(rdTimeout, timeOut);
        }

        isShowLoadingSplash = true;

        interstitialSplash.setListener(new MaxAdListener() {
            @Override
            public void onAdLoaded(MaxAd ad) {
                Log.e(TAG, "loadSplashInterstitialAds end time loading success: "
                        + Calendar.getInstance().getTimeInMillis()
                        + " time limit:" + isTimeout);
                if (isTimeout)
                    return;
                if (isTimeDelay) {
                    if (showSplashIfReady)
                        onShowSplash((Activity) context, adListener);
                    else
                        adListener.onAdSplashReady();
                    Log.i(TAG, "loadSplashInterstitialAds: show ad on loaded ");
                }
            }

            @Override
            public void onAdDisplayed(MaxAd ad) {

            }

            @Override
            public void onAdHidden(MaxAd ad) {

            }

            @Override
            public void onAdClicked(MaxAd ad) {
                FirebaseUtil.logClickAdsEvent(context, ad.getAdUnitId());
                if (adListener != null) {
                    adListener.onAdClicked();
                }
            }

            @Override
            public void onAdLoadFailed(String adUnitId, MaxError error) {
                Log.e(TAG, "onAdLoadFailed: " + error.getMessage());
                if (isTimeout)
                    return;
                if (adListener != null) {
                    if (handlerTimeout != null && rdTimeout != null) {
                        handlerTimeout.removeCallbacks(rdTimeout);
                    }
                    Log.e(TAG, "loadSplashInterstitialAds: load fail " + error.getMessage());
                    adListener.onAdFailedToLoad(error);
                }
            }

            @Override
            public void onAdDisplayFailed(MaxAd ad, MaxError error) {

            }
        });
    }

    public void onShowSplash(Activity activity, AppLovinCallback adListener) {
        isShowLoadingSplash = true;
        Log.d(TAG, "onShowSplash: ");
        if (handlerTimeout != null && rdTimeout != null) {
            handlerTimeout.removeCallbacks(rdTimeout);
        }

        if (adListener != null) {
            adListener.onAdLoaded();
        }
        if (interstitialSplash == null) {
            adListener.onAdClosed();
            return;
        }
        interstitialSplash.setListener(new MaxAdListener() {
            @Override
            public void onAdLoaded(MaxAd ad) {

            }

            @Override
            public void onAdDisplayed(MaxAd ad) {
                Log.d(TAG, "onAdDisplayed: ");
                if (AppOpenManager.getInstance().isInitialized()) {
                    AppOpenManager.getInstance().disableAppResume();
                }
                AppOpenMax.getInstance().disableAppResume();
            }

            @Override
            public void onAdHidden(MaxAd ad) {
                Log.d(TAG, "onAdHidden: " + ((AppCompatActivity) activity).getLifecycle().getCurrentState());
                isShowLoadingSplash = false;
                if (adListener != null && ((AppCompatActivity) activity).getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.RESUMED)) {
                    adListener.onAdClosed();
                    interstitialSplash = null;
                    if (dialog != null) {
                        dialog.dismiss();
                    }
                }
                if (AppOpenManager.getInstance().isInitialized()) {
                    AppOpenManager.getInstance().enableAppResume();
                }
                AppOpenMax.getInstance().enableAppResume();
            }

            @Override
            public void onAdClicked(MaxAd ad) {
                FirebaseUtil.logClickAdsEvent(context, interstitialSplash.getAdUnitId());
                if (adListener != null) {
                    adListener.onAdClicked();
                }
            }

            @Override
            public void onAdLoadFailed(String adUnitId, MaxError error) {

            }

            @Override
            public void onAdDisplayFailed(MaxAd ad, MaxError error) {
                Log.d(TAG, "onAdDisplayFailed: " + error.getMessage());
                interstitialSplash = null;
                isShowLoadingSplash = false;
                if (adListener != null) {
                    adListener.onAdFailedToShow(error);
                    if (dialog != null) {
                        dialog.dismiss();
                    }
                }
            }
        });

        if (ProcessLifecycleOwner.get().getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.RESUMED)) {
            try {
                if (dialog != null && dialog.isShowing())
                    dialog.dismiss();
                dialog = new LoadingAdsDialog(activity);
                if (activity != null && !activity.isDestroyed()) {
                    dialog.setCancelable(false);
                    dialog.show();
                }
            } catch (Exception e) {
                dialog = null;
                e.printStackTrace();
                adListener.onAdClosed();
                return;
            }
            new Handler().postDelayed(() -> {
                if (activity != null && !activity.isDestroyed())
                    interstitialSplash.showAd();
            }, 800);
        } else {
            Log.e(TAG, "onShowSplash fail ");
            isShowLoadingSplash = false;
        }
    }

    public void onCheckShowSplashWhenFail(Activity activity, AppLovinCallback callback, int timeDelay) {
        if (AppLovin.getInstance().getInterstitialSplash() != null && !AppLovin.getInstance().isShowLoadingSplash) {
            new Handler(activity.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (AppLovin.getInstance().getInterstitialSplash().isReady()) {
                        Log.i(TAG, "show ad splash when show fail in background");
                        AppLovin.getInstance().onShowSplash(activity, callback);
                    } else {
                        callback.onAdClosed();
                    }
                }
            }, timeDelay);
        }
    }


    /**
     * Trả về 1 InterstitialAd và request Ads
     *
     * @param context
     * @param id
     * @return
     */
    public MaxInterstitialAd getInterstitialAds(Context context, String id) {
        if (AppPurchase.getInstance().isPurchased(context) || AppLovinHelper.getNumClickAdsPerDay(context, id) >= maxClickAds) {
            Log.d(TAG, "getInterstitialAds: ignore");
            return null;
        }
        final MaxInterstitialAd interstitialAd = new MaxInterstitialAd(id, (Activity) context);
        interstitialAd.setListener(new MaxAdListener() {
            @Override
            public void onAdLoaded(MaxAd ad) {
                Log.d(TAG, "onAdLoaded: getInterstitialAds");
            }

            @Override
            public void onAdDisplayed(MaxAd ad) {

            }

            @Override
            public void onAdHidden(MaxAd ad) {

            }

            @Override
            public void onAdClicked(MaxAd ad) {
                FirebaseUtil.logClickAdsEvent(context, ad.getAdUnitId());
            }

            @Override
            public void onAdLoadFailed(String adUnitId, MaxError error) {
                Log.e(TAG, "onAdLoadFailed: getInterstitialAds " + error.getMessage());
            }

            @Override
            public void onAdDisplayFailed(MaxAd ad, MaxError error) {

            }
        });
        requestInterstitialAds(interstitialAd);
        return interstitialAd;
    }

    private void requestInterstitialAds(MaxInterstitialAd maxInterstitialAd) {
        if (maxInterstitialAd != null && !maxInterstitialAd.isReady()) {
            maxInterstitialAd.loadAd();
        }
    }

    /**
     * Bắt buộc hiển thị  ads full và callback result
     *
     * @param context
     * @param interstitialAd
     * @param callback
     */
    public void forceShowInterstitial(Context context, MaxInterstitialAd interstitialAd, final InterCallback callback, boolean shouldReload) {
        currentClicked = numShowAds;
        showInterstitialAdByTimes(context, interstitialAd, callback, shouldReload);
    }

    /**
     * Hiển thị ads theo số lần được xác định trước và callback result
     * vd: click vào 3 lần thì show ads full.
     * AdmodHelper.setupAdmodData(context) -> kiểm tra xem app đc hoạt động đc 1 ngày chưa nếu YES thì reset lại số lần click vào ads
     *
     * @param context
     * @param interstitialAd
     * @param callback
     * @param shouldReloadAds
     */
    public void showInterstitialAdByTimes(final Context context, MaxInterstitialAd interstitialAd, final InterCallback callback, final boolean shouldReloadAds) {
        AppLovinHelper.setupAppLovinData(context);
        if (AppOpenManager.getInstance().isInitialized()) {
            AppOpenManager.getInstance().disableAppResume();
        }
        AppOpenMax.getInstance().disableAppResume();

        if (AppPurchase.getInstance().isPurchased(context)) {
            callback.onAdClosed();
            return;
        }
        if (interstitialAd == null || !interstitialAd.isReady()) {
            if (callback != null) {
                callback.onAdClosed();
            }
            return;
        }

        interstitialAd.setListener(new MaxAdListener() {
            @Override
            public void onAdLoaded(MaxAd ad) {
            }

            @Override
            public void onAdDisplayed(MaxAd ad) {

            }


            @Override
            public void onAdHidden(MaxAd ad) {
                //  if (callback != null && ((AppCompatActivity) context).getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.RESUMED)) {
                callback.onAdClosed();
                if (shouldReloadAds) {
                    requestInterstitialAds(interstitialAd);
                }
                if (dialog != null) {
                    dialog.dismiss();
                }
                //    }
                Log.d(TAG, "onAdHidden: " + ((AppCompatActivity) context).getLifecycle().getCurrentState());
                if (AppOpenManager.getInstance().isInitialized()) {
                    AppOpenManager.getInstance().enableAppResume();
                }
                AppOpenMax.getInstance().enableAppResume();
            }

            @Override
            public void onAdClicked(MaxAd ad) {
                FirebaseUtil.logClickAdsEvent(context, ad.getAdUnitId());
                if (callback != null) {
                    callback.onAdClicked();
                }
            }

            @Override
            public void onAdLoadFailed(String adUnitId, MaxError error) {
                if (AppOpenManager.getInstance().isInitialized()) {
                    AppOpenManager.getInstance().enableAppResume();
                }
                AppOpenMax.getInstance().enableAppResume();
            }

            @Override
            public void onAdDisplayFailed(MaxAd ad, MaxError error) {
                Log.e(TAG, "onAdDisplayFailed: " + error.getMessage());
                if (callback != null) {
                    callback.onAdClosed();
                    if (dialog != null) {
                        dialog.dismiss();
                    }
                }
                if (AppOpenManager.getInstance().isInitialized()) {
                    AppOpenManager.getInstance().enableAppResume();
                }
                AppOpenMax.getInstance().enableAppResume();
            }
        });
        if (AppLovinHelper.getNumClickAdsPerDay(context, interstitialAd.getAdUnitId()) < maxClickAds) {
            showInterstitialAd(context, interstitialAd, callback);
            return;
        }
        if (callback != null) {
            callback.onAdClosed();
        }
    }

    /**
     * Kiểm tra và hiện thị ads
     *
     * @param context
     * @param interstitialAd
     * @param callback
     */
    private void showInterstitialAd(Context context, MaxInterstitialAd interstitialAd, InterCallback callback) {
        currentClicked++;
        if (currentClicked >= numShowAds && interstitialAd != null) {
            if (ProcessLifecycleOwner.get().getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.RESUMED)) {
                try {
                    if (dialog != null && dialog.isShowing())
                        dialog.dismiss();
                    dialog = new LoadingAdsDialog(context);
                    try {
//                        callback.onInterstitialShow();
                        dialog.setCancelable(false);
                        dialog.show();
                    } catch (Exception e) {
                        callback.onAdClosed();
                        return;
                    }
                } catch (Exception e) {
                    dialog = null;
                    e.printStackTrace();
                }
                new Handler().postDelayed(interstitialAd::showAd, 800);
            }
            currentClicked = 0;
        } else if (callback != null) {
            if (dialog != null) {
                dialog.dismiss();
            }
            callback.onAdClosed();
        }
    }

    /**
     * Load quảng cáo Banner Trong Activity
     *
     * @param mActivity
     * @param id
     */
    public void loadBanner(final Activity mActivity, String id) {
        final FrameLayout adContainer = mActivity.findViewById(R.id.banner_container);
        final ShimmerFrameLayout containerShimmer = mActivity.findViewById(R.id.shimmer_container_banner);
        loadBanner(mActivity, id, adContainer, containerShimmer);
    }

    public void loadBanner(final Activity mActivity, String id, final InterCallback adCallback) {
        final FrameLayout adContainer = mActivity.findViewById(R.id.banner_container);
        final ShimmerFrameLayout containerShimmer = mActivity.findViewById(R.id.shimmer_container_banner);
        loadBanner(mActivity, id, adContainer, containerShimmer, adCallback);
    }

    /**
     * Load Quảng Cáo Banner Trong Fragment
     *
     * @param mActivity
     * @param id
     * @param rootView
     */
    public void loadBannerFragment(final Activity mActivity, String id, final View rootView) {
        final FrameLayout adContainer = rootView.findViewById(R.id.banner_container);
        final ShimmerFrameLayout containerShimmer = rootView.findViewById(R.id.shimmer_container_banner);
        loadBanner(mActivity, id, adContainer, containerShimmer);
    }

    public void loadBannerFragment(final Activity mActivity, String id, final View rootView, final InterCallback adCallback) {
        final FrameLayout adContainer = rootView.findViewById(R.id.banner_container);
        final ShimmerFrameLayout containerShimmer = rootView.findViewById(R.id.shimmer_container_banner);
        loadBanner(mActivity, id, adContainer, containerShimmer, adCallback);
    }

    private void loadBanner(final Activity mActivity, String id, final FrameLayout adContainer, final ShimmerFrameLayout containerShimmer) {
        if (AppPurchase.getInstance().isPurchased(mActivity)) {
            containerShimmer.setVisibility(View.GONE);
            return;
        }
        containerShimmer.setVisibility(View.VISIBLE);
        containerShimmer.startShimmer();
        MaxAdView adView = new MaxAdView(id, mActivity);
        int width = ViewGroup.LayoutParams.MATCH_PARENT;
        // Banner height on phones and tablets is 50 and 90, respectively
        int heightPx = mActivity.getResources().getDimensionPixelSize(R.dimen.banner_height);
        adView.setLayoutParams(new FrameLayout.LayoutParams(width, heightPx));
        adContainer.addView(adView);
        adView.setListener(new MaxAdViewAdListener() {
            @Override
            public void onAdExpanded(MaxAd ad) {

            }

            @Override
            public void onAdCollapsed(MaxAd ad) {

            }

            @Override
            public void onAdLoaded(MaxAd ad) {
                Log.d(TAG, "onAdLoaded: banner");
                containerShimmer.stopShimmer();
                containerShimmer.setVisibility(View.GONE);
                adContainer.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAdDisplayed(MaxAd ad) {

            }

            @Override
            public void onAdHidden(MaxAd ad) {

            }

            @Override
            public void onAdClicked(MaxAd ad) {
                FirebaseUtil.logClickAdsEvent(context, ad.getAdUnitId());
            }

            @Override
            public void onAdLoadFailed(String adUnitId, MaxError error) {
                Log.e(TAG, "onAdLoadFailed: banner " + error.getMessage() + "   code:" + error.getCode());
                containerShimmer.stopShimmer();
                adContainer.setVisibility(View.GONE);
                containerShimmer.setVisibility(View.GONE);
            }

            @Override
            public void onAdDisplayFailed(MaxAd ad, MaxError error) {

            }
        });
        adView.loadAd();
    }

    private void loadBanner(final Activity mActivity, String id, final FrameLayout adContainer, final ShimmerFrameLayout containerShimmer, final InterCallback adCallback) {
        if (AppPurchase.getInstance().isPurchased(mActivity)) {
            containerShimmer.setVisibility(View.GONE);
            return;
        }
        containerShimmer.setVisibility(View.VISIBLE);
        containerShimmer.startShimmer();
        MaxAdView adView = new MaxAdView(id, mActivity);
        int width = ViewGroup.LayoutParams.MATCH_PARENT;
        // Banner height on phones and tablets is 50 and 90, respectively
        int heightPx = mActivity.getResources().getDimensionPixelSize(R.dimen.banner_height);
        adView.setLayoutParams(new FrameLayout.LayoutParams(width, heightPx));
        adContainer.addView(adView);
        adView.setListener(new MaxAdViewAdListener() {
            @Override
            public void onAdExpanded(MaxAd ad) {

            }

            @Override
            public void onAdCollapsed(MaxAd ad) {

            }

            @Override
            public void onAdLoaded(MaxAd ad) {
                Log.d(TAG, "onAdLoaded: banner");
                containerShimmer.stopShimmer();
                containerShimmer.setVisibility(View.GONE);
                adContainer.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAdDisplayed(MaxAd ad) {

            }

            @Override
            public void onAdHidden(MaxAd ad) {

            }

            @Override
            public void onAdClicked(MaxAd ad) {
                FirebaseUtil.logClickAdsEvent(context, ad.getAdUnitId());
                if (adCallback != null) {
                    adCallback.onAdClicked();
                }
            }

            @Override
            public void onAdLoadFailed(String adUnitId, MaxError error) {
                Log.e(TAG, "onAdLoadFailed: banner " + error.getMessage() + "   code:" + error.getCode());
                containerShimmer.stopShimmer();
                adContainer.setVisibility(View.GONE);
                containerShimmer.setVisibility(View.GONE);
            }

            @Override
            public void onAdDisplayFailed(MaxAd ad, MaxError error) {

            }
        });
        adView.loadAd();
    }

    public void loadNative(final Activity mActivity, String adUnitId) {
        final FrameLayout frameLayout = mActivity.findViewById(R.id.fl_adplaceholder);
        final ShimmerFrameLayout containerShimmer = mActivity.findViewById(R.id.shimmer_container_native);
        loadNativeAd(mActivity, containerShimmer, frameLayout, adUnitId, R.layout.custom_native_max_free_size);
    }

    public void loadNativeSmall(final Activity mActivity, String adUnitId) {
        final FrameLayout frameLayout = mActivity.findViewById(R.id.fl_adplaceholder);
        final ShimmerFrameLayout containerShimmer = mActivity.findViewById(R.id.shimmer_container_native);
        loadNativeAd(mActivity, containerShimmer, frameLayout, adUnitId, R.layout.custom_native_max_medium);
    }

    public void loadNativeFragment(final Activity mActivity, String adUnitId, View parent) {
        final FrameLayout frameLayout = parent.findViewById(R.id.fl_adplaceholder);
        final ShimmerFrameLayout containerShimmer = parent.findViewById(R.id.shimmer_container_native);
        loadNativeAd(mActivity, containerShimmer, frameLayout, adUnitId, R.layout.custom_native_max_free_size);
    }

    public void loadNativeSmallFragment(final Activity mActivity, String adUnitId, View parent) {
        final FrameLayout frameLayout = parent.findViewById(R.id.fl_adplaceholder);
        final ShimmerFrameLayout containerShimmer = parent.findViewById(R.id.shimmer_container_native);
        loadNativeAd(mActivity, containerShimmer, frameLayout, adUnitId, R.layout.custom_native_max_medium);
    }


    public void loadNativeAd(Activity activity, ShimmerFrameLayout containerShimmer, FrameLayout nativeAdLayout, String id, int layoutCustomNative) {

        if (AppPurchase.getInstance().isPurchased(context)) {
            containerShimmer.setVisibility(View.GONE);
            return;
        }
        containerShimmer.setVisibility(View.VISIBLE);
        containerShimmer.startShimmer();

        nativeAdLayout.removeAllViews();
        nativeAdLayout.setVisibility(View.GONE);
        MaxNativeAdViewBinder binder = new MaxNativeAdViewBinder.Builder(layoutCustomNative)
                .setTitleTextViewId(R.id.ad_headline)
                .setBodyTextViewId(R.id.ad_body)
                .setAdvertiserTextViewId(R.id.ad_advertiser)
                .setIconImageViewId(R.id.ad_app_icon)
                .setMediaContentViewGroupId(R.id.ad_media)
                .setOptionsContentViewGroupId(R.id.ad_options_view)
                .setCallToActionButtonId(R.id.ad_call_to_action)
                .build();

        nativeAdView = new MaxNativeAdView(binder, activity);

        MaxNativeAdLoader nativeAdLoader = new MaxNativeAdLoader(id, activity);
        nativeAdLoader.setNativeAdListener(new MaxNativeAdListener() {
            @Override
            public void onNativeAdLoaded(final MaxNativeAdView nativeAdView, final MaxAd ad) {
                Log.d(TAG, "onNativeAdLoaded ");
                containerShimmer.stopShimmer();
                containerShimmer.setVisibility(View.GONE);
                // Add ad view to view.
                nativeAdLayout.setVisibility(View.VISIBLE);
                nativeAdLayout.addView(nativeAdView);
            }

            @Override
            public void onNativeAdLoadFailed(final String adUnitId, final MaxError error) {
                Log.e(TAG, "onAdFailedToLoad: " + error.getMessage());
                containerShimmer.stopShimmer();
                containerShimmer.setVisibility(View.GONE);
                nativeAdLayout.setVisibility(View.GONE);
            }

            @Override
            public void onNativeAdClicked(final MaxAd ad) {
                Log.e(TAG, "`onNativeAdClicked`: ");
                containerShimmer.setVisibility(View.VISIBLE);
                containerShimmer.startShimmer();
                nativeAdLayout.removeAllViews();
                nativeAdLayout.setVisibility(View.GONE);

                nativeAdView = new MaxNativeAdView(binder, activity);
                nativeAdLoader.loadAd(nativeAdView);
            }
        });
        nativeAdLoader.loadAd(nativeAdView);
    }

    public void loadNativeAd(Activity activity, String id, int layoutCustomNative, AppLovinCallback callback) {

        if (AppPurchase.getInstance().isPurchased(context)) {
            callback.onAdClosed();
            return;
        }

        MaxNativeAdViewBinder binder = new MaxNativeAdViewBinder.Builder(layoutCustomNative)
                .setTitleTextViewId(R.id.ad_headline)
                .setBodyTextViewId(R.id.ad_body)
                .setAdvertiserTextViewId(R.id.ad_advertiser)
                .setIconImageViewId(R.id.ad_app_icon)
                .setMediaContentViewGroupId(R.id.ad_media)
                .setOptionsContentViewGroupId(R.id.options_view)
                .setCallToActionButtonId(R.id.ad_call_to_action)
                .build();

        nativeAdView = new MaxNativeAdView(binder, activity);
        Log.e(TAG, "load native");
        MaxNativeAdLoader nativeAdLoader = new MaxNativeAdLoader(id, activity);
        nativeAdLoader.setNativeAdListener(new MaxNativeAdListener() {
            @Override
            public void onNativeAdLoaded(final MaxNativeAdView nativeAdView, final MaxAd ad) {
                Log.d(TAG, "onNativeAdLoaded ");
                callback.onUnifiedNativeAdLoaded(nativeAdView);
            }

            @Override
            public void onNativeAdLoadFailed(final String adUnitId, final MaxError error) {
                Log.e(TAG, "onAdFailedToLoad: " + error.getMessage());
                callback.onAdFailedToLoad(error);
            }

            @Override
            public void onNativeAdClicked(final MaxAd ad) {
                Log.e(TAG, "onNativeAdClicked: ");
                FirebaseUtil.logClickAdsEvent(context, ad.getAdUnitId());
                callback.onAdClicked();

            }
        });
        nativeAdLoader.loadAd(nativeAdView);
    }

    public MaxRecyclerAdapter getNativeRepeatAdapter(Activity activity, String id, int layoutCustomNative, RecyclerView.Adapter originalAdapter,
                                                     MaxAdPlacer.Listener listener, int repeatingInterval) {
        //seting max
        MaxAdPlacerSettings settings = new MaxAdPlacerSettings(id);
        settings.setRepeatingInterval(repeatingInterval);

        MaxRecyclerAdapter adAdapter = new MaxRecyclerAdapter(settings, originalAdapter, activity);
        adAdapter.getAdPlacer().setAdSize(-1, -1);

        MaxNativeAdViewBinder binder = new MaxNativeAdViewBinder.Builder(layoutCustomNative)
                .setTitleTextViewId(R.id.ad_headline)
                .setBodyTextViewId(R.id.ad_body)
                .setAdvertiserTextViewId(R.id.ad_advertiser)
                .setIconImageViewId(R.id.ad_app_icon)
                .setMediaContentViewGroupId(R.id.ad_media)
                .setOptionsContentViewGroupId(R.id.ad_options_view)
                .setCallToActionButtonId(R.id.ad_call_to_action)
                .build();

        adAdapter.getAdPlacer().setNativeAdViewBinder(binder);
        if (listener != null)
            adAdapter.setListener(listener);
        return adAdapter;
    }

    public MaxRecyclerAdapter getNativeFixedPositionAdapter(Activity activity, String id, int layoutCustomNative, RecyclerView.Adapter originalAdapter,
                                                            MaxAdPlacer.Listener listener, int position) {
        //seting max
        MaxAdPlacerSettings settings = new MaxAdPlacerSettings(id);
        settings.addFixedPosition(position);

        MaxRecyclerAdapter adAdapter = new MaxRecyclerAdapter(settings, originalAdapter, activity);
        adAdapter.getAdPlacer().setAdSize(-1, -1);

        MaxNativeAdViewBinder binder = new MaxNativeAdViewBinder.Builder(layoutCustomNative)
                .setTitleTextViewId(R.id.ad_headline)
                .setBodyTextViewId(R.id.ad_body)
                .setAdvertiserTextViewId(R.id.ad_advertiser)
                .setIconImageViewId(R.id.ad_app_icon)
                .setMediaContentViewGroupId(R.id.ad_media)
                .setOptionsContentViewGroupId(R.id.ad_options_view)
                .setCallToActionButtonId(R.id.ad_call_to_action)
                .build();

        adAdapter.getAdPlacer().setNativeAdViewBinder(binder);
        if (listener != null)
            adAdapter.setListener(listener);
        return adAdapter;
    }


    public MaxRewardedAd getRewardAd(Activity activity, String id, AppLovinCallback callback) {
        MaxRewardedAd rewardedAd = MaxRewardedAd.getInstance(id, activity);
        rewardedAd.setListener(new MaxRewardedAdListener() {
            @Override
            public void onRewardedVideoStarted(MaxAd ad) {
                Log.d(TAG, "onRewardedVideoStarted: ");
            }

            @Override
            public void onRewardedVideoCompleted(MaxAd ad) {
                Log.d(TAG, "onRewardedVideoCompleted: ");
            }

            @Override
            public void onUserRewarded(MaxAd ad, MaxReward reward) {
                callback.onUserRewarded(reward);
                Log.d(TAG, "onUserRewarded: ");
            }

            @Override
            public void onAdLoaded(MaxAd ad) {
                Log.d(TAG, "onAdLoaded: ");
                callback.onAdLoaded();
            }

            @Override
            public void onAdDisplayed(MaxAd ad) {
                Log.d(TAG, "onAdDisplayed: ");
            }

            @Override
            public void onAdHidden(MaxAd ad) {
                callback.onAdClosed();
                Log.d(TAG, "onAdHidden: ");
            }

            @Override
            public void onAdClicked(MaxAd ad) {
                FirebaseUtil.logClickAdsEvent(context, ad.getAdUnitId());
                callback.onAdClicked();
            }

            @Override
            public void onAdLoadFailed(String adUnitId, MaxError error) {
                Log.d(TAG, "onAdLoadFailed: " + error.getMessage());
                callback.onAdFailedToLoad(error);
            }

            @Override
            public void onAdDisplayFailed(MaxAd ad, MaxError error) {
                Log.d(TAG, "onAdDisplayFailed: " + error.getMessage());
                callback.onAdFailedToShow(error);
            }
        });
        rewardedAd.loadAd();
        return rewardedAd;
    }

    public MaxRewardedAd getRewardAd(Activity activity, String id) {
        MaxRewardedAd rewardedAd = MaxRewardedAd.getInstance(id, activity);
        rewardedAd.loadAd();
        return rewardedAd;
    }

    public void showRewardAd(Activity activity, MaxRewardedAd maxRewardedAd, AppLovinCallback callback) {
        if (maxRewardedAd.isReady()) {
            if (AppOpenManager.getInstance().isInitialized()) {
                AppOpenManager.getInstance().disableAppResume();
            }
            maxRewardedAd.setListener(new MaxRewardedAdListener() {
                @Override
                public void onRewardedVideoStarted(MaxAd ad) {
                    Log.d(TAG, "onRewardedVideoStarted: ");
                }

                @Override
                public void onRewardedVideoCompleted(MaxAd ad) {
                    Log.d(TAG, "onRewardedVideoCompleted: ");
                }

                @Override
                public void onUserRewarded(MaxAd ad, MaxReward reward) {
                    callback.onUserRewarded(reward);
                    Log.d(TAG, "onUserRewarded: ");
                    if (AppOpenManager.getInstance().isInitialized()) {
                        AppOpenManager.getInstance().enableAppResume();
                    }
                    AppOpenMax.getInstance().enableAppResume();
                }

                @Override
                public void onAdLoaded(MaxAd ad) {
                    Log.d(TAG, "onAdLoaded: ");
                    callback.onAdLoaded();
                }

                @Override
                public void onAdDisplayed(MaxAd ad) {
                    Log.d(TAG, "onAdDisplayed: ");
                }

                @Override
                public void onAdHidden(MaxAd ad) {
                    callback.onAdClosed();
                    Log.d(TAG, "onAdHidden: ");
                    if (AppOpenManager.getInstance().isInitialized()) {
                        AppOpenManager.getInstance().enableAppResume();
                    }
                    AppOpenMax.getInstance().enableAppResume();
                }

                @Override
                public void onAdClicked(MaxAd ad) {
                    FirebaseUtil.logClickAdsEvent(context, ad.getAdUnitId());
                    callback.onAdClicked();
                }

                @Override
                public void onAdLoadFailed(String adUnitId, MaxError error) {
                    Log.d(TAG, "onAdLoadFailed: " + error.getMessage());
                    callback.onAdFailedToLoad(error);
                    callback.onAdClosed();
                }

                @Override
                public void onAdDisplayFailed(MaxAd ad, MaxError error) {
                    Log.d(TAG, "onAdDisplayFailed: " + error.getMessage());
                    callback.onAdFailedToShow(error);
                    callback.onAdClosed();
                }
            });
            maxRewardedAd.showAd();
        } else {
            Log.e(TAG, "showRewardAd error -  reward ad not ready");
            callback.onAdFailedToShow(null);
        }
    }

    public void showRewardAd(Activity activity, MaxRewardedAd maxRewardedAd) {
        if (maxRewardedAd.isReady()) {
            maxRewardedAd.showAd();
        } else {
            Log.e(TAG, "showRewardAd error -  reward ad not ready");
        }
    }
}
