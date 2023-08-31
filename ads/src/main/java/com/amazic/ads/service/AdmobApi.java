package com.amazic.ads.service;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.net.ConnectivityManager;
import android.os.Handler;
import android.util.Log;

import androidx.lifecycle.ProcessLifecycleOwner;

import com.amazic.ads.callback.ApiCallBack;
import com.amazic.ads.callback.InterCallback;
import com.amazic.ads.util.Admob;
import com.amazic.ads.util.AppOpenManager;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;

public class AdmobApi {
    private String TAG = "AdmobApi";
    private ApiService apiService;
    private boolean debug = true;
    private String linkServer = "http://language-master.top";
    public static String appIDRelease = "ca-app-pub-4973559944609228~2346710863";
    private static volatile AdmobApi INSTANCE;
    private Context context;


    private List<String> listIDOpenSplash = new ArrayList<>();
    private List<String> listIDNativeLanguage = new ArrayList<>();
    private List<String> listIDNativeIntro = new ArrayList<>();
    private List<String> listIDNativePermission = new ArrayList<>();
    private List<String> listIDNativeAll = new ArrayList<>();
    private List<String> listIDInterAll = new ArrayList<>();
    private List<String> listIDBannerAll = new ArrayList<>();
    private List<String> listIDCollapseBannerAll = new ArrayList<>();
    private List<String> listIDInterIntro = new ArrayList<>();
    private List<String> listIDOther = new ArrayList<>();
    private InterstitialAd interAll = null;

    public List<String> getListIDOpenSplash() {
        return listIDOpenSplash;
    }

    public List<String> getListIDNativeLanguage() {
        return listIDNativeLanguage;
    }

    public List<String> getListIDNativeIntro() {
        return listIDNativeIntro;
    }

    public List<String> getListIDNativePersimmon() {
        return listIDNativePermission;
    }

    public List<String> getListIDNativeAll() {
        return listIDNativeAll;
    }

    public List<String> getListIDInterAll() {
        return listIDInterAll;
    }

    public List<String> getListIDBannerAll() {
        return listIDBannerAll;
    }

    public List<String> getListIDCollapseBannerAll() {
        return listIDCollapseBannerAll;
    }

    public List<String> getListIDInterIntro() {
        return listIDInterIntro;
    }

    public List<String> getListIDOther() {
        return listIDOther;
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
        if (linkServerRelease != null & AppID != null) {
            this.linkServer = linkServerRelease;
            this.appIDRelease = AppID;
        }

        String baseUrl = linkServer + "/api/";
        apiService = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()
                .create(ApiService.class);

        Log.i(TAG, "link Server:" + baseUrl);

        if (isNetworkConnected()) {
            fetchData(callBack);
        } else {
            new Handler().postDelayed(() -> callBack.onReady(), 2000);
        }

    }


    private void fetchData(ApiCallBack callBack) {
        Log.e(TAG, "fetchData: ");
        try {
            apiService.callAds(appIDRelease).enqueue(new Callback<List<AdsModel>>() {
                @Override
                public void onResponse(Call<List<AdsModel>> call, Response<List<AdsModel>> response) {
                    if (response.body() == null) {
                        new Handler().postDelayed(() -> callBack.onReady(), 2000);
                        return;
                    }
                    if (response.body().size() != 0 && response.body() != null) {
                        for (AdsModel ads : response.body()) {
                            if (ads.getName().equals("open_splash")) {
                                listIDOpenSplash.add(ads.getAds_id());
                            } else if (ads.getName().equals("native_language")) {
                                listIDNativeLanguage.add(ads.getAds_id());
                            } else if (ads.getName().equals("inter_all")) {
                                listIDInterAll.add(ads.getAds_id());
                            } else if (ads.getName().equals("native_intro")) {
                                listIDNativeIntro.add(ads.getAds_id());
                            } else if (ads.getName().equals("banner_all")) {
                                listIDBannerAll.add(ads.getAds_id());
                            } else if (ads.getName().equals("native_permission")) {
                                listIDNativePermission.add(ads.getAds_id());
                            } else if (ads.getName().equals("native_all")) {
                                listIDNativeAll.add(ads.getAds_id());
                            } else if (ads.getName().equals("inter_intro")) {
                                listIDInterIntro.add(ads.getAds_id());
                            } else if (ads.getName().equals("collapse_banner")) {
                                listIDCollapseBannerAll.add(ads.getAds_id());
                            } else if (ads.getName().equals(nameIDOther)) {
                                listIDOther.add(ads.getAds_id());
                            }

                        }

                        Log.i(TAG, "listIDOpenSplash: " + listIDOpenSplash.toString());
                        Log.i(TAG, "listIDNativeLanguage: " + listIDNativeLanguage.toString());
                        Log.i(TAG, "listIDInterAll: " + listIDInterAll.toString());
                        Log.i(TAG, "listIDNativeIntro: " + listIDNativeIntro.toString());
                        Log.i(TAG, "listIDBannerAll: " + listIDBannerAll.toString());
                        Log.i(TAG, "listIDNativeAll: " + listIDNativeAll.toString());
                        Log.i(TAG, "listIDNativePersimmon: " + listIDNativePermission.toString());
                        Log.i(TAG, "listIDInterIntro: " + listIDInterIntro.toString());
                        Log.i(TAG, "listIDCollapseBannerAll: " + listIDCollapseBannerAll.toString());
                        Log.i(TAG, "listIDOther: " + listIDOther.toString());
                        callBack.onReady();
                    }
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
    public void loadCollapsibleBanner(final Activity activity) {
        Admob.getInstance().loadCollapsibleBannerFloor(activity, getListIDCollapseBannerAll(), "bottom");
    }
    public void loadInterAll(final Activity activity) {
        if (interAll == null)
            Admob.getInstance().loadInterAdsFloor(activity, getListIDInterAll(), new InterCallback() {
                @Override
                public void onInterstitialLoad(InterstitialAd interstitialAd) {
                    super.onInterstitialLoad(interstitialAd);
                    interAll = interstitialAd;
                }
            });
    }
    public void showInterAll(final Activity activity, InterCallback interCallback) {
        Admob.getInstance().showInterAds(activity, this.interAll, new InterCallback() {
            @Override
            public void onNextAction() {
                super.onNextAction();
                interCallback.onNextAction();
                interAll = null;
                loadInterAll(activity);
            }

            @Override
            public void onAdClosedByUser() {
                super.onAdClosedByUser();
                interCallback.onAdClosedByUser();
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
        });
    }

}
