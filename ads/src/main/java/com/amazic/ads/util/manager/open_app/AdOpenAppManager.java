package com.amazic.ads.util.manager.open_app;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.amazic.ads.dialog.LoadingAdsDialog;
import com.amazic.ads.util.NetworkUtil;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.appopen.AppOpenAd;

import java.util.ArrayList;
import java.util.List;

public class AdOpenAppManager implements Application.ActivityLifecycleCallbacks {

    enum State {LOADING, LOADED, SHOWING, DISMISS}

    private static final String TAG = "OpenAppManager";
    private static AdOpenAppManager INSTANCE = null;

    public static AdOpenAppManager getInstance() {
        if (INSTANCE == null)
            INSTANCE = new AdOpenAppManager();
        return INSTANCE;
    }

    LoadingAdsDialog dialog;

    OpenAppBuilder builder;
    static AppOpenAd myAppOpenAd;
    List<String> listId = new ArrayList<>();
    State state = State.DISMISS;
    Activity currentActivity = null;

    public void setBuilder(OpenAppBuilder builder) {
        this.builder = builder;
        this.builder.getApplication().registerActivityLifecycleCallbacks(this);
    }

    public void setId(List<String> listId) {
        builder.setId(listId);
    }

    private void loadOpenAdWithList() {
        if (!NetworkUtil.isNetworkActive(currentActivity)) {
            LoadAdError error = new LoadAdError(-1, "network isn't active", "local", null, null);
            builder.getCallback().onAdFailedToLoad(error);
            return;
        }
        if (listId.isEmpty()) {
            LoadAdError error = new LoadAdError(-2, "can't load ad", "local", null, null);
            builder.getCallback().onAdFailedToLoad(error);
            return;
        }
        Log.d(TAG, "loadOpenAdWithList: " + listId.get(0));
        if (myAppOpenAd == null)
            AppOpenAd.load(currentActivity, listId.get(0), builder.getAdNewRequest(), new AppOpenAd.AppOpenAdLoadCallback() {
                @Override
                public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                    super.onAdFailedToLoad(loadAdError);
                    listId.remove(0);
                    if (listId.isEmpty()) {
                        Log.d(TAG, "onAdFailedToLoad: " + loadAdError);
                        builder.getCallback().onAdFailedToLoad(loadAdError);
                        state = State.LOADED;
                    } else {
                        loadOpenAdWithList();
                    }
                }

                @Override
                public void onAdLoaded(@NonNull AppOpenAd appOpenAd) {
                    super.onAdLoaded(appOpenAd);
                    Log.d(TAG, "onAdLoaded: ");
                    builder.getCallback().onAdLoaded(appOpenAd);
                    myAppOpenAd = appOpenAd;
                    state = State.LOADED;
                }
            });
    }

    public void loadAd() {
        if (state != State.LOADING && myAppOpenAd == null) {
            state = State.LOADING;
            listId.clear();
            listId.addAll(builder.getListIdAd());
            loadOpenAdWithList();
        }
    }

    public void showAd(Activity activity, OpenAppCallback callback) {
        if (myAppOpenAd == null || builder.getCallback() == null) {
            callback.onNextAction();
            return;
        }
        if (!NetworkUtil.isNetworkActive(currentActivity)) {
            LoadAdError error = new LoadAdError(-1, "network isn't active", "local", null, null);
            builder.getCallback().onAdFailedToLoad(error);
            callback.onNextAction();
            return;
        }
        showLoading();
        myAppOpenAd.setFullScreenContentCallback(new FullScreenContentCallback() {
            @Override
            public void onAdClicked() {
                super.onAdClicked();
                Log.d(TAG, "onAdClicked: ");
                callback.onAdClicked();
            }

            @Override
            public void onAdDismissedFullScreenContent() {
                super.onAdDismissedFullScreenContent();
                state = State.DISMISS;
                Log.d(TAG, "onAdDismissedFullScreenContent: ");
                dismissLoading();
                callback.onAdClosed();
                callback.onNextAction();
                myAppOpenAd = null;
                loadAd();
            }

            @Override
            public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                super.onAdFailedToShowFullScreenContent(adError);
                state = State.DISMISS;
                Log.d(TAG, "onAdFailedToShowFullScreenContent: ");
                callback.onAdFailedToShow(adError);
                callback.onNextAction();
            }

            @Override
            public void onAdImpression() {
                super.onAdImpression();
                state = State.SHOWING;
                Log.d(TAG, "onAdImpression: ");
                callback.onAdImpression();
            }

            @Override
            public void onAdShowedFullScreenContent() {
                super.onAdShowedFullScreenContent();
                state = State.SHOWING;
                Log.d(TAG, "onAdShowedFullScreenContent: ");
                callback.onAdShowed();
            }
        });
        myAppOpenAd.show(activity);
    }

    public void showLoading() {
        if (dialog != null && !dialog.isShowing())
            dialog.show();
    }

    public void dismissLoading() {
        if (dialog != null && dialog.isShowing())
            dialog.dismiss();
    }

    @Override
    public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {
    }

    @Override
    public void onActivityStarted(@NonNull Activity activity) {
        currentActivity = activity;
        dialog = new LoadingAdsDialog(currentActivity);
        loadAd();
    }

    @Override
    public void onActivityResumed(@NonNull Activity activity) {

    }

    @Override
    public void onActivityPaused(@NonNull Activity activity) {

    }

    @Override
    public void onActivityStopped(@NonNull Activity activity) {

    }

    @Override
    public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {

    }

    @Override
    public void onActivityDestroyed(@NonNull Activity activity) {
        currentActivity = null;
        dialog = null;
        dismissLoading();
    }
}
