package com.amazic.ads.service;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.os.Handler;
import android.util.Log;
import android.widget.FrameLayout;

import com.amazic.ads.callback.AdCallback;
import com.amazic.ads.callback.ApiCallBack;
import com.amazic.ads.callback.BannerCallBack;
import com.amazic.ads.callback.InterCallback;
import com.amazic.ads.util.Admob;
import com.amazic.ads.util.AppOpenManager;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class AdmobApi {
    private String TAG = "AdmobApi";
    private ApiService apiService;
    private boolean debug = true;
    private String linkServer = "http://language-master.top";
    private String packageName = "";
    public static String appIDRelease = "ca-app-pub-4973559944609228~2346710863";
    private static volatile AdmobApi INSTANCE;
    private Context context;
    private InterstitialAd interAll = null;

    LinkedHashMap<String, List<String>> listAds = new LinkedHashMap<>();

    public List<String> getListIDOpenSplash() {
        return getListIDByName("open_splash");
    }

    public List<String> getListIDNativeLanguage() {
        return getListIDByName("native_language");
    }

    public List<String> getListIDNativeIntro() {
        return getListIDByName("native_intro");
    }

    public List<String> getListIDNativePermission() {
        return getListIDByName("native_permission");
    }

    public List<String> getListIDNativeAll() {
        return getListIDByName("native_all");
    }

    public List<String> getListIDInterAll() {
        return getListIDByName("inter_all");
    }

    public List<String> getListIDBannerAll() {
        return getListIDByName("banner_all");
    }

    public List<String> getListIDCollapseBannerAll() {
        return getListIDByName("collapse_banner_all");
    }

    public List<String> getListIDInterIntro() {
        return getListIDByName("inter_intro");
    }

    public List<String> getListIDByName(String nameAds) {
        List<String> list = new ArrayList<>();
        if (listAds.get(nameAds) != null)
            list.addAll(Objects.requireNonNull(listAds.get(nameAds)));
        return list;
    }

    private String nameIDOther = "";

    Gson gson = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd HH:mm:ss")
            .create();

    public static synchronized AdmobApi getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new AdmobApi();
        }
        return INSTANCE;
    }


    public void init(Context context, String linkServerRelease, String AppID, ApiCallBack callBack) {
        this.context = context;
        this.packageName = context.getPackageName();
        if (linkServerRelease != null && AppID != null) {
            if (!linkServerRelease.trim().equals("")
                    && (linkServerRelease.contains("http://")
                    || linkServerRelease.contains("https://"))) {
                this.linkServer = linkServerRelease.trim();
                this.appIDRelease = AppID.trim();
            }
        }

        String baseURL = linkServer + "/api/";
        apiService = new Retrofit.Builder()
                .baseUrl(baseURL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()
                .create(ApiService.class);

        Log.i(TAG, "link Server:" + baseURL);
        if (isNetworkConnected()) {
            fetchData(callBack);
        } else {
            new Handler().postDelayed(() -> callBack.onReady(), 2000);
        }

    }


    private void fetchData(ApiCallBack callBack) {
        Log.e(TAG, "fetchData: ");
        try {
            String appID_package = appIDRelease + "+" + packageName;
            Log.i(TAG, "link Server query :" + linkServer + "/api/getidv2/" + appID_package);
            apiService.callAds(appID_package).enqueue(new Callback<List<AdsModel>>() {
                @Override
                public void onResponse(Call<List<AdsModel>> call, Response<List<AdsModel>> response) {
                    if (response.body() == null) {
                        new Handler().postDelayed(() -> callBack.onReady(), 2000);
                        return;
                    }
                    if (response.body().size() == 0) {
                        new Handler().postDelayed(() -> callBack.onReady(), 2000);
                        return;
                    }
                    for (AdsModel ads : response.body()) {
                        List<String> listIDAds = null;
                        if (listAds.containsKey(ads.getName())) {
                            listIDAds = listAds.get(ads.getName());
                        }
                        if (listIDAds == null) {
                            listIDAds = new ArrayList<>();
                        }
                        listIDAds.add(ads.getAds_id());
                        listAds.put(ads.getName(), listIDAds);
                    }
                    callBack.onReady();
                }

                @Override
                public void onFailure(Call<List<AdsModel>> call, Throwable t) {
                    Log.e(TAG, "onFailure: " + t.toString());
                    new Handler().postDelayed(() -> callBack.onReady(), 2000);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            new Handler().postDelayed(() -> callBack.onReady(), 2000);
        }
    }

    public void setListIDOther(String nameIDOther) {
        this.nameIDOther = nameIDOther;
    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected();
    }

    public void loadBanner(final Activity activity) {
        Admob.getInstance().loadBannerFloor(activity, getListIDBannerAll());
    }

    public void loadBanner(final Activity activity, BannerCallBack bannerCallBack) {
        Admob.getInstance().loadBannerFloor(activity, getListIDBannerAll(), bannerCallBack);
    }

    public void loadCollapsibleBanner(final Activity activity) {
        Admob.getInstance().loadCollapsibleBannerFloor(activity, getListIDCollapseBannerAll(), "bottom");
    }

    public void loadCollapsibleBanner(final Activity activity, BannerCallBack bannerCallBack) {
        Admob.getInstance().loadCollapsibleBannerFloor(activity, getListIDCollapseBannerAll(), "bottom", bannerCallBack);
    }

    public void loadInterAll(final Activity activity) {
        if (interAll == null) {
            Log.d(TAG, "loadInterAll: xxxx");
            Admob.getInstance().loadInterAdsFloor(activity, getListIDInterAll(), new InterCallback() {
                @Override
                public void onAdLoadSuccess(InterstitialAd interstitialAd) {
                    super.onAdLoadSuccess(interstitialAd);
                    interAll = interstitialAd;
                }
            });
        }
    }

    public void loadInterAll(final Activity activity, InterCallback interCallback) {
        if (interAll == null) {
            Admob.getInstance().loadInterAdsFloor(activity, getListIDInterAll(), new InterCallback() {
                @Override
                public void onAdLoadSuccess(InterstitialAd interstitialAd) {
                    super.onAdLoadSuccess(interstitialAd);
                    interAll = interstitialAd;
                    interCallback.onAdLoadSuccess(interstitialAd);
                }

                @Override
                public void onAdClicked() {
                    super.onAdClicked();
                    interCallback.onAdClicked();
                }

                @Override
                public void onAdFailedToLoad(LoadAdError i) {
                    super.onAdFailedToLoad(i);
                    interCallback.onAdFailedToLoad(i);
                }

                @Override
                public void onAdLoaded() {
                    super.onAdLoaded();
                    interCallback.onAdLoaded();
                }

                @Override
                public void onAdImpression() {
                    super.onAdImpression();
                    interCallback.onAdImpression();
                }
            });
        }
    }


    public void showInterAll(final Activity activity, InterCallback interCallback) {
        Admob.getInstance().showInterAds(activity, this.interAll, new InterCallback() {
            @Override
            public void onNextAction() {
                super.onNextAction();
                interCallback.onNextAction();
            }

            @Override
            public void onAdClicked() {
                super.onAdClicked();
                interCallback.onAdClicked();
            }

            @Override
            public void onAdClosedByUser() {
                super.onAdClosedByUser();
                interCallback.onAdClosedByUser();
                interAll = null;
                loadInterAll(activity);
            }

            @Override
            public void onAdFailedToLoad(LoadAdError i) {
                super.onAdFailedToLoad(i);
                interCallback.onAdFailedToLoad(i);
                interAll = null;
                loadInterAll(activity);
            }

            @Override
            public void onAdImpression() {
                super.onAdImpression();
                interCallback.onAdImpression();
            }

            @Override
            public void onLoadInter() {
                super.onLoadInter();
                interAll = null;
                loadInterAll(activity);
            }
        });
    }

    public void loadOpenAppAdSplashFloor(final Activity activity, AdCallback adCallback) {
        List listIdNew = new ArrayList();
        for (String id : getListIDOpenSplash()) {
            listIdNew.add(id);
        }
        AppOpenManager.getInstance().loadOpenAppAdSplashFloor(activity, listIdNew, true, adCallback);
    }

    public void loadNativeIntro(final Activity activity, FrameLayout frameLayout, int layoutNative) {
        Admob.getInstance().loadNativeAdFloor(activity, getListIDNativeIntro(), frameLayout, layoutNative);
    }

    public void loadNativeLanguage(final Activity activity, FrameLayout frameLayout, int layoutNative) {
        Admob.getInstance().loadNativeAdFloor(activity, getListIDNativeLanguage(), frameLayout, layoutNative);
    }

    public void loadNativePermission(final Activity activity, FrameLayout frameLayout, int layoutNative) {
        Admob.getInstance().loadNativeAdFloor(activity, getListIDNativePermission(), frameLayout, layoutNative);
    }
}
