package com.amazic.ads.service;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.os.Handler;
import android.util.Log;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;

import com.amazic.ads.callback.AdCallback;
import com.amazic.ads.callback.ApiCallBack;
import com.amazic.ads.callback.BannerCallBack;
import com.amazic.ads.callback.InterCallback;
import com.amazic.ads.util.Admob;
import com.amazic.ads.util.AppOpenManager;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONArray;
import org.json.JSONObject;

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
    private String jsonIdAdsDefault = "[{\"id\":14,\"package_name\":null,\"app name\":\"Api test\",\"app_id\":\"ca-app-pub-4973559944609228~2346710863\",\"name\":\"inter_splash\",\"ads_id\":\"ca-app-pub-3940256099942544\\/3419835294\"},{\"id\":15,\"package_name\":null,\"app name\":\"Api test\",\"app_id\":\"ca-app-pub-4973559944609228~2346710863\",\"name\":\"policy_inter_splash\",\"ads_id\":\"ca-app-pub-3940256099942544\\/3419835294\"},{\"id\":16,\"package_name\":null,\"app name\":\"Api test\",\"app_id\":\"ca-app-pub-4973559944609228~2346710863\",\"name\":\"policy_inter_theme\",\"ads_id\":\"ca-app-pub-3940256099942544\\/1033173712\"},{\"id\":17,\"package_name\":null,\"app name\":\"Api test\",\"app_id\":\"ca-app-pub-4973559944609228~2346710863\",\"name\":\"banner_all\",\"ads_id\":\"ca-app-pub-3940256099942544\\/6300978111\"},{\"id\":18,\"package_name\":null,\"app name\":\"Api test\",\"app_id\":\"ca-app-pub-4973559944609228~2346710863\",\"name\":\"open_splash\",\"ads_id\":\"ca-app-pub-3940256099942544\\/9257395921\"},{\"id\":19,\"package_name\":null,\"app name\":\"Api test\",\"app_id\":\"ca-app-pub-4973559944609228~2346710863\",\"name\":\"inter_all\",\"ads_id\":\"ca-app-pub-3940256099942544\\/1033173712\"},{\"id\":20,\"package_name\":null,\"app name\":\"Api test\",\"app_id\":\"ca-app-pub-4973559944609228~2346710863\",\"name\":\"policy_open_splash\",\"ads_id\":\"ca-app-pub-3940256099942544\\/3419835294\"},{\"id\":21,\"package_name\":null,\"app name\":\"Api test\",\"app_id\":\"ca-app-pub-4973559944609228~2346710863\",\"name\":\"inter_intro\",\"ads_id\":\"ca-app-pub-3940256099942544\\/1033173712\"},{\"id\":91,\"package_name\":null,\"app name\":\"Api test\",\"app_id\":\"ca-app-pub-4973559944609228~2346710863\",\"name\":\"collapse_banner\",\"ads_id\":\"ca-app-pub-3940256099942544\\/2014213617\"},{\"id\":2326,\"package_name\":null,\"app name\":\"Api test\",\"app_id\":\"ca-app-pub-4973559944609228~2346710863\",\"name\":\"native_preview\",\"ads_id\":\"ca-app-pub-3940256099942544\\/2247696110\"},{\"id\":2327,\"package_name\":null,\"app name\":\"Api test\",\"app_id\":\"ca-app-pub-4973559944609228~2346710863\",\"name\":\"native_theme\",\"ads_id\":\"ca-app-pub-3940256099942544\\/2247696110\"},{\"id\":2425,\"package_name\":null,\"app name\":\"Api test\",\"app_id\":\"ca-app-pub-4973559944609228~2346710863\",\"name\":\"native_emi\",\"ads_id\":\"ca-app-pub-3940256099942544\\/2247696110\"},{\"id\":2426,\"package_name\":null,\"app name\":\"Api test\",\"app_id\":\"ca-app-pub-4973559944609228~2346710863\",\"name\":\"native_result\",\"ads_id\":\"ca-app-pub-3940256099942544\\/2247696110\"},{\"id\":2427,\"package_name\":null,\"app name\":\"Api test\",\"app_id\":\"ca-app-pub-4973559944609228~2346710863\",\"name\":\"native_welcome\",\"ads_id\":\"ca-app-pub-3940256099942544\\/2247696110\"},{\"id\":2428,\"package_name\":null,\"app name\":\"Api test\",\"app_id\":\"ca-app-pub-4973559944609228~2346710863\",\"name\":\"native_success\",\"ads_id\":\"ca-app-pub-3940256099942544\\/2247696110\"},{\"id\":2435,\"package_name\":null,\"app name\":\"Api test\",\"app_id\":\"ca-app-pub-4973559944609228~2346710863\",\"name\":\"rewarded_animation\",\"ads_id\":\"ca-app-pub-3940256099942544\\/5224354917\"},{\"id\":2436,\"package_name\":null,\"app name\":\"Api test\",\"app_id\":\"ca-app-pub-4973559944609228~2346710863\",\"name\":\"native_preview\",\"ads_id\":\"ca-app-pub-3940256099942544\\/2247696110\"},{\"id\":2437,\"package_name\":null,\"app name\":\"Api test\",\"app_id\":\"ca-app-pub-4973559944609228~2346710863\",\"name\":\"native_apply\",\"ads_id\":\"ca-app-pub-3940256099942544\\/2247696110\"},{\"id\":2438,\"package_name\":null,\"app name\":\"Api test\",\"app_id\":\"ca-app-pub-4973559944609228~2346710863\",\"name\":\"native_ringtone\",\"ads_id\":\"ca-app-pub-3940256099942544\\/2247696110\"},{\"id\":2439,\"package_name\":null,\"app name\":\"Api test\",\"app_id\":\"ca-app-pub-4973559944609228~2346710863\",\"name\":\"native_gallery\",\"ads_id\":\"ca-app-pub-3940256099942544\\/2247696110\"},{\"id\":2440,\"package_name\":null,\"app name\":\"Api test\",\"app_id\":\"ca-app-pub-4973559944609228~2346710863\",\"name\":\"inter_info\",\"ads_id\":\"ca-app-pub-3940256099942544\\/1033173712\"},{\"id\":2441,\"package_name\":null,\"app name\":\"Api test\",\"app_id\":\"ca-app-pub-4973559944609228~2346710863\",\"name\":\"native_home\",\"ads_id\":\"11\"},{\"id\":2442,\"package_name\":null,\"app name\":\"Api test\",\"app_id\":\"ca-app-pub-4973559944609228~2346710863\",\"name\":\"native_home\",\"ads_id\":\"ca-app-pub-3940256099942544\\/2247696110\"},{\"id\":2443,\"package_name\":null,\"app name\":\"Api test\",\"app_id\":\"ca-app-pub-4973559944609228~2346710863\",\"name\":\"native_welcome\",\"ads_id\":\"ca-app-pub-3940256099942544\\/2247696110\"},{\"id\":2448,\"package_name\":null,\"app name\":\"Api test\",\"app_id\":\"ca-app-pub-4973559944609228~2346710863\",\"name\":\"native_guide\",\"ads_id\":\"ca-app-pub-3940256099942544\\/2247696110\"},{\"id\":2449,\"package_name\":null,\"app name\":\"Api test\",\"app_id\":\"ca-app-pub-4973559944609228~2346710863\",\"name\":\"native_configuration\",\"ads_id\":\"ca-app-pub-3940256099942544\\/2247696110\"},{\"id\":2450,\"package_name\":null,\"app name\":\"Api test\",\"app_id\":\"ca-app-pub-4973559944609228~2346710863\",\"name\":\"native_merge_audio\",\"ads_id\":\"ca-app-pub-3940256099942544\\/2247696110\"},{\"id\":2451,\"package_name\":null,\"app name\":\"Api test\",\"app_id\":\"ca-app-pub-4973559944609228~2346710863\",\"name\":\"native_merge_video\",\"ads_id\":\"ca-app-pub-3940256099942544\\/2247696110\"},{\"id\":2452,\"package_name\":null,\"app name\":\"Api test\",\"app_id\":\"ca-app-pub-4973559944609228~2346710863\",\"name\":\"native_cutter\",\"ads_id\":\"ca-app-pub-3940256099942544\\/2247696110\"},{\"id\":2453,\"package_name\":null,\"app name\":\"Api test\",\"app_id\":\"ca-app-pub-4973559944609228~2346710863\",\"name\":\"native_process\",\"ads_id\":\"ca-app-pub-3940256099942544\\/2247696110\"},{\"id\":2454,\"package_name\":null,\"app name\":\"Api test\",\"app_id\":\"ca-app-pub-4973559944609228~2346710863\",\"name\":\"inter_splash\",\"ads_id\":\"ca-app-pub-3940256099942544\\/1033173712\"},{\"id\":2455,\"package_name\":null,\"app name\":\"Api test\",\"app_id\":\"ca-app-pub-4973559944609228~2346710863\",\"name\":\"inter_choose\",\"ads_id\":\"ca-app-pub-3940256099942544\\/1033173712\"},{\"id\":2456,\"package_name\":null,\"app name\":\"Api test\",\"app_id\":\"ca-app-pub-4973559944609228~2346710863\",\"name\":\"native_item\",\"ads_id\":\"ca-app-pub-3940256099942544\\/2247696110\"},{\"id\":2465,\"package_name\":null,\"app name\":\"Api test\",\"app_id\":\"ca-app-pub-4973559944609228~2346710863\",\"name\":\"native_detail\",\"ads_id\":\"ca-app-pub-3940256099942544\\/2247696110\"},{\"id\":2466,\"package_name\":null,\"app name\":\"Api test\",\"app_id\":\"ca-app-pub-4973559944609228~2346710863\",\"name\":\"native_file\",\"ads_id\":\"ca-app-pub-3940256099942544\\/2247696110\"},{\"id\":2469,\"package_name\":null,\"app name\":\"Api test\",\"app_id\":\"ca-app-pub-4973559944609228~2346710863\",\"name\":\"native_intro\",\"ads_id\":\"ca-app-pub-3940256099942544\\/2247696110\"},{\"id\":2470,\"package_name\":null,\"app name\":\"Api test\",\"app_id\":\"ca-app-pub-4973559944609228~2346710863\",\"name\":\"native_language\",\"ads_id\":\"ca-app-pub-3940256099942544\\/2247696110\"},{\"id\":2471,\"package_name\":null,\"app name\":\"Api test\",\"app_id\":\"ca-app-pub-4973559944609228~2346710863\",\"name\":\"inter_guide\",\"ads_id\":\"ca-app-pub-3940256099942544\\/1033173712\"},{\"id\":2472,\"package_name\":null,\"app name\":\"Api test\",\"app_id\":\"ca-app-pub-4973559944609228~2346710863\",\"name\":\"native_per\",\"ads_id\":\"ca-app-pub-3940256099942544\\/2247696110\"},{\"id\":2473,\"package_name\":null,\"app name\":\"Api test\",\"app_id\":\"ca-app-pub-4973559944609228~2346710863\",\"name\":\"appopen_resume\",\"ads_id\":\"ca-app-pub-3940256099942544\\/9257395921\"},{\"id\":2474,\"package_name\":null,\"app name\":\"Api test\",\"app_id\":\"ca-app-pub-4973559944609228~2346710863\",\"name\":\"native_stop_watch\",\"ads_id\":\"ca-app-pub-3940256099942544\\/2247696110\"},{\"id\":2475,\"package_name\":null,\"app name\":\"Api test\",\"app_id\":\"ca-app-pub-4973559944609228~2346710863\",\"name\":\"native_timer\",\"ads_id\":\"ca-app-pub-3940256099942544\\/2247696110\"},{\"id\":2476,\"package_name\":null,\"app name\":\"Api test\",\"app_id\":\"ca-app-pub-4973559944609228~2346710863\",\"name\":\"native_history\",\"ads_id\":\"ca-app-pub-3940256099942544\\/2247696110\"},{\"id\":2477,\"package_name\":null,\"app name\":\"Api test\",\"app_id\":\"ca-app-pub-4973559944609228~2346710863\",\"name\":\"inter_welcome_back\",\"ads_id\":\"ca-app-pub-3940256099942544\\/1033173712\"},{\"id\":2478,\"package_name\":null,\"app name\":\"Api test\",\"app_id\":\"ca-app-pub-4973559944609228~2346710863\",\"name\":\"native_crop\",\"ads_id\":\"ca-app-pub-3940256099942544\\/2247696110\"},{\"id\":2479,\"package_name\":null,\"app name\":\"Api test\",\"app_id\":\"ca-app-pub-4973559944609228~2346710863\",\"name\":\"banner\",\"ads_id\":\"ca-app-pub-3940256099942544\\/6300978111\"}]";
    private boolean isSetId = false;
    private int timeOutCallApi = 12000;

    public int getListAdsSize() {
        if (listAds != null) {
            return listAds.size();
        } else {
            return 0;
        }
    }

    public String getJsonIdAdsDefault() {
        return jsonIdAdsDefault;
    }

    public void setJsonIdAdsDefault(String jsonIdAdsDefault) {
        this.jsonIdAdsDefault = jsonIdAdsDefault;
    }

    public int getTimeOutCallApi() {
        return timeOutCallApi;
    }

    public void setTimeOutCallApi(int timeOutCallApi) {
        this.timeOutCallApi = timeOutCallApi;
    }

    LinkedHashMap<String, List<String>> listAds = new LinkedHashMap<>();
    public List<AdsModel> allId = new ArrayList<>();

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

    public List<String> getListIDInterSplash() {
        return getListIDByName("inter_splash");
    }

    public List<String> getListIDInterAll() {
        return getListIDByName("inter_all");
    }

    public List<String> getListIDBannerAll() {
        return getListIDByName("banner_all");
    }

    public List<String> getListIDCollapseBannerAll() {
        return getListIDByName("collapse_banner");
    }

    public List<String> getListIDInterIntro() {
        return getListIDByName("inter_intro");
    }

    public List<String> getListIDAppOpenResume() {
        return getListIDByName("open_resume");
    }

    public List<String> getListIDByName(String nameAds) {
        List<String> list = new ArrayList<>();
        if (listAds.get(nameAds.toLowerCase().trim()) != null)
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
        listAds.clear();
        isSetId = false;
        this.packageName = context.getPackageName();
        if (linkServerRelease != null && AppID != null) {
            if (!linkServerRelease.trim().equals("")
                    && (linkServerRelease.contains("http://")
                    || linkServerRelease.contains("https://"))) {
                this.linkServer = linkServerRelease.trim();
                if (linkServer.endsWith("/")) {
                    linkServer = linkServer.substring(0, linkServer.length() - 1);
                }
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
            //after 12s, if cannot call api -> set list id default
            new Handler().postDelayed(() -> {
                if (!isSetId) { //if not set id from api -> set list id default
                    convertJsonIdAdsDefaultToList(jsonIdAdsDefault);
                    isSetId = true;
                    Log.d(TAG, "isSetId = true1");
                    callBack.onReady();
                } else {
                    Log.d(TAG, "xxxxxx1");
                }
            }, timeOutCallApi);
        } else {
            callBack.onReady();
        }

    }

    private void convertJsonIdAdsDefaultToList(String jsonIdAds) {
        try {
            ArrayList<AdsModel> listAdsModel = new ArrayList<>();
            JSONArray jsonArray = new JSONArray(jsonIdAds);

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);

                int id = jsonObject.getInt("id");
                String app_id = jsonObject.getString("app_id");
                String name = jsonObject.getString("name");
                String ads_id = jsonObject.getString("ads_id");

                AdsModel adsModel = new AdsModel(id, app_id, name, ads_id);
                listAdsModel.add(adsModel);

                for (AdsModel ads : listAdsModel) {
                    List<String> listIDAds = null;
                    if (listAds.containsKey(ads.getName())) {
                        listIDAds = listAds.get(ads.getName());
                    }
                    if (listIDAds == null) {
                        listIDAds = new ArrayList<>();
                    }
                    listIDAds.add(ads.getAds_id());
                    listAds.put(ads.getName().toLowerCase().trim(), listIDAds);
                }
            }
            Log.d(TAG, "convertJsonIdAdsDefaultToList: " + listAds.size());
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "convertJsonIdAdsDefaultToList: Exception: Invalid json");
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
                    Log.d(TAG, "onResponse: isSetId: " + isSetId);
                    if (!isSetId) {
                        if (response.body() == null || response.body().isEmpty()) {
                            callBack.onReady();
                            return;
                        }
                        Log.d(TAG, "onResponse: " + listAds.size());
                        pushIDAd(response.body());
                        isSetId = true;
                        Log.d(TAG, "isSetId = true2, list size = " + listAds.size());
                        callBack.onReady();
                    } else {
                        Log.d(TAG, "xxxxxx2");
                    }
                }

                @Override
                public void onFailure(Call<List<AdsModel>> call, Throwable t) {
                    Log.e(TAG, "onFailure: " + t);
                    Log.d(TAG, "onFailure: isSetId: " + isSetId);
                    if (!isSetId) {
                        convertJsonIdAdsDefaultToList(jsonIdAdsDefault);
                        isSetId = true;
                        Log.d(TAG, "isSetId = true3");
                        callBack.onReady();
                    } else {
                        Log.d(TAG, "xxxxxx3");
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "fetchData: Exception: isSetId: " + isSetId);
            if (!isSetId) {
                convertJsonIdAdsDefaultToList(jsonIdAdsDefault);
                isSetId = true;
                Log.d(TAG, "isSetId = true4");
                callBack.onReady();
            } else {
                Log.d(TAG, "xxxxxx4");
            }
        }
    }

    public void pushIDAd(List<AdsModel> listId) {
        allId.clear();
        allId.addAll(listId);
        for (AdsModel ads : listId) {
            List<String> listIDAds = null;
            if (listAds.containsKey(ads.getName())) {
                listIDAds = listAds.get(ads.getName());
            }
            if (listIDAds == null) {
                listIDAds = new ArrayList<>();
            }
            listIDAds.add(ads.getAds_id());
            listAds.put(ads.getName().toLowerCase().trim(), listIDAds);
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

    public void loadBanner(Context context, FrameLayout frContainer, int adWidth) {
        Admob.getInstance().loadBannerFloor(context, adWidth, frContainer, getListIDBannerAll());
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

    public AdView loadCollapsibleBannerFloorWithReload(final Activity activity, BannerCallBack bannerCallBack) {
        return Admob.getInstance().loadCollapsibleBannerFloorWithReload(activity, getListIDCollapseBannerAll(), "bottom", bannerCallBack);
    }

    public void loadInterAll(final Activity activity) {
        if (interAll == null) {
            Admob.getInstance().loadInterAdsFloor(activity, getListIDInterAll(), new InterCallback() {
                @Override
                public void onAdLoadSuccess(InterstitialAd interstitialAd) {
                    super.onAdLoadSuccess(interstitialAd);
                    interAll = interstitialAd;
                }
            });
        }
    }

    public void loadInterAll(final Activity activity, @NonNull InterCallback interCallback) {
        Admob.getInstance().loadInterAdsFloor(activity, getListIDInterAll(), new InterCallback() {
            @Override
            public void onAdLoadSuccess(InterstitialAd interstitialAd) {
                super.onAdLoadSuccess(interstitialAd);
                interAll = interstitialAd;
                interCallback.onAdLoadSuccess(interstitialAd);
            }

            @Override
            public void onAdFailedToLoad(LoadAdError i) {
                super.onAdFailedToLoad(i);
                interCallback.onAdFailedToLoad(i);
            }
        });
    }


    public void showInterAll(final Activity activity, @NonNull InterCallback interCallback) {
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
            public void onAdFailedToLoad(LoadAdError i) {
                super.onAdFailedToLoad(i);
                interCallback.onAdFailedToLoad(i);
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

            @Override
            public void onInterDismiss() {
                super.onInterDismiss();
                interCallback.onInterDismiss();
            }
        });
    }

    public void loadOpenAppAdSplashFloor(final Activity activity, AdCallback adCallback) {
        AppOpenManager.getInstance().loadOpenAppAdSplashFloor(activity, getListIDOpenSplash(), true, adCallback);
    }

    public void loadInterAdSplashFloor(final Activity activity, int timeDelay, int timeOut, InterCallback callback, boolean isNextActionWhenFailedInter) {
        Admob.getInstance().loadSplashInterAds3(activity, getListIDInterSplash(), timeDelay, timeOut, callback, isNextActionWhenFailedInter);
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
