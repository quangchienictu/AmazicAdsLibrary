package com.amazic.ads.util;

import static com.amazic.ads.util.AdsSplash.STATE.INTER;
import static com.amazic.ads.util.AdsSplash.STATE.NO_ADS;
import static com.amazic.ads.util.AdsSplash.STATE.OPEN;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.amazic.ads.callback.AdCallback;
import com.amazic.ads.callback.InterCallback;
import com.amazic.ads.event.AdmobEvent;
import com.amazic.ads.service.AdmobApi;

import java.util.Random;

public class AdsSplash {
    private static final String TAG = "AdsSplash";
    private STATE state = NO_ADS;
    AppCompatActivity activity;

    public enum STATE {INTER, OPEN, NO_ADS}

    public static AdsSplash init(AppCompatActivity activity, boolean showInter, boolean showOpen, String rate) {
        AdsSplash adsSplash = new AdsSplash();
        Log.d(TAG, "init: ");
        if (!Admob.isShowAllAds) {
            adsSplash.setState(NO_ADS);
        } else if (showInter && showOpen) {
            adsSplash.checkShowInterOpenSplash(rate);
        } else if (showInter) {
            adsSplash.setState(INTER);
        } else if (showOpen) {
            adsSplash.setState(OPEN);
        } else {
            adsSplash.setState(NO_ADS);
        }
        adsSplash.activity = activity;
        Bundle bundle = new Bundle();
        bundle.putString("ads_splash", showInter + "-" + showOpen + "-" + rate);
        bundle.putString("ads_splash_state", adsSplash.state.toString());
        AdmobEvent.logEvent(activity, "tracking_ads_splash", bundle);
        return adsSplash;
    }

    private void checkShowInterOpenSplash(String rate) {
        int rateInter;
        int rateOpen;
        try {
            rateInter = Integer.parseInt(rate.trim().split("_")[1].trim());
            rateOpen = Integer.parseInt(rate.trim().split("_")[0].trim());
        } catch (Exception e) {
            rateInter = 0;
            rateOpen = 0;
        }
        Log.d(TAG, "rateInter: " + rateInter + " - rateOpen: " + rateOpen);
        if (rateInter >= 0 && rateOpen >= 0 && rateInter + rateOpen == 100) {
            boolean isShowOpenSplash = new Random().nextInt(100) + 1 < rateOpen;
            setState(isShowOpenSplash ? OPEN : INTER);
        } else {
            setState(NO_ADS);
        }
    }


    public void setState(STATE state) {
        this.state = state;
    }

    public STATE getState() {
        return state;
    }

    public void showAdsSplashApi(AdCallback openCallback, InterCallback interCallback) {
        Log.d(TAG, "state show: " + getState());
        if (getState() == OPEN) {
            AdmobApi.getInstance().loadOpenAppAdSplashFloor(activity, openCallback);
        } else if (getState() == INTER) {
            AdmobApi.getInstance().loadInterAdSplashFloor(activity, 3000, 20000, interCallback, true);
        } else {
            interCallback.onNextAction();
        }
    }

    public void onCheckShowSplashWhenFail(AdCallback openCallback, InterCallback interCallback) {
        if (getState() == OPEN)
            AppOpenManager.getInstance().onCheckShowSplashWhenFailNew(activity, openCallback, 1000);
        else if (getState() == INTER)
            Admob.getInstance().onCheckShowSplashWhenFail(activity, interCallback, 1000);
    }

}
