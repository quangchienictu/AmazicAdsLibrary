package com.amazic.ads.util.manager.open_app;

import android.app.Activity;
import android.util.Log;

import androidx.annotation.NonNull;

import com.amazic.ads.util.NetworkUtil;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.appopen.AppOpenAd;

import java.util.ArrayList;
import java.util.List;

public class AdOpenAppManager {

    enum State {LOADING, LOADED, SHOWING, DISMISS}

    private static final String TAG = "OpenAppManager";

    OpenAppBuilder builder;
    static AppOpenAd myAppOpenAd;
    List<String> listId = new ArrayList<>();
    State state = State.DISMISS;

    public void setBuilder(OpenAppBuilder builder) {
        this.builder = builder;
    }

    public void setId(List<String> listId) {
        builder.setId(listId);
    }

    private void loadOpenAdWithList() {
        if (!NetworkUtil.isNetworkActive(builder.currentActivity)) {
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
            AppOpenAd.load(builder.currentActivity, listId.get(0), builder.getAdNewRequest(), new AppOpenAd.AppOpenAdLoadCallback() {
                @Override
                public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                    super.onAdFailedToLoad(loadAdError);
                    listId.remove(0);
                    if (listId.isEmpty()) {
                        Log.d(TAG, "onAdFailedToLoad: " + loadAdError);
                        builder.getCallback().onAdFailedToLoad(loadAdError);
                        builder.getCallback().onAdLoaded();
                        state = State.LOADED;
                    } else {
                        loadOpenAdWithList();
                    }
                }

                @Override
                public void onAdLoaded(@NonNull AppOpenAd appOpenAd) {
                    super.onAdLoaded(appOpenAd);
                    Log.d(TAG, "onAdLoaded: ");
                    builder.getCallback().onAdLoaded();
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

    public void showAd(Activity activity) {
        if (myAppOpenAd == null || builder.getCallback() == null) {
            builder.getCallback().onNextAction();
            return;
        }
        if (!NetworkUtil.isNetworkActive(builder.currentActivity)) {
            LoadAdError error = new LoadAdError(-1, "network isn't active", "local", null, null);
            builder.getCallback().onAdFailedToLoad(error);
            builder.getCallback().onNextAction();
            return;
        }
        builder.showLoading();
        myAppOpenAd.setFullScreenContentCallback(new FullScreenContentCallback() {
            @Override
            public void onAdClicked() {
                super.onAdClicked();
                Log.d(TAG, "onAdClicked: ");
                builder.getCallback().onAdClicked();
            }

            @Override
            public void onAdDismissedFullScreenContent() {
                super.onAdDismissedFullScreenContent();
                state = State.DISMISS;
                Log.d(TAG, "onAdDismissedFullScreenContent: ");
                builder.dismissLoading();
                builder.getCallback().onAdClosed();
                builder.getCallback().onNextAction();
                myAppOpenAd = null;
            }

            @Override
            public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                super.onAdFailedToShowFullScreenContent(adError);
                state = State.DISMISS;
                Log.d(TAG, "onAdFailedToShowFullScreenContent: ");
                builder.getCallback().onAdFailedToShow(adError);
                builder.getCallback().onNextAction();
            }

            @Override
            public void onAdImpression() {
                super.onAdImpression();
                state = State.SHOWING;
                Log.d(TAG, "onAdImpression: ");
                builder.getCallback().onAdImpression();
            }

            @Override
            public void onAdShowedFullScreenContent() {
                super.onAdShowedFullScreenContent();
                state = State.SHOWING;
                Log.d(TAG, "onAdShowedFullScreenContent: ");
                builder.getCallback().onAdShowed();
            }
        });
        myAppOpenAd.show(activity);
    }
}
