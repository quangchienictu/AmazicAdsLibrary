package com.amazic.ads.util;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.app.Dialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ProcessLifecycleOwner;

import com.amazic.ads.BuildConfig;
import com.amazic.ads.R;
import com.amazic.ads.callback.InterCallback;
import com.amazic.ads.callback.NativeCallback;
import com.amazic.ads.callback.RewardCallback;
import com.amazic.ads.dialog.LoadingAdsDialog;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.OnUserEarnedRewardListener;
import com.google.android.gms.ads.RequestConfiguration;
import com.google.android.gms.ads.VideoOptions;
import com.google.android.gms.ads.formats.NativeAdOptions;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdView;
import com.google.android.gms.ads.rewarded.RewardItem;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;

public class Admod {
    private static Admod INSTANCE;
    private static final String TAG = "Admod";
    private LoadingAdsDialog dialog;
    private int currentClicked = 0;
    private int numShowAds = 3;
    private int maxClickAds = 100;
    private Handler handlerTimeout;
    private Runnable rdTimeout;
    private boolean isTimeLimited;
    private boolean isShowLoadingSplash = false; //kiểm tra trạng thái ad splash, ko cho load, show khi đang show loading ads splash
    boolean checkTimeDelay = false;
    private boolean openActivityAfterShowInterAds = true;
    private Context context;
    boolean isTimeDelay = false; //xử lý delay time show ads, = true mới show ads
    private boolean isTimeout; // xử lý timeout show ads

    private RewardedAd rewardedAd;
    private String rewardedId;
    InterstitialAd mInterstitialSplash;
    InterstitialAd interstitialAd;

    public long timeLimitAds = 0; // Set > 1000 nếu cần limit ads click
    private boolean isShowInter = true;
    private boolean isShowBanner = true;
    private boolean isShowNative = true;

    public static boolean isShowAllAds = true;



    private boolean isFan = false;

    public static Admod getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new Admod();
        }
        return INSTANCE;
    }

    public void initAdmod(Context context, List<String> testDeviceList) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            String processName = Application.getProcessName();
            String packageName = context.getPackageName();
            if (!packageName.equals(processName)) {
                WebView.setDataDirectorySuffix(processName);
            }
        }
        MobileAds.initialize(context, initializationStatus -> {
        });
        MobileAds.setRequestConfiguration(new RequestConfiguration.Builder().setTestDeviceIds(testDeviceList).build());

        this.context = context;
    }

    public void initAdmod(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            String processName = Application.getProcessName();
            String packageName = context.getPackageName();
            if (!packageName.equals(processName)) {
                WebView.setDataDirectorySuffix(processName);
            }
        }

        MobileAds.initialize(context, initializationStatus -> {
        });
        if (BuildConfig.DEBUG) {
            MobileAds.setRequestConfiguration(new RequestConfiguration.Builder().setTestDeviceIds(Arrays.asList(getDeviceId((Activity) context))).build());
        }

        this.context = context;
    }

    public void setFan(boolean fan) {
        isFan = fan;
    }
    /**
     * Set tắt toàn bộ ads trong project
     **/
    public void setShowAllAds(boolean isShowAllAds){
        this.isShowAllAds = isShowAllAds;
    }
    /* =======================   Banner ================================= */


    public void loadBanner(final Activity mActivity, String id) {
        final FrameLayout adContainer = mActivity.findViewById(R.id.banner_container);
        final ShimmerFrameLayout containerShimmer = mActivity.findViewById(R.id.shimmer_container_banner);
        loadBanner(mActivity, id, adContainer, containerShimmer, false);
    }

    private void loadBanner(final Activity mActivity, String id, final FrameLayout adContainer, final ShimmerFrameLayout containerShimmer, Boolean useInlineAdaptive) {
        if(isShowBanner){
            if (!isShowAllAds) {
                containerShimmer.setVisibility(View.GONE);
                return;
            }
            containerShimmer.setVisibility(View.VISIBLE);
            containerShimmer.startShimmer();
            try {
                AdView adView = new AdView(mActivity);
                adView.setAdUnitId(id);
                adContainer.addView(adView);
                AdSize adSize = getAdSize(mActivity, useInlineAdaptive);
                adView.setAdSize(adSize);
                adView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
                adView.loadAd(getAdRequest());
                adView.setAdListener(new AdListener() {

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        super.onAdFailedToLoad(loadAdError);
                        containerShimmer.stopShimmer();
                        adContainer.setVisibility(View.GONE);
                        containerShimmer.setVisibility(View.GONE);
                    }

                    @Override
                    public void onAdLoaded() {
                        Log.d(TAG, "Banner adapter class name: " + adView.getResponseInfo().getMediationAdapterClassName());
                        containerShimmer.stopShimmer();
                        containerShimmer.setVisibility(View.GONE);
                        adContainer.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onAdClicked() {
                        super.onAdClicked();
                        FirebaseAnalyticsUtil.logClickAdsEventByActivity(context,FirebaseAnalyticsUtil.BANNER);
                        FirebaseAnalyticsUtil.logClickAdsEventAdmob(context);
                        if(timeLimitAds>1000){
                            setTimeLimitBanner();
                            containerShimmer.stopShimmer();
                            adContainer.setVisibility(View.GONE);
                            containerShimmer.setVisibility(View.GONE);
                        }
                    }
                });


            } catch (Exception e) {
                e.printStackTrace();
            }
        }else{
            containerShimmer.stopShimmer();
            adContainer.setVisibility(View.GONE);
            containerShimmer.setVisibility(View.GONE);
        }

    }
    private AdSize getAdSize(Activity mActivity, Boolean useInlineAdaptive) {
        // Step 2 - Determine the screen width (less decorations) to use for the ad width.
        Display display = mActivity.getWindowManager().getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics();
        display.getMetrics(outMetrics);

        float widthPixels = outMetrics.widthPixels;
        float density = outMetrics.density;

        int adWidth = (int) (widthPixels / density);

        // Step 3 - Get adaptive ad size and return for setting on the ad view.
        if (useInlineAdaptive) {
            return AdSize.getCurrentOrientationInlineAdaptiveBannerAdSize(mActivity, adWidth);
        }
        return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(mActivity, adWidth);

    }
    /**
     * Load ads Banner in Fragment
     */
    public void loadBannerFragment(final Activity mActivity, String id, final View rootView) {
        final FrameLayout adContainer = rootView.findViewById(R.id.banner_container);
        final ShimmerFrameLayout containerShimmer = rootView.findViewById(R.id.shimmer_container_banner);
        loadBanner(mActivity, id, adContainer, containerShimmer, false);
    }

    /*===========================  end Banner ========================================= */




    public boolean interstialSplashLoead() {
        return mInterstitialSplash != null;
    }

    public InterstitialAd getmInterstitialSplash() {
        return mInterstitialSplash;
    }


    /* ==========================  Inter Splash============================================== */





    /**
     * Load ads in Splash
     */
    public void loadSplashInterAds(final Context context, String id, long timeOut, long timeDelay, final InterCallback adListener) {
        isTimeDelay = false;
        isTimeout = false;
        if (!isShowAllAds) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (adListener != null) {
                        adListener.onAdClosed();
                    }
                    return;
                }
            },3000);
        }else{
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    //check delay show ad splash
                    if (mInterstitialSplash != null) {
                        Log.d(TAG, "loadSplashInterAds:show ad on delay ");
                        onShowSplash((Activity) context, adListener);
                        return;
                    }
                    Log.d(TAG, "loadSplashInterAds: delay validate");
                    isTimeDelay = true;
                }
            }, timeDelay);
            if (timeOut > 0) {
                handlerTimeout = new Handler();
                rdTimeout = new Runnable() {
                    @Override
                    public void run() {
                        Log.e(TAG, "loadSplashInterstitalAds: on timeout");
                        isTimeout = true;
                        if (mInterstitialSplash != null) {
                            Log.i(TAG, "loadSplashInterstitalAds:show ad on timeout ");
                            onShowSplash((Activity) context, adListener);
                            return;
                        }
                        if (adListener != null) {
                            adListener.onAdClosed();
                            isShowLoadingSplash = false;
                        }
                    }
                };
                handlerTimeout.postDelayed(rdTimeout, timeOut);
            }

            isShowLoadingSplash = true;
            loadInterAds(context, id, new InterCallback() {
                @Override
                public void onInterstitialLoad(InterstitialAd interstitialAd) {
                    super.onInterstitialLoad(interstitialAd);
                    adListener.onInterstitialLoad(interstitialAd);
                    Log.e(TAG, "loadSplashInterstitalAds  end time loading success:" + Calendar.getInstance().getTimeInMillis() + "     time limit:" + isTimeout);
                    if (isTimeout)
                        return;
                    if (interstitialAd != null) {
                        mInterstitialSplash = interstitialAd;
                        if (isTimeDelay) {
                            onShowSplash((Activity) context, adListener);
                            Log.i(TAG, "loadSplashInterstitalAds:show ad on loaded ");
                        }
                    }
                }

                @Override
                public void onAdFailedToLoad(LoadAdError i) {
                    super.onAdFailedToLoad(i);
                    Log.e(TAG, "loadSplashInterstitalAds  end time loading error:" + Calendar.getInstance().getTimeInMillis() + "     time limit:" + isTimeout);
                    if (isTimeout)
                        return;
                    if (adListener != null) {
                        if (handlerTimeout != null && rdTimeout != null) {
                            handlerTimeout.removeCallbacks(rdTimeout);
                        }
                        if (i != null)
                            Log.e(TAG, "loadSplashInterstitalAds: load fail " + i.getMessage());
                        adListener.onAdFailedToLoad(i);
                    }
                }

                @Override
                public void onAdClicked() {
                    super.onAdClicked();
                    FirebaseAnalyticsUtil.logClickAdsEventByActivity(context,FirebaseAnalyticsUtil.INTER);
                    FirebaseAnalyticsUtil.logClickAdsEventAdmob(context);
                    if(timeLimitAds>1000)
                        setTimeLimitInter();
                }
            });
        }



    }



    private void onShowSplash(Activity activity, InterCallback adListener) {
        isShowLoadingSplash = true;
        if (mInterstitialSplash == null) {
            adListener.onAdClosed();
            return;
        }else{
            adListener.onAdLoadedSuccess();
        }
        if (handlerTimeout != null && rdTimeout != null) {
            handlerTimeout.removeCallbacks(rdTimeout);
        }

        if (adListener != null) {
            adListener.onAdLoaded();
        }

        mInterstitialSplash.setFullScreenContentCallback(new FullScreenContentCallback() {
            @Override
            public void onAdShowedFullScreenContent() {
                isShowLoadingSplash = false;
                adListener.onAdShowSuccess();
            }
            @Override
            public void onAdDismissedFullScreenContent() {
                if (AppOpenManager.getInstance().isInitialized()) {
                    AppOpenManager.getInstance().enableAppResume();
                }
                if (adListener != null) {
                    if (!openActivityAfterShowInterAds) {
                        adListener.onAdClosed();
                        adListener.onNextAction();
                    }else {
                        adListener.onAdClosedByUser();
                    }

                    if (dialog != null) {
                        dialog.dismiss();
                    }

                }
                mInterstitialSplash = null;
                isShowLoadingSplash = false;
            }

            @Override
            public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                mInterstitialSplash = null;
                isShowLoadingSplash = false;
                if (adListener != null) {
                    if (!openActivityAfterShowInterAds) {
                        adListener.onAdFailedToShow(adError);
                        adListener.onNextAction();
                    }

                    if (dialog != null) {
                        dialog.dismiss();
                    }
                }
            }

            @Override
            public void onAdClicked() {
                super.onAdClicked();
                FirebaseAnalyticsUtil.logClickAdsEventByActivity(activity,FirebaseAnalyticsUtil.INTER);
                FirebaseAnalyticsUtil.logClickAdsEventAdmob(activity);
                adListener.onAdClicked();
                if(timeLimitAds>1000){setTimeLimitInter();}
            }

        });
        if (ProcessLifecycleOwner.get().getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.RESUMED)) {
            try {
                if (dialog != null && dialog.isShowing())
                    dialog.dismiss();
                dialog = new LoadingAdsDialog(activity);
                try {
                    dialog.show();
                } catch (Exception e) {
                    adListener.onAdClosed();
                    adListener.onNextAction();
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
                    adListener.onNextAction();
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (dialog != null && dialog.isShowing() && !activity.isDestroyed())
                                dialog.dismiss();
                        }
                    }, 1500);
                }

                if(activity!=null){
                    mInterstitialSplash.show(activity);
                    isShowLoadingSplash = false;
                }else if (adListener != null) {
                    if (dialog != null) {
                        dialog.dismiss();
                    }
                    adListener.onAdClosed();
                    adListener.onNextAction();
                    isShowLoadingSplash = false;
                }
            }, 800);
        }else{
            isShowLoadingSplash = false;
            Log.e(TAG, "onShowSplash: fail on background");
        }
    }

    /* =============================End Inter Splash==========================================*/

    /* =============================   Inter ==========================================*/

    /**
     Load ads inter
     Return 1 inter ads
     */

    public void loadInterAds(Context context, String id, InterCallback adCallback) {
        if (!isShowAllAds ) {
            adCallback.onInterstitialLoad(null);
            return;
        }

        if(isShowInter){
            isTimeout = false;
            interstitialAd = null;
            InterstitialAd.load(context, id, getAdRequest(),
                    new InterstitialAdLoadCallback() {
                        @Override
                        public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                            if(adCallback!=null){
                                adCallback.onInterstitialLoad(interstitialAd);
                            }
                        }

                        @Override
                        public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                            // Handle the error
                            Log.i(TAG, loadAdError.getMessage());
                            if(adCallback!=null)
                                adCallback.onAdFailedToLoad(loadAdError);
                        }

                    });
        }
    }
    /**
     Show ads inter
     */
    public void showInterAds(Context context, InterstitialAd mInterstitialAd, final InterCallback callback) {
        showInterAds(context, mInterstitialAd, callback, true);

    }

    private void showInterAds(Context context, InterstitialAd mInterstitialAd, final InterCallback callback, boolean shouldReload) {
        currentClicked = numShowAds;
        showInterAdByTimes(context, mInterstitialAd, callback, shouldReload);
    }

    private void showInterAdByTimes(final Context context, InterstitialAd mInterstitialAd, final InterCallback callback, final boolean shouldReloadAds) {
        Helper.setupAdmodData(context);
        if (!isShowAllAds ) {
            callback.onAdClosed();
            callback.onNextAction();
            return;
        }
        if (mInterstitialAd == null) {
            if (callback != null) {
                callback.onAdClosed();
                callback.onNextAction();
            }
            return;
        }else{
            callback.onAdLoadedSuccess();
        }

        mInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {

            @Override
            public void onAdDismissedFullScreenContent() {
                super.onAdDismissedFullScreenContent();
                // Called when fullscreen content is dismissed.
                if (AppOpenManager.getInstance().isInitialized()) {
                    AppOpenManager.getInstance().enableAppResume();
                }
                if (callback != null) {
                    if (!openActivityAfterShowInterAds) {
                        callback.onAdClosed();
                        callback.onNextAction();
                    }else {
                        callback.onAdClosedByUser();
                    }

                    if (dialog != null) {
                        dialog.dismiss();
                    }

                }
                Log.e(TAG, "onAdDismissedFullScreenContent");
            }

            @Override
            public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                super.onAdFailedToShowFullScreenContent(adError);
                Log.e(TAG, "onAdFailedToShowFullScreenContent: " + adError.getMessage());
                callback.onAdFailedToShow(adError);
                callback.onNextAction();
                if (callback != null) {
                    if (dialog != null) {
                        dialog.dismiss();
                    }
                }
            }

            @Override
            public void onAdShowedFullScreenContent() {
                super.onAdShowedFullScreenContent();
                callback.onAdShowSuccess();
                // Called when fullscreen content is shown.
            }

            @Override
            public void onAdClicked() {
                super.onAdClicked();
                FirebaseAnalyticsUtil.logClickAdsEventByActivity(context,FirebaseAnalyticsUtil.INTER);
                FirebaseAnalyticsUtil.logClickAdsEventAdmob(context);
                if(timeLimitAds>1000)
                    setTimeLimitInter();
            }
        });

        if (Helper.getNumClickAdsPerDay(context, mInterstitialAd.getAdUnitId()) < maxClickAds) {
            showInterstitialAd(context, mInterstitialAd, callback);
            return;
        }
        if (callback != null) {
            callback.onAdClosed();
            callback.onNextAction();
        }
    }

    private void showInterstitialAd(Context context, InterstitialAd mInterstitialAd, InterCallback callback) {
        if(!isShowInter){
            callback.onAdClosed();
            callback.onNextAction();
            return;
        }
        currentClicked++;
        if (currentClicked >= numShowAds && mInterstitialAd != null) {
            if (ProcessLifecycleOwner.get().getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.RESUMED)) {
                try {
                    if (dialog != null && dialog.isShowing())
                        dialog.dismiss();
                    dialog = new LoadingAdsDialog(context);
                    try {
                        dialog.show();
                    } catch (Exception e) {
                        callback.onAdClosed();
                        callback.onNextAction();
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

                    if (openActivityAfterShowInterAds && callback != null) {
                        callback.onAdClosed();
                        callback.onNextAction();
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if (dialog != null && dialog.isShowing() && !((Activity) context).isDestroyed())
                                    dialog.dismiss();
                            }
                        }, 1500);
                    }

                    mInterstitialAd.show((Activity) context);

                }, 800);

            }
            currentClicked = 0;
        } else if (callback != null) {
            if (dialog != null) {
                dialog.dismiss();
            }
            callback.onAdClosed();
            callback.onNextAction();
        }
    }


    /**
     load and show ads inter
     */
    public void loadAndShowInter(AppCompatActivity activity, String idInter, int timeDelay, int timeOut, InterCallback callback){
        if (!isNetworkConnected()) {
            callback.onAdClosed();
            callback.onNextAction();
            return;
        }
        if (!isShowAllAds&&!isShowInter) {
            callback.onAdClosed();
            callback.onNextAction();
            return;
        }

        if (AppOpenManager.getInstance().isInitialized()) {
            AppOpenManager.getInstance().disableAppResumeWithActivity(activity.getClass());
        }
        Dialog dialog2 = new LoadingAdsDialog(activity);
        dialog2.show();
        InterstitialAd.load(activity, idInter, getAdRequestTimeOut(timeOut), new InterstitialAdLoadCallback() {
            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                super.onAdFailedToLoad(loadAdError);
                dialog2.dismiss();
                callback.onAdFailedToLoad(loadAdError);
                if (AppOpenManager.getInstance().isInitialized()) {
                    AppOpenManager.getInstance().enableAppResumeWithActivity(activity.getClass());
                }
            }

            @Override
            public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                super.onAdLoaded(interstitialAd);
                if(interstitialAd!=null){
                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        interstitialAd.setFullScreenContentCallback(new FullScreenContentCallback(){
                            @Override
                            public void onAdDismissedFullScreenContent() {
                                dialog2.dismiss();
                                callback.onAdClosed();
                                callback.onNextAction();
                                if (AppOpenManager.getInstance().isInitialized()) {
                                    AppOpenManager.getInstance().enableAppResumeWithActivity(activity.getClass());
                                }
                            }

                            @Override
                            public void onAdFailedToShowFullScreenContent(AdError adError) {
                                dialog2.dismiss();
                                callback.onAdFailedToShow(adError);
                                callback.onNextAction();
                                if (AppOpenManager.getInstance().isInitialized()) {
                                    AppOpenManager.getInstance().enableAppResumeWithActivity(activity.getClass());
                                }
                            }

                            @Override
                            public void onAdShowedFullScreenContent() {
                                callback.onAdShowSuccess();
                                if (AppOpenManager.getInstance().isInitialized()) {
                                    AppOpenManager.getInstance().disableAppResume();
                                }
                            }

                            @Override
                            public void onAdClicked() {
                                super.onAdClicked();
                                FirebaseAnalyticsUtil.logClickAdsEventByActivity(activity,FirebaseAnalyticsUtil.INTER);
                                FirebaseAnalyticsUtil.logClickAdsEventAdmob(activity);
                                if(timeLimitAds>1000){setTimeLimitInter();}
                            }
                        });
                        if (activity.getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.RESUMED) && interstitialAd != null) {
                            interstitialAd.show(activity);
                        } else {
                            if(interstitialAd!=null){
                                if (AppOpenManager.getInstance().isInitialized()) {
                                    AppOpenManager.getInstance().enableAppResumeWithActivity(activity.getClass());
                                    dialog2.dismiss();
                                }
                            }
                            // dialog.dismiss();
                        }
                    },timeDelay);
                }
            }
        });
    }


    /* ============================= End  Inter  ==========================================*/


    /* =============================  Rewarded Ads ==========================================*/

    public void showRewardAds(final Activity context, final RewardCallback adCallback) {
        if (rewardedAd == null) {
            initRewardAds(context, rewardedId);
            adCallback.onAdFailedToShow(0);
            return;
        } else {
            Admod.this.rewardedAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                @Override
                public void onAdDismissedFullScreenContent() {
                    super.onAdDismissedFullScreenContent();
                    if (adCallback != null)
                        adCallback.onAdClosed();
                    if (AppOpenManager.getInstance().isInitialized()) {
                        AppOpenManager.getInstance().enableAppResume();
                    }

                }

                @Override
                public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                    super.onAdFailedToShowFullScreenContent(adError);
                    if (adCallback != null)
                        adCallback.onAdFailedToShow(adError.getCode());
                }

                @Override
                public void onAdShowedFullScreenContent() {
                    super.onAdShowedFullScreenContent();
                    if (AppOpenManager.getInstance().isInitialized()) {
                        AppOpenManager.getInstance().disableAppResume();
                    }
                    initRewardAds(context, rewardedId);
                    rewardedAd = null;
                }

                public void onAdClicked() {
                    super.onAdClicked();
                }
            });
            rewardedAd.show(context, new OnUserEarnedRewardListener() {
                @Override
                public void onUserEarnedReward(@NonNull RewardItem rewardItem) {
                    if (adCallback != null) {
                        adCallback.onEarnedReward(rewardItem);

                    }
                }
            });
        }
    }
    public void initRewardAds(Context context, String id) {
        if (!isShowAllAds) {
            return;
        }
        this.rewardedId = id;
        RewardedAd.load(context, id, getAdRequest(), new RewardedAdLoadCallback() {
            @Override
            public void onAdLoaded(@NonNull RewardedAd rewardedAd) {
                Admod.this.rewardedAd = rewardedAd;
            }
            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                super.onAdFailedToLoad(loadAdError);
                Log.e(TAG, "RewardedAd onAdFailedToLoad: " + loadAdError.getMessage());
            }
        });
    }

    /* =============================  End Rewarded Ads ==========================================*/




    /* =============================  Native Ads ==========================================*/

    public void loadNativeAd(Context context, String id, final NativeCallback callback) {
        if (!isShowAllAds) {
            callback.onAdFailedToLoad();
            return;
        }
        if(isShowNative){
            if(isNetworkConnected()){
                VideoOptions videoOptions = new VideoOptions.Builder()
                        .setStartMuted(true)
                        .build();

                NativeAdOptions adOptions = new NativeAdOptions.Builder()
                        .setVideoOptions(videoOptions)
                        .build();
                AdLoader adLoader = new AdLoader.Builder(context, id)
                        .forNativeAd(new NativeAd.OnNativeAdLoadedListener() {

                            @Override
                            public void onNativeAdLoaded(@NonNull NativeAd nativeAd) {
                                callback.onNativeAdLoaded(nativeAd);
                            }
                        })
                        .withAdListener(new AdListener() {
                            @Override
                            public void onAdFailedToLoad(LoadAdError error) {
                                Log.e(TAG, "NativeAd onAdFailedToLoad: " + error.getMessage());
                                callback.onAdFailedToLoad();
                            }

                            @Override
                            public void onAdClicked() {
                                super.onAdClicked();
                                FirebaseAnalyticsUtil.logClickAdsEventAdmob(context);
                                FirebaseAnalyticsUtil.logClickAdsEventByActivity(context,FirebaseAnalyticsUtil.NATIVE);
                                if(timeLimitAds>1000){
                                    setTimeLimitNative();
                                    if (callback != null) {
                                        callback.onAdFailedToLoad();
                                    }
                                }
                            }
                        })
                        .withNativeAdOptions(adOptions)
                        .build();
                adLoader.loadAd(getAdRequest());
            }else{
                callback.onAdFailedToLoad();
            }
        }else{
            callback.onAdFailedToLoad();
        }


    }

    public void pushAdsToViewCustom(NativeAd nativeAd, NativeAdView adView) {
        adView.setMediaView(adView.findViewById(R.id.ad_media));
        if (adView.getMediaView() != null) {
            adView.getMediaView().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (context != null && BuildConfig.DEBUG) {
                        float sizeMin = TypedValue.applyDimension(
                                TypedValue.COMPLEX_UNIT_DIP,
                                120,
                                context.getResources().getDisplayMetrics()
                        );
                        Log.e(TAG, "Native sizeMin: " + sizeMin);
                        Log.e(TAG, "Native w/h media : " + adView.getMediaView().getWidth() + "/" + adView.getMediaView().getHeight());
                        if (adView.getMediaView().getWidth() < sizeMin || adView.getMediaView().getHeight() < sizeMin) {
                            Toast.makeText(context, "Size media native not valid", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }, 1000);

        }
        // Set other ad assets.
        adView.setHeadlineView(adView.findViewById(R.id.ad_headline));
        adView.setBodyView(adView.findViewById(R.id.ad_body));
        adView.setCallToActionView(adView.findViewById(R.id.ad_call_to_action));
        adView.setIconView(adView.findViewById(R.id.ad_app_icon));
        adView.setAdvertiserView(adView.findViewById(R.id.ad_advertiser));

        // The headline is guaranteed to be in every UnifiedNativeAd.
        try {
            ((TextView) adView.getHeadlineView()).setText(nativeAd.getHeadline());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // These assets aren't guaranteed to be in every UnifiedNativeAd, so it's important to
        // check before trying to display them.
        try {
            if (nativeAd.getBody() == null) {
                adView.getBodyView().setVisibility(View.INVISIBLE);
            } else {
                adView.getBodyView().setVisibility(View.VISIBLE);
                ((TextView) adView.getBodyView()).setText(nativeAd.getBody());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            if (nativeAd.getCallToAction() == null) {
                Objects.requireNonNull(adView.getCallToActionView()).setVisibility(View.INVISIBLE);
            } else {
                Objects.requireNonNull(adView.getCallToActionView()).setVisibility(View.VISIBLE);
                ((TextView) adView.getCallToActionView()).setText(nativeAd.getCallToAction());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            if (nativeAd.getIcon() == null) {
                Objects.requireNonNull(adView.getIconView()).setVisibility(View.GONE);
            } else {
                ((ImageView) adView.getIconView()).setImageDrawable(
                        nativeAd.getIcon().getDrawable());
                adView.getIconView().setVisibility(View.VISIBLE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            if (nativeAd.getPrice() == null) {
                Objects.requireNonNull(adView.getPriceView()).setVisibility(View.INVISIBLE);
            } else {
                Objects.requireNonNull(adView.getPriceView()).setVisibility(View.VISIBLE);
                ((TextView) adView.getPriceView()).setText(nativeAd.getPrice());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            if (nativeAd.getStore() == null) {
                Objects.requireNonNull(adView.getStoreView()).setVisibility(View.INVISIBLE);
            } else {
                Objects.requireNonNull(adView.getStoreView()).setVisibility(View.VISIBLE);
                ((TextView) adView.getStoreView()).setText(nativeAd.getStore());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            if (nativeAd.getStarRating() == null) {
                Objects.requireNonNull(adView.getStarRatingView()).setVisibility(View.INVISIBLE);
            } else {
                ((RatingBar) Objects.requireNonNull(adView.getStarRatingView()))
                        .setRating(nativeAd.getStarRating().floatValue());
                adView.getStarRatingView().setVisibility(View.VISIBLE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            if (nativeAd.getAdvertiser() == null) {
                adView.getAdvertiserView().setVisibility(View.INVISIBLE);
            } else {
                ((TextView) adView.getAdvertiserView()).setText(nativeAd.getAdvertiser());
                adView.getAdvertiserView().setVisibility(View.VISIBLE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // This method tells the Google Mobile Ads SDK that you have finished populating your
        // native ad view with this native ad. The SDK will populate the adView's MediaView
        // with the media content from this native ad.
        adView.setNativeAd(nativeAd);

    }


    public void loadNativeFragment(final Activity mActivity, String id, View parent) {
        final FrameLayout frameLayout = parent.findViewById(R.id.fl_load_native);
        final ShimmerFrameLayout containerShimmer = parent.findViewById(R.id.shimmer_container_native);
        loadNative(mActivity, containerShimmer, frameLayout, id, R.layout.native_admob_ad);
    }
    private void loadNative(final Context context, final ShimmerFrameLayout containerShimmer, final FrameLayout frameLayout, final String id, final int layout) {
        if (!isShowAllAds) {
            containerShimmer.setVisibility(View.GONE);
            return;
        }
        frameLayout.removeAllViews();
        frameLayout.setVisibility(View.GONE);
        containerShimmer.setVisibility(View.VISIBLE);
        containerShimmer.startShimmer();

        VideoOptions videoOptions = new VideoOptions.Builder()
                .setStartMuted(true)
                .build();

        NativeAdOptions adOptions = new NativeAdOptions.Builder()
                .setVideoOptions(videoOptions)
                .build();


        AdLoader adLoader = new AdLoader.Builder(context, id)
                .forNativeAd(new NativeAd.OnNativeAdLoadedListener() {

                    @Override
                    public void onNativeAdLoaded(@NonNull NativeAd nativeAd) {
                        containerShimmer.stopShimmer();
                        containerShimmer.setVisibility(View.GONE);
                        frameLayout.setVisibility(View.VISIBLE);
                        @SuppressLint("InflateParams") NativeAdView adView = (NativeAdView) LayoutInflater.from(context)
                                .inflate(layout, null);
                        pushAdsToViewCustom(nativeAd, adView);
                        frameLayout.removeAllViews();
                        frameLayout.addView(adView);
                    }


                })
                .withAdListener(new AdListener() {
                    @Override
                    public void onAdFailedToLoad(LoadAdError error) {
                        Log.e(TAG, "onAdFailedToLoad: " + error.getMessage());
                        containerShimmer.stopShimmer();
                        containerShimmer.setVisibility(View.GONE);
                        frameLayout.setVisibility(View.GONE);
                    }
                    @Override
                    public void onAdClicked() {
                        super.onAdClicked();
                        FirebaseAnalyticsUtil.logClickAdsEventByActivity(context,FirebaseAnalyticsUtil.NATIVE);
                        FirebaseAnalyticsUtil.logClickAdsEventAdmob(context);
                    }

                })
                .withNativeAdOptions(adOptions)
                .build();

        adLoader.loadAd(getAdRequest());
    }
    /* =============================  End Native Ads ==========================================*/


    public AdRequest getAdRequest() {
        AdRequest.Builder builder = new AdRequest.Builder();
        return builder.build();
    }
    private AdRequest getAdRequestTimeOut(int timeOut) {
        if(timeOut<5000) timeOut = 5000;
        return (AdRequest) new AdRequest.Builder().setHttpTimeoutMillis(timeOut).build();
    }

    public void setOpenActivityAfterShowInterAds(boolean openActivityAfterShowInterAds) {
        this.openActivityAfterShowInterAds = openActivityAfterShowInterAds;
    }



    /* =============================  GET  INFO DEVICE  ==========================================*/
    @SuppressLint("HardwareIds")
    public String getDeviceId(Activity activity) {
        String android_id = Settings.Secure.getString(activity.getContentResolver(),
                Settings.Secure.ANDROID_ID);
        return md5(android_id).toUpperCase();
    }
    private String md5(final String s) {
        try {
            // Create MD5 Hash
            MessageDigest digest = java.security.MessageDigest
                    .getInstance("MD5");
            digest.update(s.getBytes());
            byte messageDigest[] = digest.digest();

            // Create Hex String
            StringBuffer hexString = new StringBuffer();
            for (int i = 0; i < messageDigest.length; i++) {
                String h = Integer.toHexString(0xFF & messageDigest[i]);
                while (h.length() < 2)
                    h = "0" + h;
                hexString.append(h);
            }
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
        }
        return "";
    }
    /* ============================= END GET  INFO DEVICE  ==========================================*/
    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected();
    }


    private void setTimeLimitInter() {
        if(timeLimitAds>1000){
            isShowInter = false;
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    isShowInter = true;
                }
            }, timeLimitAds);
        }
    }
    private void setTimeLimitBanner() {
        if(timeLimitAds>1000){
            isShowBanner = false;
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    isShowBanner = true;
                }
            }, timeLimitAds);
        }

    }
    private void setTimeLimitNative() {
        if(timeLimitAds>1000){
            isShowNative = false;
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    isShowNative = true;
                }
            }, timeLimitAds);
        }

    }
    public void setTimeLimit(long timeLimit) {
        this.timeLimitAds = timeLimit;
    }

    public long getTimeLimit() {
        return this.timeLimitAds;
    }
}