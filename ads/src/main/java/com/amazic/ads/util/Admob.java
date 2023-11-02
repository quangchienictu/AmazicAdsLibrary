package com.amazic.ads.util;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.app.Dialog;
import android.content.Context;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
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
import com.amazic.ads.callback.BannerCallBack;
import com.amazic.ads.callback.InterCallback;
import com.amazic.ads.callback.NativeCallback;
import com.amazic.ads.callback.RewardCallback;
import com.amazic.ads.dialog.LoadingAdsDialog;
import com.amazic.ads.event.AdType;
import com.amazic.ads.event.FirebaseUtil;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.ads.mediation.admob.AdMobAdapter;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;

public class Admob {
    private static Admob INSTANCE;
    private static final String TAG = "Admob";
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
    private boolean disableAdResumeWhenClickAds = false;
    public static final String BANNER_INLINE_SMALL_STYLE = "BANNER_INLINE_SMALL_STYLE";
    public static final String BANNER_INLINE_LARGE_STYLE = "BANNER_INLINE_LARGE_STYLE";
    private static int MAX_SMALL_INLINE_BANNER_HEIGHT = 50;

    public static long timeLimitAds = 0; // Set > 1000 nếu cần limit ads click
    private boolean isShowInter = true;
    private boolean isShowBanner = true;
    private boolean isShowNative = true;
    private boolean logTimeLoadAdsSplash = false;
    private boolean logLogTimeShowAds = false;
    public static boolean isShowAllAds = true;
    private boolean isFan = false;
    private long currentTime;
    private long currentTimeShowAds;
    private boolean checkLoadBanner = false;
    private boolean checkLoadBannerCollap = false;
    private long timeInterval = 0L;
    private long lastTimeDismissInter = 0L;
    private StateInter stateInter = StateInter.DISMISS;

    enum StateInter {SHOWING, SHOWED, DISMISS}

    public static Admob getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new Admob();
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

    /* =======================   Banner ================================= */

    /**
     * Set tắt ads resume khi click ads
     */
    public void setDisableAdResumeWhenClickAds(boolean disableAdResumeWhenClickAds) {
        this.disableAdResumeWhenClickAds = disableAdResumeWhenClickAds;
    }


    /**
     * Set tắt toàn bộ ads trong project
     **/
    public void setOpenShowAllAds(boolean isShowAllAds) {
        this.isShowAllAds = isShowAllAds;
    }

    /**
     * Set tắt event log time load splash
     **/
    public void setOpenEventLoadTimeLoadAdsSplash(boolean logTimeLoadAdsSplash) {
        this.logTimeLoadAdsSplash = logTimeLoadAdsSplash;
    }

    /**
     * Set tắt event log time show splash
     **/
    public void setOpenEventLoadTimeShowAdsInter(boolean logLogTimeShowAds) {
        this.logLogTimeShowAds = logLogTimeShowAds;
    }

    /*=================================Banner ======================================/
 /**
      * Load quảng cáo Banner Trong Activity
      *
      * @param mActivity
      * @param id
      */
    public void hideBanner(final Activity mActivity) {
        final View viewAds = mActivity.findViewById(R.id.ll_ads);
        if (viewAds != null) {
            viewAds.setVisibility(View.GONE);
        }
    }

    public void loadBanner(final Activity mActivity, String id) {
        final FrameLayout adContainer = mActivity.findViewById(R.id.banner_container);
        final ShimmerFrameLayout containerShimmer = mActivity.findViewById(R.id.shimmer_container_banner);
        if (!isShowAllAds || !isNetworkConnected()) {
            adContainer.setVisibility(View.GONE);
            containerShimmer.setVisibility(View.GONE);
        } else {
            loadBanner(mActivity, id, adContainer, containerShimmer, null, false, BANNER_INLINE_LARGE_STYLE);
        }
    }

    public void loadBannerFloor(final Activity mActivity, List<String> listID) {
        Log.e("Admob", "Load Native ID Floor");
        final FrameLayout adContainer = mActivity.findViewById(R.id.banner_container);
        final ShimmerFrameLayout containerShimmer = mActivity.findViewById(R.id.shimmer_container_banner);
        if (!isShowAllAds || !isNetworkConnected()) {
            adContainer.setVisibility(View.GONE);
            containerShimmer.setVisibility(View.GONE);
        } else {
            if (listID == null) {
                adContainer.setVisibility(View.GONE);
                containerShimmer.setVisibility(View.GONE);
                return;
            }
            if (listID.size() == 0) {
                adContainer.setVisibility(View.GONE);
                containerShimmer.setVisibility(View.GONE);
                return;
            }
            List idNew = new ArrayList();
            for (String id : listID) {
                idNew.add(id);
            }
            checkLoadBanner = false;
            loadBannerFloor(mActivity, idNew, adContainer, containerShimmer, null, false, BANNER_INLINE_LARGE_STYLE);
        }
    }

    public void loadBannerFloor(final Activity mActivity, List<String> listID, BannerCallBack bannerCallBack) {
        Log.e("Admob", "Load Native ID Floor");
        final FrameLayout adContainer = mActivity.findViewById(R.id.banner_container);
        final ShimmerFrameLayout containerShimmer = mActivity.findViewById(R.id.shimmer_container_banner);
        if (!isShowAllAds || !isNetworkConnected()) {
            adContainer.setVisibility(View.GONE);
            containerShimmer.setVisibility(View.GONE);
        } else {
            if (listID == null) {
                adContainer.setVisibility(View.GONE);
                containerShimmer.setVisibility(View.GONE);
                return;
            }
            if (listID.size() == 0) {
                adContainer.setVisibility(View.GONE);
                containerShimmer.setVisibility(View.GONE);
                return;
            }
            List idNew = new ArrayList();
            for (String id : listID) {
                idNew.add(id);
            }
            checkLoadBanner = false;
            loadBannerFloor(mActivity, idNew, adContainer, containerShimmer, bannerCallBack, false, BANNER_INLINE_LARGE_STYLE);
        }
    }

    /**
     * Load quảng cáo Banner Trong Activity
     */
    public void loadBanner(final Activity mActivity, String id, BannerCallBack callback) {
        final FrameLayout adContainer = mActivity.findViewById(R.id.banner_container);
        final ShimmerFrameLayout containerShimmer = mActivity.findViewById(R.id.shimmer_container_banner);
        if (!isShowAllAds || !isNetworkConnected()) {
            adContainer.setVisibility(View.GONE);
            containerShimmer.setVisibility(View.GONE);
        } else {
            loadBanner(mActivity, id, adContainer, containerShimmer, callback, false, BANNER_INLINE_LARGE_STYLE);
        }
    }


    /**
     * Load quảng cáo Banner Trong Activity set Inline adaptive banners
     */
    public void loadBanner(final Activity mActivity, String id, Boolean useInlineAdaptive) {
        final FrameLayout adContainer = mActivity.findViewById(R.id.banner_container);
        final ShimmerFrameLayout containerShimmer = mActivity.findViewById(R.id.shimmer_container_banner);
        if (!isShowAllAds || !isNetworkConnected()) {
            adContainer.setVisibility(View.GONE);
            containerShimmer.setVisibility(View.GONE);
        } else {
            loadBanner(mActivity, id, adContainer, containerShimmer, null, useInlineAdaptive, BANNER_INLINE_LARGE_STYLE);
        }
    }

    /**
     * Load quảng cáo Banner Trong Activity set Inline adaptive banners
     */
    public void loadInlineBanner(final Activity activity, String id, String inlineStyle) {
        final FrameLayout adContainer = activity.findViewById(R.id.banner_container);
        final ShimmerFrameLayout containerShimmer = activity.findViewById(R.id.shimmer_container_banner);
        loadBanner(activity, id, adContainer, containerShimmer, null, true, inlineStyle);
    }

    /**
     * Load quảng cáo Banner Trong Activity set Inline adaptive banners
     */
    public void loadBanner(final Activity mActivity, String id, final BannerCallBack callback, Boolean useInlineAdaptive) {
        final FrameLayout adContainer = mActivity.findViewById(R.id.banner_container);
        final ShimmerFrameLayout containerShimmer = mActivity.findViewById(R.id.shimmer_container_banner);
        if (!isShowAllAds || !isNetworkConnected()) {
            adContainer.setVisibility(View.GONE);
            containerShimmer.setVisibility(View.GONE);
        } else {
            loadBanner(mActivity, id, adContainer, containerShimmer, callback, useInlineAdaptive, BANNER_INLINE_LARGE_STYLE);
        }
    }

    /**
     * Load quảng cáo Banner Trong Activity set Inline adaptive banners
     */
    public void loadInlineBanner(final Activity activity, String id, String inlineStyle, final BannerCallBack callback) {
        final FrameLayout adContainer = activity.findViewById(R.id.banner_container);
        final ShimmerFrameLayout containerShimmer = activity.findViewById(R.id.shimmer_container_banner);
        if (!isShowAllAds || !isNetworkConnected()) {
            adContainer.setVisibility(View.GONE);
            containerShimmer.setVisibility(View.GONE);
        } else {
            loadBanner(activity, id, adContainer, containerShimmer, callback, true, inlineStyle);
        }
    }

    /**
     * Load quảng cáo Collapsible Banner Trong Activity
     */
    public void loadCollapsibleBanner(final Activity mActivity, String id, String gravity) {
        final FrameLayout adContainer = mActivity.findViewById(R.id.banner_container);
        final ShimmerFrameLayout containerShimmer = mActivity.findViewById(R.id.shimmer_container_banner);
        if (!isShowAllAds || !isNetworkConnected()) {
            adContainer.setVisibility(View.GONE);
            containerShimmer.setVisibility(View.GONE);
        } else {
            loadCollapsibleBanner(mActivity, id, gravity, adContainer, containerShimmer);
        }
    }

    public void loadCollapsibleBannerFloor(final Activity mActivity, List<String> listID, String gravity) {
        final FrameLayout adContainer = mActivity.findViewById(R.id.banner_container);
        final ShimmerFrameLayout containerShimmer = mActivity.findViewById(R.id.shimmer_container_banner);
        if (!isShowAllAds || !isNetworkConnected()) {
            adContainer.setVisibility(View.GONE);
            containerShimmer.setVisibility(View.GONE);
        } else {
            if (listID == null) {
                adContainer.setVisibility(View.GONE);
                containerShimmer.setVisibility(View.GONE);
                return;
            }
            if (listID.size() < 1) {
                adContainer.setVisibility(View.GONE);
                containerShimmer.setVisibility(View.GONE);
                return;
            }
            List idNew = new ArrayList();
            for (String id : listID) {
                idNew.add(id);
            }
            checkLoadBannerCollap = false;
            loadCollapsibleBannerFloor(mActivity, idNew, gravity, adContainer, containerShimmer);
        }


    }

    public void loadCollapsibleBannerFloor(final Activity mActivity, List<String> listID, String gravity, BannerCallBack bannerCallBack) {
        final FrameLayout adContainer = mActivity.findViewById(R.id.banner_container);
        final ShimmerFrameLayout containerShimmer = mActivity.findViewById(R.id.shimmer_container_banner);
        if (!isShowAllAds || !isNetworkConnected()) {
            adContainer.setVisibility(View.GONE);
            containerShimmer.setVisibility(View.GONE);
        } else {
            if (listID == null) {
                adContainer.setVisibility(View.GONE);
                containerShimmer.setVisibility(View.GONE);
                return;
            }
            if (listID.size() < 1) {
                adContainer.setVisibility(View.GONE);
                containerShimmer.setVisibility(View.GONE);
                return;
            }
            List idNew = new ArrayList();
            for (String id : listID) {
                idNew.add(id);
            }
            checkLoadBannerCollap = false;
            loadCollapsibleBannerFloor(mActivity, idNew, gravity, adContainer, containerShimmer, bannerCallBack);
        }


    }

    public void loadBannerFragmentFloor(final Activity mActivity, List<String> listID, final View rootView) {
        final FrameLayout adContainer = rootView.findViewById(R.id.banner_container);
        final ShimmerFrameLayout containerShimmer = rootView.findViewById(R.id.shimmer_container_banner);

        if (!isShowAllAds || !isNetworkConnected()) {
            adContainer.setVisibility(View.GONE);
            containerShimmer.setVisibility(View.GONE);
        } else {
            if (listID == null) {
                adContainer.setVisibility(View.GONE);
                containerShimmer.setVisibility(View.GONE);
                return;
            }
            if (listID.size() == 0) {
                adContainer.setVisibility(View.GONE);
                containerShimmer.setVisibility(View.GONE);
                return;
            }
            List idNew = new ArrayList();
            for (String id : listID) {
                idNew.add(id);
            }
            checkLoadBanner = false;
            loadBannerFloor(mActivity, idNew, adContainer, containerShimmer, null, false, BANNER_INLINE_LARGE_STYLE);
        }
    }

    public void loadBannerFragmentFloor(final Activity mActivity, List<String> listID, final View rootView, BannerCallBack bannerCallBack) {
        final FrameLayout adContainer = rootView.findViewById(R.id.banner_container);
        final ShimmerFrameLayout containerShimmer = rootView.findViewById(R.id.shimmer_container_banner);

        if (!isShowAllAds || !isNetworkConnected()) {
            adContainer.setVisibility(View.GONE);
            containerShimmer.setVisibility(View.GONE);
        } else {
            if (listID == null) {
                adContainer.setVisibility(View.GONE);
                containerShimmer.setVisibility(View.GONE);
                return;
            }
            if (listID.size() == 0) {
                adContainer.setVisibility(View.GONE);
                containerShimmer.setVisibility(View.GONE);
                return;
            }
            List idNew = new ArrayList();
            for (String id : listID) {
                idNew.add(id);
            }
            checkLoadBanner = false;
            loadBannerFloor(mActivity, idNew, adContainer, containerShimmer, bannerCallBack, false, BANNER_INLINE_LARGE_STYLE);
        }
    }

    /**
     * Load Quảng Cáo Banner Trong Fragment
     */
    public void loadBannerFragment(final Activity mActivity, String id, final View rootView) {
        final FrameLayout adContainer = rootView.findViewById(R.id.banner_container);
        final ShimmerFrameLayout containerShimmer = rootView.findViewById(R.id.shimmer_container_banner);
        if (!isShowAllAds || !isNetworkConnected()) {
            adContainer.setVisibility(View.GONE);
            containerShimmer.setVisibility(View.GONE);
        } else {
            loadBanner(mActivity, id, adContainer, containerShimmer, null, false, BANNER_INLINE_LARGE_STYLE);
        }
    }

    /**
     * Load Quảng Cáo Banner Trong Fragment
     */
    public void loadBannerFragment(final Activity mActivity, String id, final View rootView, final BannerCallBack callback) {
        final FrameLayout adContainer = rootView.findViewById(R.id.banner_container);
        final ShimmerFrameLayout containerShimmer = rootView.findViewById(R.id.shimmer_container_banner);
        if (!isShowAllAds || !isNetworkConnected()) {
            adContainer.setVisibility(View.GONE);
            containerShimmer.setVisibility(View.GONE);
        } else {
            loadBanner(mActivity, id, adContainer, containerShimmer, callback, false, BANNER_INLINE_LARGE_STYLE);
        }
    }

    /**
     * Load Quảng Cáo Banner Trong Fragment set Inline adaptive banners
     */
    public void loadBannerFragment(final Activity mActivity, String id, final View rootView, Boolean useInlineAdaptive) {
        final FrameLayout adContainer = rootView.findViewById(R.id.banner_container);
        final ShimmerFrameLayout containerShimmer = rootView.findViewById(R.id.shimmer_container_banner);
        if (!isShowAllAds || !isNetworkConnected()) {
            adContainer.setVisibility(View.GONE);
            containerShimmer.setVisibility(View.GONE);
        } else {
            loadBanner(mActivity, id, adContainer, containerShimmer, null, useInlineAdaptive, BANNER_INLINE_LARGE_STYLE);
        }
    }

    /**
     * Load Quảng Cáo Banner Trong Fragment set Inline adaptive banners
     */
    public void loadInlineBannerFragment(final Activity activity, String id, final View rootView, String inlineStyle) {
        final FrameLayout adContainer = rootView.findViewById(R.id.banner_container);
        final ShimmerFrameLayout containerShimmer = rootView.findViewById(R.id.shimmer_container_banner);
        if (!isShowAllAds || !isNetworkConnected()) {
            adContainer.setVisibility(View.GONE);
            containerShimmer.setVisibility(View.GONE);
        } else {
            loadBanner(activity, id, adContainer, containerShimmer, null, true, inlineStyle);
        }
    }

    /**
     * Load Quảng Cáo Banner Trong Fragment set Inline adaptive banners
     */
    public void loadBannerFragment(final Activity mActivity, String id, final View rootView, final BannerCallBack callback, Boolean useInlineAdaptive) {
        final FrameLayout adContainer = rootView.findViewById(R.id.banner_container);
        final ShimmerFrameLayout containerShimmer = rootView.findViewById(R.id.shimmer_container_banner);
        if (!isShowAllAds || !isNetworkConnected()) {
            adContainer.setVisibility(View.GONE);
            containerShimmer.setVisibility(View.GONE);
        } else {
            loadBanner(mActivity, id, adContainer, containerShimmer, callback, useInlineAdaptive, BANNER_INLINE_LARGE_STYLE);
        }
    }

    /**
     * Load Quảng Cáo Banner Trong Fragment set Inline adaptive banners
     */
    public void loadInlineBannerFragment(final Activity activity, String id, final View rootView, String inlineStyle, final BannerCallBack callback) {
        final FrameLayout adContainer = rootView.findViewById(R.id.banner_container);
        final ShimmerFrameLayout containerShimmer = rootView.findViewById(R.id.shimmer_container_banner);
        if (!isShowAllAds || !isNetworkConnected()) {
            adContainer.setVisibility(View.GONE);
            containerShimmer.setVisibility(View.GONE);
        } else {
            loadBanner(activity, id, adContainer, containerShimmer, callback, true, inlineStyle);
        }
    }

    /**
     * Load quảng cáo Collapsible Banner Trong Fragment
     */
    public void loadCollapsibleBannerFragment(final Activity mActivity, String id, final View rootView, String gravity) {
        final FrameLayout adContainer = rootView.findViewById(R.id.banner_container);
        final ShimmerFrameLayout containerShimmer = rootView.findViewById(R.id.shimmer_container_banner);
        loadCollapsibleBanner(mActivity, id, gravity, adContainer, containerShimmer);
    }

    public void loadCollapsibleBannerFragmentFloor(final Activity mActivity, List<String> listID, final View rootView, String gravity) {
        final FrameLayout adContainer = rootView.findViewById(R.id.banner_container);
        final ShimmerFrameLayout containerShimmer = rootView.findViewById(R.id.shimmer_container_banner);
        if (!isShowAllAds || !isNetworkConnected()) {
            adContainer.setVisibility(View.GONE);
            containerShimmer.setVisibility(View.GONE);
        } else {
            if (listID == null) {
                adContainer.setVisibility(View.GONE);
                containerShimmer.setVisibility(View.GONE);
                return;
            }
            if (listID.size() < 1) {
                adContainer.setVisibility(View.GONE);
                containerShimmer.setVisibility(View.GONE);
                return;
            }
            List idNew = new ArrayList();
            for (String id : listID) {
                idNew.add(id);
            }
            checkLoadBannerCollap = false;
            loadCollapsibleBannerFloor(mActivity, idNew, gravity, adContainer, containerShimmer);
        }
    }

    private void loadBanner(final Activity mActivity, String id, final FrameLayout adContainer, final ShimmerFrameLayout containerShimmer, final BannerCallBack callback, Boolean useInlineAdaptive, String inlineStyle) {
        containerShimmer.setVisibility(View.VISIBLE);
        containerShimmer.startShimmer();

        try {
            AdView adView = new AdView(mActivity);
            adView.setAdUnitId(id);
            adContainer.addView(adView);
            AdSize adSize = getAdSize(mActivity, useInlineAdaptive, inlineStyle);
            int adHeight;
            if (useInlineAdaptive && inlineStyle.equalsIgnoreCase(BANNER_INLINE_SMALL_STYLE)) {
                adHeight = MAX_SMALL_INLINE_BANNER_HEIGHT;
            } else {
                adHeight = adSize.getHeight();
            }
            containerShimmer.getLayoutParams().height = (int) (adHeight * Resources.getSystem().getDisplayMetrics().density + 0.5f);
            adView.setAdSize(adSize);
            adView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
            adView.setAdListener(new AdListener() {
                @Override
                public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                    containerShimmer.stopShimmer();
                    adContainer.setVisibility(View.GONE);
                    containerShimmer.setVisibility(View.GONE);
                    if (callback != null) {
                        callback.onAdFailedToLoad(loadAdError);
                    }
                }


                @Override
                public void onAdLoaded() {

                    Log.d(TAG, "Banner adapter class name: " + adView.getResponseInfo().getMediationAdapterClassName());
                    containerShimmer.stopShimmer();
                    containerShimmer.setVisibility(View.GONE);
                    adContainer.setVisibility(View.VISIBLE);
                    if (callback != null) {
                        callback.onAdLoadSuccess();
                    }
                    if (adView != null) {
                        adView.setOnPaidEventListener(adValue -> {
                            Log.d(TAG, "OnPaidEvent banner:" + adValue.getValueMicros());
                            FirebaseUtil.logPaidAdImpression(context,
                                    adValue,
                                    adView.getAdUnitId(), AdType.BANNER);
                        });
                    }
                }

                @Override
                public void onAdClicked() {
                    super.onAdClicked();
                    if (callback != null) {
                        callback.onAdClicked();
                    }
                    if (disableAdResumeWhenClickAds)
                        AppOpenManager.getInstance().disableAdResumeByClickAction();
                    FirebaseUtil.logClickAdsEvent(context, id);
                }

                @Override
                public void onAdImpression() {
                    super.onAdImpression();
                    if (callback != null) {
                        callback.onAdImpression();
                    }
                }
            });

            adView.loadAd(getAdRequest());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadBannerFloor(final Activity mActivity, List<String> listID, final FrameLayout adContainer, final ShimmerFrameLayout containerShimmer, final BannerCallBack callback, Boolean useInlineAdaptive, String inlineStyle) {
        if (checkLoadBanner) {
            return;
        }
        if (listID.size() == 0) {
            containerShimmer.stopShimmer();
            adContainer.setVisibility(View.GONE);
            containerShimmer.setVisibility(View.GONE);
            return;
        }
        Log.e("Admob", "load banner ID : " + listID.get(0));


        containerShimmer.setVisibility(View.VISIBLE);
        containerShimmer.startShimmer();
        try {
            AdView adView = new AdView(mActivity);
            adView.setAdUnitId(listID.get(0));
            adContainer.addView(adView);
            AdSize adSize = getAdSize(mActivity, useInlineAdaptive, inlineStyle);
            int adHeight;
            if (useInlineAdaptive && inlineStyle.equalsIgnoreCase(BANNER_INLINE_SMALL_STYLE)) {
                adHeight = MAX_SMALL_INLINE_BANNER_HEIGHT;
            } else {
                adHeight = adSize.getHeight();
            }
            containerShimmer.getLayoutParams().height = (int) (adHeight * Resources.getSystem().getDisplayMetrics().density + 0.5f);
            adView.setAdSize(adSize);
            adView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
            adView.setAdListener(new AdListener() {
                @Override
                public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                    if (callback != null) {
                        callback.onAdFailedToLoad(loadAdError);
                    }

                    if (listID.size() > 0) {
                        listID.remove(0);
                        loadBannerFloor(mActivity, listID, adContainer, containerShimmer, callback, useInlineAdaptive, inlineStyle);
                    } else {
                        containerShimmer.stopShimmer();
                        adContainer.setVisibility(View.GONE);
                        containerShimmer.setVisibility(View.GONE);
                    }
                }


                @Override
                public void onAdLoaded() {
                    checkLoadBanner = true;
                    //lỗi: chưa kiểm tra null
                    if (callback != null)
                        callback.onAdLoadSuccess();
                    Log.d(TAG, "Banner adapter class name: " + adView.getResponseInfo().getMediationAdapterClassName());
                    containerShimmer.stopShimmer();
                    containerShimmer.setVisibility(View.GONE);
                    adContainer.setVisibility(View.VISIBLE);
                    if (adView != null) {
                        adView.setOnPaidEventListener(adValue -> {
                            Log.d(TAG, "OnPaidEvent banner:" + adValue.getValueMicros());

                            FirebaseUtil.logPaidAdImpression(context,
                                    adValue,
                                    adView.getAdUnitId(), AdType.BANNER);
                        });
                    }
                }

                @Override
                public void onAdClicked() {
                    super.onAdClicked();
                    //lỗi: chưa kiểm tra null
                    if (callback != null)
                        callback.onAdClicked();
                    if (disableAdResumeWhenClickAds)
                        AppOpenManager.getInstance().disableAdResumeByClickAction();
                    FirebaseUtil.logClickAdsEvent(context, listID.get(0));
                }

                @Override
                public void onAdImpression() {
                    super.onAdImpression();
                    //lỗi: chưa kiểm tra null
                    if (callback != null)
                        callback.onAdImpression();
                    //end log
                }
            });

            adView.loadAd(getAdRequest());
        } catch (Exception e) {
            e.printStackTrace();
            containerShimmer.stopShimmer();
            adContainer.setVisibility(View.GONE);
            containerShimmer.setVisibility(View.GONE);
        }
    }

    private void loadCollapsibleBanner(final Activity mActivity, String id, String gravity, final FrameLayout adContainer, final ShimmerFrameLayout containerShimmer) {
        if (!isNetworkConnected()) {
            containerShimmer.setVisibility(View.GONE);
            return;
        }

        containerShimmer.setVisibility(View.VISIBLE);
        containerShimmer.startShimmer();
        try {
            AdView adView = new AdView(mActivity);
            adView.setAdUnitId(id);
            adContainer.addView(adView);
            AdSize adSize = getAdSize(mActivity, false, "");
            containerShimmer.getLayoutParams().height = (int) (adSize.getHeight() * Resources.getSystem().getDisplayMetrics().density + 0.5f);
            adView.setAdSize(adSize);
            adView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
            adView.loadAd(getAdRequestForCollapsibleBanner(gravity));
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
                    adView.setOnPaidEventListener(adValue -> {
                        Log.d(TAG, "OnPaidEvent banner:" + adValue.getValueMicros());

                        FirebaseUtil.logPaidAdImpression(context,
                                adValue,
                                adView.getAdUnitId(), AdType.BANNER);
                    });

                }

                @Override
                public void onAdClicked() {
                    super.onAdClicked();
                    if (disableAdResumeWhenClickAds)
                        AppOpenManager.getInstance().disableAdResumeByClickAction();
                    FirebaseUtil.logClickAdsEvent(context, id);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadCollapsibleBannerFloor(final Activity mActivity, List<String> listId, String gravity, final FrameLayout adContainer, final ShimmerFrameLayout containerShimmer) {
        if (checkLoadBannerCollap) {
            return;
        }
        containerShimmer.setVisibility(View.VISIBLE);
        containerShimmer.startShimmer();
        try {
            Log.e("Admob", "load collap banner ID : " + listId.get(0));
            AdView adView = new AdView(mActivity);
            adView.setAdUnitId(listId.get(0));
            adContainer.addView(adView);
            AdSize adSize = getAdSize(mActivity, false, "");
            containerShimmer.getLayoutParams().height = (int) (adSize.getHeight() * Resources.getSystem().getDisplayMetrics().density + 0.5f);
            adView.setAdSize(adSize);
            adView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
            adView.loadAd(getAdRequestForCollapsibleBanner(gravity));
            adView.setAdListener(new AdListener() {
                @Override
                public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                    super.onAdFailedToLoad(loadAdError);
                    Log.e("Admob", "load failed collap banner ID : " + listId.get(0));
                    if (listId.size() > 0) {
                        listId.remove(0);
                        loadCollapsibleBannerFloor(mActivity, listId, gravity, adContainer, containerShimmer);
                    } else {
                        containerShimmer.stopShimmer();
                        adContainer.setVisibility(View.GONE);
                        containerShimmer.setVisibility(View.GONE);
                    }

                }

                @Override
                public void onAdLoaded() {
                    checkLoadBannerCollap = true;
                    Log.d(TAG, "Banner adapter class name: " + adView.getResponseInfo().getMediationAdapterClassName());
                    containerShimmer.stopShimmer();
                    containerShimmer.setVisibility(View.GONE);
                    adContainer.setVisibility(View.VISIBLE);
                    adView.setOnPaidEventListener(adValue -> {
                        Log.d(TAG, "OnPaidEvent banner:" + adValue.getValueMicros());

                        FirebaseUtil.logPaidAdImpression(context,
                                adValue,
                                adView.getAdUnitId(), AdType.BANNER);
                    });

                }

                @Override
                public void onAdClicked() {
                    super.onAdClicked();
                    if (disableAdResumeWhenClickAds)
                        AppOpenManager.getInstance().disableAdResumeByClickAction();
                    FirebaseUtil.logClickAdsEvent(context, listId.get(0));
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadCollapsibleBannerFloor(final Activity mActivity, List<String> listId, String gravity, final FrameLayout adContainer, final ShimmerFrameLayout containerShimmer, BannerCallBack bannerCallBack) {
        if (checkLoadBannerCollap) {
            return;
        }
        containerShimmer.setVisibility(View.VISIBLE);
        containerShimmer.startShimmer();
        try {
            Log.e("Admob", "load collap banner ID : " + listId.get(0));
            AdView adView = new AdView(mActivity);
            adView.setAdUnitId(listId.get(0));
            adContainer.addView(adView);
            AdSize adSize = getAdSize(mActivity, false, "");
            containerShimmer.getLayoutParams().height = (int) (adSize.getHeight() * Resources.getSystem().getDisplayMetrics().density + 0.5f);
            adView.setAdSize(adSize);
            adView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
            adView.loadAd(getAdRequestForCollapsibleBanner(gravity));
            adView.setAdListener(new AdListener() {
                @Override
                public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                    super.onAdFailedToLoad(loadAdError);
                    Log.e("Admob", "load failed collap banner ID : " + listId.get(0));
                    if (listId.size() > 0) {
                        listId.remove(0);
                        loadCollapsibleBannerFloor(mActivity, listId, gravity, adContainer, containerShimmer);
                    } else {
                        bannerCallBack.onAdFailedToLoad(loadAdError);
                        containerShimmer.stopShimmer();
                        adContainer.setVisibility(View.GONE);
                        containerShimmer.setVisibility(View.GONE);
                    }

                }

                @Override
                public void onAdLoaded() {
                    checkLoadBannerCollap = true;
                    bannerCallBack.onAdLoadSuccess();
                    Log.d(TAG, "Banner adapter class name: " + adView.getResponseInfo().getMediationAdapterClassName());
                    containerShimmer.stopShimmer();
                    containerShimmer.setVisibility(View.GONE);
                    adContainer.setVisibility(View.VISIBLE);
                    adView.setOnPaidEventListener(adValue -> {
                        Log.d(TAG, "OnPaidEvent banner:" + adValue.getValueMicros());

                        FirebaseUtil.logPaidAdImpression(context,
                                adValue,
                                adView.getAdUnitId(), AdType.BANNER);
                    });

                }

                @Override
                public void onAdClicked() {
                    super.onAdClicked();
                    bannerCallBack.onAdClicked();
                    if (disableAdResumeWhenClickAds)
                        AppOpenManager.getInstance().disableAdResumeByClickAction();
                    FirebaseUtil.logClickAdsEvent(context, listId.get(0));
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private AdSize getAdSize(Activity mActivity, Boolean useInlineAdaptive, String inlineStyle) {

        // Step 2 - Determine the screen width (less decorations) to use for the ad width.
        Display display = mActivity.getWindowManager().getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics();
        display.getMetrics(outMetrics);

        float widthPixels = outMetrics.widthPixels;
        float density = outMetrics.density;

        int adWidth = (int) (widthPixels / density);

        // Step 3 - Get adaptive ad size and return for setting on the ad view.
        if (useInlineAdaptive) {
            if (inlineStyle.equalsIgnoreCase(BANNER_INLINE_LARGE_STYLE)) {
                return AdSize.getCurrentOrientationInlineAdaptiveBannerAdSize(mActivity, adWidth);
            } else {
                return AdSize.getInlineAdaptiveBannerAdSize(adWidth, MAX_SMALL_INLINE_BANNER_HEIGHT);
            }
        }
        return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(mActivity, adWidth);

    }

    private AdRequest getAdRequestForCollapsibleBanner(String gravity) {
        AdRequest.Builder builder = new AdRequest.Builder();
        Bundle admobExtras = new Bundle();
        admobExtras.putString("collapsible", gravity);
        builder.addNetworkExtrasBundle(AdMobAdapter.class, admobExtras);
        return builder.build();
    }

    /*===========================  end Banner ========================================= */


    public boolean interstitialSplashLoaded() {
        return mInterstitialSplash != null;
    }

    public InterstitialAd getmInterstitialSplash() {
        return mInterstitialSplash;
    }


    /* ==========================  Inter Splash============================================== */


    public RewardedAd getRewardedAd() {
        return this.rewardedAd;
    }


    /**
     * Load ads in Splash
     */
    public void loadSplashInterAds(final Context context, String id, long timeOut, long timeDelay, final InterCallback adListener) {
        isTimeDelay = false;
        isTimeout = false;
        if (!isNetworkConnected()) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (adListener != null) {
                        adListener.onAdClosed();
                        adListener.onNextAction();
                    }
                    return;
                }
            }, 3000);
        } else {
            if (logTimeLoadAdsSplash) {
                currentTime = System.currentTimeMillis();
            }
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
                            adListener.onNextAction();
                            isShowLoadingSplash = false;
                        }
                    }
                };
                handlerTimeout.postDelayed(rdTimeout, timeOut);
            }

            isShowLoadingSplash = true;
            loadInterAds(context, id, new InterCallback() {
                @Override
                public void onAdLoadSuccess(InterstitialAd interstitialAd) {
                    super.onAdLoadSuccess(interstitialAd);
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
                    if (interstitialAd != null) {
                        interstitialAd.setOnPaidEventListener(adValue -> {
                            Log.d(TAG, "OnPaidEvent loadInterstitialAds:" + adValue.getValueMicros());
                            FirebaseUtil.logPaidAdImpression(context,
                                    adValue,
                                    interstitialAd.getAdUnitId(), AdType.BANNER);
                        });
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
                        adListener.onNextAction();
                    }
                }

                @Override
                public void onAdClicked() {
                    if (disableAdResumeWhenClickAds)
                        AppOpenManager.getInstance().disableAdResumeByClickAction();
                    super.onAdClicked();
                    if (timeLimitAds > 1000)
                        setTimeLimitInter();
                }
            });

        }
    }

    public void loadSplashInterAds2(final Context context, String id, long timeDelay, final InterCallback adListener) {
        if (!isNetworkConnected() || !isShowAllAds) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (adListener != null) {
                        adListener.onAdClosed();
                        adListener.onNextAction();
                    }
                    return;
                }
            }, 3000);
        } else {
            mInterstitialSplash = null;
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    InterstitialAd.load(context, id, getAdRequest(),
                            new InterstitialAdLoadCallback() {
                                @Override
                                public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                                    super.onAdLoaded(interstitialAd);
                                    mInterstitialSplash = interstitialAd;
                                    AppOpenManager.getInstance().disableAppResume();
                                    onShowSplash((Activity) context, adListener);
                                }

                                @Override
                                public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                                    super.onAdFailedToLoad(loadAdError);
                                    mInterstitialSplash = null;
                                    adListener.onAdFailedToLoad(loadAdError);
                                    adListener.onNextAction();
                                }

                            });
                }
            }, timeDelay);
        }
    }

    Handler handlerTimeOutSplash = null;
    Runnable runnableTimeOutSplash = null;
    long timeStartSplash = 0;

    public void loadSplashInterAds3(Context context, List<String> idInter, int timeDelay, int timeOut, InterCallback callback, boolean isNextActionWhenFailedInter) {
        if (handlerTimeOutSplash == null) {
            timeStartSplash = System.currentTimeMillis();
            handlerTimeOutSplash = new Handler(Looper.getMainLooper());
            runnableTimeOutSplash = () -> {
                Log.d(TAG, "handlerTimeOutSplash: timeout");
                callback.onAdClosed();
                callback.onNextAction();
                handlerTimeOutSplash = null;
            };
            handlerTimeOutSplash.postDelayed(runnableTimeOutSplash, timeOut);
        }
        if (!isNetworkConnected() || idInter == null || idInter.size() == 0) {
            handlerTimeOutSplash.removeCallbacks(runnableTimeOutSplash);
            handlerTimeOutSplash.removeCallbacksAndMessages(null);
            handlerTimeOutSplash.postDelayed(() -> {
                Log.d(TAG, "handlerTimeOutSplash: size 0");
                callback.onNextAction();
                handlerTimeOutSplash = null;
            }, timeDelay);
        } else {
            Log.d(TAG, "loadSplashInterAds3: " + idInter.get(0));
            InterstitialAd.load(context, idInter.get(0), getAdRequest(),
                    new InterstitialAdLoadCallback() {
                        @Override
                        public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                            super.onAdLoaded(interstitialAd);
                            Log.d(TAG, "loadSplashInterAds3 - onAdLoaded: ");
                            mInterstitialSplash = interstitialAd;
                            handlerTimeOutSplash.removeCallbacks(runnableTimeOutSplash);
                            handlerTimeOutSplash.removeCallbacksAndMessages(null);
                            handlerTimeOutSplash = null;
                            AppOpenManager.getInstance().disableAppResume();
                            onShowSplash((Activity) context, callback);
                        }

                        @Override
                        public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                            super.onAdFailedToLoad(loadAdError);
                            mInterstitialSplash = null;
                            idInter.remove(0);
                            if (idInter.size() == 0) {
                                callback.onAdFailedToLoad(loadAdError);
                                if (!isNextActionWhenFailedInter)
                                    return;
                            }
                            Log.d(TAG, "loadSplashInterAds3 - onAdFailedToLoad: ");
                            if (System.currentTimeMillis() - timeStartSplash < timeOut) {
//                                new Handler().postDelayed(() -> loadSplashInterAds3(context, idInter, timeDelay, timeOut, callback, isNextAction), 5000);
//                            }
                                loadSplashInterAds3(context, idInter, timeDelay, timeOut, callback, isNextActionWhenFailedInter);
                            }
                        }
                    });
        }
    }


    public void onShowSplash(Activity activity, InterstitialAd interSplash, InterCallback adListener) {
        AppOpenManager.getInstance().disableAppResume();
        isShowLoadingSplash = true;
        mInterstitialSplash = interSplash;
        if (!isNetworkConnected()) {
            adListener.onAdClosed();
            return;
        } else {
            if (mInterstitialSplash == null) {
                adListener.onAdClosed();
                adListener.onNextAction();
                return;
            } else {
                mInterstitialSplash.setOnPaidEventListener(adValue -> {
                    Log.d(TAG, "OnPaidEvent splash:" + adValue.getValueMicros());
                    FirebaseUtil.logPaidAdImpression(context,
                            adValue,
                            mInterstitialSplash.getAdUnitId(), AdType.INTERSTITIAL);
                    adListener.onEarnRevenue((double) adValue.getValueMicros());
                });

                if (handlerTimeout != null && rdTimeout != null) {
                    handlerTimeout.removeCallbacks(rdTimeout);
                }

                if (adListener != null) {
                    adListener.onAdLoaded();
                }

                mInterstitialSplash.setFullScreenContentCallback(new FullScreenContentCallback() {
                    @Override
                    public void onAdShowedFullScreenContent() {
                        if (AppOpenManager.getInstance().isInitialized()) {
                            AppOpenManager.getInstance().disableAppResume();
                        }
                        isShowLoadingSplash = true;
                        if (logTimeLoadAdsSplash) {
                            long timeLoad = System.currentTimeMillis() - currentTime;
                            Log.e(TAG, "load ads time :" + timeLoad);
                            FirebaseUtil.logTimeLoadAdsSplash(activity, round1000(timeLoad));
                        }
                    }

                    @Override
                    public void onAdDismissedFullScreenContent() {
                        Log.e(TAG, "DismissedFullScreenContent Splash");
                        if (AppOpenManager.getInstance().isInitialized()) {
                            AppOpenManager.getInstance().enableAppResume();
                        }
                        lastTimeDismissInter = System.currentTimeMillis();
                        if (adListener != null) {
                            if (!openActivityAfterShowInterAds) {
                                adListener.onAdClosed();
                                adListener.onNextAction();
                            } else {
                                adListener.onAdClosedByUser();
                            }

                            if (dialog != null) {
                                dialog.dismiss();
                            }

                        }
                        mInterstitialSplash = null;
                        isShowLoadingSplash = true;
                    }

                    @Override
                    public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                        Log.e(TAG, "onAdFailedToShowFullScreenContent : " + adError);
                        //  mInterstitialSplash = null;
                        if (adError.getCode() == 1) {
                            mInterstitialSplash = null;
                            adListener.onAdClosed();
                        }
                        isShowLoadingSplash = false;
                        if (adListener != null) {
                            adListener.onAdFailedToShow(adError);

                            if (dialog != null) {
                                dialog.dismiss();
                            }
                        }
                    }

                    @Override
                    public void onAdClicked() {
                        super.onAdClicked();
                        if (disableAdResumeWhenClickAds)
                            AppOpenManager.getInstance().disableAdResumeByClickAction();
                        if (timeLimitAds > 1000) {
                            setTimeLimitInter();
                        }
                        FirebaseUtil.logClickAdsEvent(context, mInterstitialSplash.getAdUnitId());
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

                        if (activity != null) {
                            mInterstitialSplash.show(activity);
                            Log.e(TAG, "onShowSplash: mInterstitialSplash.show");
                            isShowLoadingSplash = false;
                        } else if (adListener != null) {
                            if (dialog != null) {
                                dialog.dismiss();
                            }
                            adListener.onAdClosed();
                            adListener.onNextAction();
                            isShowLoadingSplash = false;
                        }
                    }, 300);
                } else {
                    isShowLoadingSplash = false;
                    Log.e(TAG, "onShowSplash: fail on background");
                }
            }

        }

    }

    private void onShowSplash(Activity activity, InterCallback adListener) {
        isShowLoadingSplash = true;
        if (mInterstitialSplash == null) {
            Log.d(TAG, "loadSplashInterAds3: ");
            adListener.onAdClosed();
            adListener.onNextAction();
            return;
        }
        mInterstitialSplash.setOnPaidEventListener(adValue -> {
            Log.d(TAG, "OnPaidEvent splash:" + adValue.getValueMicros());
            FirebaseUtil.logPaidAdImpression(context,
                    adValue,
                    mInterstitialSplash.getAdUnitId(), AdType.INTERSTITIAL);
            adListener.onEarnRevenue((double) adValue.getValueMicros());
        });

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
                if (logTimeLoadAdsSplash) {
                    long timeLoad = System.currentTimeMillis() - currentTime;
                    Log.e(TAG, "load ads time :" + timeLoad);
                    FirebaseUtil.logTimeLoadAdsSplash(activity, round1000(timeLoad));
                }
            }

            @Override
            public void onAdDismissedFullScreenContent() {
                Log.e(TAG, "DismissedFullScreenContent Splash");
                if (AppOpenManager.getInstance().isInitialized()) {
                    AppOpenManager.getInstance().enableAppResume();
                }
                lastTimeDismissInter = System.currentTimeMillis();
                if (adListener != null) {
                    if (!openActivityAfterShowInterAds) {
                        adListener.onAdClosed();
                        adListener.onNextAction();
                    } else {
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
                if (disableAdResumeWhenClickAds)
                    AppOpenManager.getInstance().disableAdResumeByClickAction();
                if (timeLimitAds > 1000) {
                    setTimeLimitInter();
                }
                FirebaseUtil.logClickAdsEvent(context, mInterstitialSplash.getAdUnitId());
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

                if (activity != null) {
                    mInterstitialSplash.show(activity);
                    Log.e(TAG, "onShowSplash: mInterstitialSplash.show");
                    isShowLoadingSplash = false;
                } else if (adListener != null) {
                    if (dialog != null) {
                        dialog.dismiss();
                    }
                    adListener.onAdClosed();
                    adListener.onNextAction();
                    isShowLoadingSplash = false;
                }
            }, 500);
        } else {
            isShowLoadingSplash = false;
            Log.e(TAG, "onShowSplash: fail on background");
        }
    }

    /* =============================End Inter Splash==========================================*/

    /* =============================   Inter ==========================================*/

    /**
     * Load ads inter
     * Return 1 inter ads
     */

    public void loadInterAds(Context context, String id, InterCallback adCallback) {
        Log.d(TAG, "loadInterAds: ");
        if (!isShowAllAds) {
            adCallback.onNextAction();
            adCallback.onAdFailedToLoad(null);
            return;
        }
        adCallback.onAdLoaded();
        if (isShowInter) {
            isTimeout = false;
            interstitialAd = null;
            InterstitialAd.load(context, id, getAdRequest(),
                    new InterstitialAdLoadCallback() {
                        @Override
                        public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                            if (adCallback != null) {
                                adCallback.onAdLoadSuccess(interstitialAd);
                            }
                            //tracking adjust
                            interstitialAd.setOnPaidEventListener(adValue -> {
                                Log.d(TAG, "OnPaidEvent getInterstitalAds:" + adValue.getValueMicros());
                                FirebaseUtil.logPaidAdImpression(context,
                                        adValue,
                                        interstitialAd.getAdUnitId(), AdType.INTERSTITIAL);
                                adCallback.onEarnRevenue((double) adValue.getValueMicros());
                            });
                        }

                        @Override
                        public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                            // Handle the error
                            Log.i(TAG, loadAdError.getMessage());
                            if (adCallback != null) {
                                adCallback.onAdFailedToLoad(loadAdError);
                                adCallback.onNextAction();
                            }
                        }
                    });
        }
    }

    public void loadInterAdsFloor(Context context, List<String> listID, InterCallback adCallback) {
        Log.d(TAG, "loadInterAdsFloor: ");
        if (listID == null) {
            adCallback.onAdFailedToLoad(null);
            adCallback.onNextAction();
            return;
        }
        if (listID.size() < 1) {
            adCallback.onAdFailedToLoad(null);
            adCallback.onNextAction();
            return;
        }
        List<String> listIDNew = new ArrayList<>();
        for (String idNew : listID) {
            listIDNew.add(idNew);
        }
        loadInterAdsFloorByList(context, listIDNew, adCallback);
    }

    private void loadInterAdsFloorByList(Context context, List<String> listID, InterCallback adCallback) {
        Log.d(TAG, "loadInterAdsFloorByList: ");
        if (!isShowAllAds) {
            adCallback.onNextAction();
            adCallback.onAdFailedToLoad(null);
            return;
        }
        if (listID.size() == 0) {
            adCallback.onAdFailedToLoad(null);
            adCallback.onNextAction();
            return;
        }
        Log.e("Admob", "load Inter ID : " + listID.get(0));

        adCallback.onAdLoaded();

        if (isShowInter) {
            isTimeout = false;
            interstitialAd = null;
            InterstitialAd.load(context, listID.get(0), getAdRequest(),
                    new InterstitialAdLoadCallback() {
                        @Override
                        public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                            if (adCallback != null) {
                                adCallback.onAdLoadSuccess(interstitialAd);
                            }
                            //tracking adjust
                            interstitialAd.setOnPaidEventListener(adValue -> {
                                Log.d(TAG, "OnPaidEvent getInterstitalAds:" + adValue.getValueMicros());
                                FirebaseUtil.logPaidAdImpression(context,
                                        adValue,
                                        interstitialAd.getAdUnitId(), AdType.INTERSTITIAL);
                                adCallback.onEarnRevenue((double) adValue.getValueMicros());
                            });
                        }

                        @Override
                        public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                            listID.remove(0);
                            if (listID.size() < 1) {
                                // Log event admob
                                adCallback.onAdFailedToLoad(loadAdError);
                            } else {
                                //end log
                                loadInterAdsFloorByList(context, listID, adCallback);
                            }
                        }

                    });
        }
    }

    /**
     * Show ads inter
     */
    public void showInterAds(Context context, InterstitialAd mInterstitialAd, final InterCallback callback) {
        Log.d(TAG, "time: " + (System.currentTimeMillis() - lastTimeDismissInter) +
                " - stateInter: " + stateInter);
        if (System.currentTimeMillis() - lastTimeDismissInter > timeInterval && stateInter == StateInter.DISMISS) {
            showInterAds(context, mInterstitialAd, callback, false);
        } else {
            callback.onNextAction();
        }
    }

    private void showInterAds(Context context, InterstitialAd mInterstitialAd, final InterCallback callback, boolean shouldReload) {
        currentClicked = numShowAds;
        showInterAdByTimes(context, mInterstitialAd, callback, shouldReload);
    }


    private void showInterAdByTimes(final Context context, InterstitialAd mInterstitialAd, final InterCallback callback, final boolean shouldReloadAds) {
        if (logLogTimeShowAds) {
            currentTimeShowAds = System.currentTimeMillis();
        }
        Helper.setupAdmodData(context);
        if (!isShowAllAds) {
            callback.onAdClosed();
            callback.onNextAction();
            return;
        }
        if (mInterstitialAd == null) {
            if (callback != null) {
                callback.onAdClosed();
                callback.onNextAction();
                callback.onLoadInter();
            }
            return;
        }
        stateInter = StateInter.SHOWING;
        mInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
            @Override
            public void onAdDismissedFullScreenContent() {
                super.onAdDismissedFullScreenContent();
                // Called when fullscreen content is dismissed.
                if (AppOpenManager.getInstance().isInitialized()) {
                    AppOpenManager.getInstance().enableAppResume();
                }
                Log.d(TAG, "onAdDismissedFullScreenContent: stateInter = " + stateInter);
                if (stateInter == StateInter.SHOWED)
                    lastTimeDismissInter = System.currentTimeMillis();
                stateInter = StateInter.DISMISS;
                if (callback != null) {
                    if (!openActivityAfterShowInterAds) {
                        callback.onAdClosed();
                        callback.onNextAction();
                    } else {
                        callback.onAdClosedByUser();
                    }
                    callback.onLoadInter();

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
                stateInter = StateInter.DISMISS;

                // Called when fullscreen content failed to show.
                if (callback != null) {
                    if (!openActivityAfterShowInterAds) {
                        callback.onAdClosed();
                        callback.onNextAction();
                        callback.onLoadInter();
                    }

                    if (dialog != null) {
                        dialog.dismiss();
                    }
                }
            }

            @Override
            public void onAdShowedFullScreenContent() {
                super.onAdShowedFullScreenContent();
                // Called when fullscreen content is shown.
                callback.onAdImpression();
                stateInter = StateInter.SHOWED;
                if (logLogTimeShowAds) {
                    long timeLoad = System.currentTimeMillis() - currentTimeShowAds;
                    Log.e(TAG, "show ads time :" + timeLoad);
                    FirebaseUtil.logTimeLoadShowAdsInter(context, (double) timeLoad / 1000);
                }
            }

            @Override
            public void onAdClicked() {
                super.onAdClicked();
                callback.onAdClicked();
                if (disableAdResumeWhenClickAds)
                    AppOpenManager.getInstance().disableAdResumeByClickAction();
                if (timeLimitAds > 1000)
                    setTimeLimitInter();
                FirebaseUtil.logClickAdsEvent(context, mInterstitialAd.getAdUnitId());
            }
        });

        if (Helper.getNumClickAdsPerDay(context, mInterstitialAd.getAdUnitId()) < maxClickAds) {
            showInterstitialAd(context, mInterstitialAd, callback);
            return;
        }
        if (callback != null) {
            callback.onAdClosed();
            callback.onNextAction();
            callback.onLoadInter();
        }
    }

    private void showInterstitialAd(Context context, InterstitialAd mInterstitialAd, InterCallback callback) {
        if (!isShowInter || !isShowAllAds) {
            callback.onAdClosed();
            callback.onNextAction();
            return;
        }
        if (!isNetworkConnected() || mInterstitialAd == null) {
            callback.onAdClosed();
            callback.onNextAction();
            callback.onLoadInter();
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
                        callback.onLoadInter();
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
                        callback.onLoadInter();
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
            callback.onLoadInter();
        }
    }


    /**
     * load and show ads inter
     */

    public void loadAndShowInter(AppCompatActivity activity, String idInter, int timeDelay, int timeOut, InterCallback callback) {
        if (!isNetworkConnected()) {
            callback.onAdClosed();
            callback.onNextAction();
            return;
        }
        if (!isShowAllAds && !isShowInter) {
            callback.onAdClosed();
            callback.onNextAction();
            return;
        }
        Log.d(TAG, "time: " + (System.currentTimeMillis() - lastTimeDismissInter) + " - stateInter: " + stateInter);
        if (System.currentTimeMillis() - lastTimeDismissInter < timeInterval || stateInter != StateInter.DISMISS) {
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
                if (interstitialAd != null) {
                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        stateInter = StateInter.SHOWING;
                        interstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                            @Override
                            public void onAdDismissedFullScreenContent() {
                                dialog2.dismiss();
                                callback.onAdClosed();
                                callback.onNextAction();
                                callback.onLoadInter();
                                Log.d(TAG, "onAdDismissedFullScreenContent: stateInter = " + stateInter);
                                if (stateInter == StateInter.SHOWED)
                                    lastTimeDismissInter = System.currentTimeMillis();
                                stateInter = StateInter.DISMISS;
                                if (AppOpenManager.getInstance().isInitialized()) {
                                    AppOpenManager.getInstance().enableAppResumeWithActivity(activity.getClass());
                                }
                            }

                            @Override
                            public void onAdFailedToShowFullScreenContent(AdError adError) {
                                dialog2.dismiss();
                                callback.onAdClosed();
                                callback.onNextAction();
                                callback.onLoadInter();
                                stateInter = StateInter.DISMISS;
                                Log.d(TAG, "onAdFailedToShowFullScreenContent: ");
                                if (AppOpenManager.getInstance().isInitialized()) {
                                    AppOpenManager.getInstance().enableAppResumeWithActivity(activity.getClass());
                                }
                            }

                            @Override
                            public void onAdShowedFullScreenContent() {
                                stateInter = StateInter.SHOWED;
                                Log.d("TAG", "The ad was shown.");
                            }

                            @Override
                            public void onAdClicked() {
                                super.onAdClicked();
                                if (disableAdResumeWhenClickAds)
                                    AppOpenManager.getInstance().disableAdResumeByClickAction();
                                if (timeLimitAds > 1000) {
                                    setTimeLimitInter();
                                }
                                FirebaseUtil.logClickAdsEvent(context, mInterstitialSplash.getAdUnitId());
                            }
                        });
                        if (activity.getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.RESUMED) && interstitialAd != null) {
                            interstitialAd.show(activity);
                        } else {
                            if (interstitialAd != null) {
                                if (AppOpenManager.getInstance().isInitialized()) {
                                    AppOpenManager.getInstance().enableAppResumeWithActivity(activity.getClass());
                                    dialog2.dismiss();
                                }
                            }
                            // dialog.dismiss();
                        }
                    }, timeDelay);
                }
            }
        });
    }


    /* ============================= End  Inter  ==========================================*/


    /* =============================  Rewarded Ads ==========================================*/

    public void showRewardAds(final Activity context, final RewardCallback adCallback) {
        if (!isShowAllAds || !isNetworkConnected()) {
            adCallback.onAdClosed();
            return;
        }
        if (rewardedAd == null) {
            initRewardAds(context, rewardedId);
            adCallback.onAdFailedToShow(0);
            return;
        } else {
            Admob.this.rewardedAd.setFullScreenContentCallback(new FullScreenContentCallback() {
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
                    adCallback.onAdImpression();
                }

                public void onAdClicked() {
                    super.onAdClicked();
                    if (disableAdResumeWhenClickAds)
                        AppOpenManager.getInstance().disableAdResumeByClickAction();
                    FirebaseUtil.logClickAdsEvent(context, rewardedAd.getAdUnitId());
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
                Admob.this.rewardedAd = rewardedAd;
                Admob.this.rewardedAd.setOnPaidEventListener(adValue -> {

                    Log.d(TAG, "OnPaidEvent Reward:" + adValue.getValueMicros());
                    FirebaseUtil.logPaidAdImpression(context,
                            adValue,
                            rewardedAd.getAdUnitId(),
                            AdType.REWARDED);
                });
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
        Log.e("Load native id ", id);
        if (!isShowAllAds || !isNetworkConnected()) {
            callback.onAdFailedToLoad();
        } else {
            if (isShowNative) {
                if (isNetworkConnected()) {
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
                                    nativeAd.setOnPaidEventListener(adValue -> {
                                        Log.d(TAG, "OnPaidEvent getInterstitalAds:" + adValue.getValueMicros());
                                        FirebaseUtil.logPaidAdImpression(context,
                                                adValue,
                                                id,
                                                AdType.NATIVE);
                                        callback.onEarnRevenue((double) adValue.getValueMicros());
                                    });
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
                                    Log.e(TAG, "NativeAd onAdClicked: ");
                                    callback.onAdClicked();
                                    if (disableAdResumeWhenClickAds)
                                        AppOpenManager.getInstance().disableAdResumeByClickAction();
                                    FirebaseUtil.logClickAdsEvent(context, id);
                                }
                            })
                            .withNativeAdOptions(adOptions)
                            .build();
                    adLoader.loadAd(getAdRequest());
                } else {
                    callback.onAdFailedToLoad();
                }
            } else {
                callback.onAdFailedToLoad();
            }
        }

    }

    public void loadNativeAd(Context context, String id, FrameLayout frameLayout, int layoutNative) {
        if (!isShowAllAds || !isNetworkConnected()) {
            frameLayout.removeAllViews();
            return;
        }
        if (isShowNative) {
            if (isNetworkConnected()) {
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
                                NativeAdView adView = (NativeAdView) LayoutInflater.from(context).inflate(layoutNative, null);
                                frameLayout.removeAllViews();
                                frameLayout.addView(adView);
                                Admob.getInstance().pushAdsToViewCustom(nativeAd, adView);
                                nativeAd.setOnPaidEventListener(adValue -> {
                                    Log.d(TAG, "OnPaidEvent getInterstitalAds:" + adValue.getValueMicros());
                                    FirebaseUtil.logPaidAdImpression(context,
                                            adValue,
                                            id,
                                            AdType.NATIVE);
                                });
                            }
                        })
                        .withAdListener(new AdListener() {
                            @Override
                            public void onAdFailedToLoad(LoadAdError error) {
                                Log.e(TAG, "NativeAd onAdFailedToLoad: " + error.getMessage());
                                frameLayout.removeAllViews();
                            }

                            @Override
                            public void onAdClicked() {
                                super.onAdClicked();
                                if (disableAdResumeWhenClickAds)
                                    AppOpenManager.getInstance().disableAdResumeByClickAction();
                                FirebaseUtil.logClickAdsEvent(context, id);
                                if (timeLimitAds > 1000) {
                                    setTimeLimitNative();
                                }
                            }
                        })
                        .withNativeAdOptions(adOptions)
                        .build();
                adLoader.loadAd(getAdRequest());
            } else {
                frameLayout.removeAllViews();
            }
        } else {
            frameLayout.removeAllViews();
        }
    }

    /* =============================  Native Ads Floor  ==========================================*/
    public void loadNativeAd(Context context, List<String> listID, final NativeCallback callback) {
        if (listID == null) {
            callback.onAdFailedToLoad();
        } else if (listID.size() == 0) {
            callback.onAdFailedToLoad();
        } else {
            List<String> listIDNew = new ArrayList<>();
            for (String idNew : listID) {
                listIDNew.add(idNew);
            }
            Log.e("xxxx listID", listID.toString() + "");
            Log.e("xxxx listIDNew", listID.toString() + "");
            Log.e(TAG, listIDNew + listID.get(0));

            loadNativeAd(context, listIDNew.get(0), new NativeCallback() {
                @Override
                public void onNativeAdLoaded(NativeAd nativeAd) {
                    super.onNativeAdLoaded(nativeAd);
                    callback.onNativeAdLoaded(nativeAd);
                }

                @Override
                public void onAdClicked() {
                    super.onAdClicked();
                    callback.onAdClicked();
                }

                @Override
                public void onAdFailedToLoad() {
                    super.onAdFailedToLoad();
                    if (listIDNew.size() > 1) {
                        listIDNew.remove(0);
                        loadNativeAd(context, listIDNew, callback);
                    } else {
                        callback.onAdFailedToLoad();
                    }

                }
            });
        }
    }

    private void loadNativeAdFloor(Context context, List<String> listID, final NativeCallback callback) {
        if (listID == null || listID.size() == 0) {
            callback.onAdFailedToLoad();
        } else {
            if (!isShowAllAds || !isNetworkConnected()) {
                callback.onAdFailedToLoad();
                return;
            }
            if (listID.size() > 0) {
                int position = 0;
                Log.e(TAG, "Load Native ID :" + listID.get(position));
                loadNativeAd(context, listID.get(position), callback);
            } else {
                callback.onAdFailedToLoad();
            }
        }
    }

    public void loadNativeAdFloor(Context context, List<String> listID, FrameLayout frameLayout, int layoutNative) {
        if (listID == null || listID.size() == 0) {
            frameLayout.removeAllViews();
        } else {
            if (!isNetworkConnected() || !isShowAllAds) {
                frameLayout.removeAllViews();
                return;
            }
            NativeCallback callback1 = new NativeCallback() {
                @Override
                public void onNativeAdLoaded(NativeAd nativeAd) {
                    super.onNativeAdLoaded(nativeAd);
                    NativeAdView adView = (NativeAdView) LayoutInflater.from(context).inflate(layoutNative, null);
                    frameLayout.removeAllViews();
                    frameLayout.addView(adView);
                    Admob.getInstance().pushAdsToViewCustom(nativeAd, adView);
                    nativeAd.setOnPaidEventListener(adValue -> {
                        Log.d(TAG, "OnPaidEvent getInterstitalAds:" + adValue.getValueMicros());
                        FirebaseUtil.logPaidAdImpression(context,
                                adValue,
                                listID.get(0),
                                AdType.NATIVE);
                    });
                }

                @Override
                public void onAdFailedToLoad() {
                    super.onAdFailedToLoad();
                    if (listID.size() > 0) {
                        listID.remove(0);
                        loadNativeAdFloor(context, listID, frameLayout, layoutNative);
                    }
                }
            };
            if (listID.size() > 0) {
                int position = 0;
                Log.e(TAG, "Load Native ID :" + listID.get(position));
                loadNativeAd(context, listID.get(position), callback1);
            } else {
                frameLayout.removeAllViews();
            }
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
        if (!isNetworkConnected() || !isShowAllAds) {
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
                        nativeAd.setOnPaidEventListener(adValue -> {
                            Log.d(TAG, "OnPaidEvent getInterstitalAds:" + adValue.getValueMicros());
                            FirebaseUtil.logPaidAdImpression(context,
                                    adValue,
                                    id,
                                    AdType.NATIVE);
                        });
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
                        if (disableAdResumeWhenClickAds)
                            AppOpenManager.getInstance().disableAdResumeByClickAction();
                        FirebaseUtil.logClickAdsEvent(context, id);
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
        if (timeOut < 5000) timeOut = 5000;
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
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected();
    }


    private void setTimeLimitInter() {
        if (timeLimitAds > 1000) {
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
        if (timeLimitAds > 1000) {
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
        if (timeLimitAds > 1000) {
            isShowNative = false;
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    isShowNative = true;
                }
            }, timeLimitAds);
        }

    }

    public void onCheckShowSplashWhenFail(final AppCompatActivity activity, final InterCallback callback, int timeDelay) {
        if (isNetworkConnected()) {
            (new Handler(activity.getMainLooper())).postDelayed(new Runnable() {
                public void run() {
                    if (Admob.this.interstitialSplashLoaded() && !Admob.this.isShowLoadingSplash) {
                        Log.i("Admob", "show ad splash when show fail in background");
                        Admob.getInstance().onShowSplash(activity, callback);
                    }

                }
            }, (long) timeDelay);
        }
    }

    public void onCheckShowSplashWhenFailClickButton(final AppCompatActivity activity, InterstitialAd interstitialAd, final InterCallback callback, int timeDelay) {
        if (interstitialAd != null) {
            if (isNetworkConnected()) {
                (new Handler(activity.getMainLooper())).postDelayed(new Runnable() {
                    public void run() {
                        if (Admob.this.interstitialSplashLoaded() && !Admob.this.isShowLoadingSplash) {
                            Log.i("Admob", "show ad splash when show fail in background");
                            Admob.getInstance().onShowSplash(activity, interstitialAd, callback);
                        }

                    }
                }, (long) timeDelay);
            }
        }
    }

    public int round1000(long time) {
        return (int) (Math.round(time / 1000));
    }

    public void setTimeInterval(long timeInterval) {
        this.lastTimeDismissInter = 0L;
        stateInter = StateInter.DISMISS;
        this.timeInterval = timeInterval;
    }
}
