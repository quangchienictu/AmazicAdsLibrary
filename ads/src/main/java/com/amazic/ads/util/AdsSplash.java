package com.amazic.ads.util;

import static com.amazic.ads.util.AdsSplash.STATE.INTER;
import static com.amazic.ads.util.AdsSplash.STATE.NO_ADS;
import static com.amazic.ads.util.AdsSplash.STATE.OPEN;

import android.app.Activity;
import android.os.Handler;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.amazic.ads.callback.AdCallback;
import com.amazic.ads.callback.InterCallback;
import com.amazic.ads.service.AdmobApi;
import com.google.android.gms.common.api.internal.LifecycleActivity;

import java.util.Random;

public class AdsSplash {
    private static final String TAG = "AdsSplash";
    private STATE state = NO_ADS;

    enum STATE {INTER, OPEN, NO_ADS}

    public static AdsSplash init(boolean showInter, boolean showOpen, String rate) {
        AdsSplash adsSplash = new AdsSplash();
        Log.d(TAG, "init: ");
        if (showInter && showOpen) {
            adsSplash.checkShowInterOpenSplash(rate);
        } else if (showInter) {
            adsSplash.setState(INTER);
        } else if (showOpen) {
            adsSplash.setState(OPEN);
        } else {
            adsSplash.setState(NO_ADS);
        }
        return adsSplash;
    }

    private void checkShowInterOpenSplash(String rate) {
        int rateInter;
        int rateOpen;
        try {
            rateInter = Integer.parseInt(rate.trim().split("_")[1].trim());
            rateOpen = Integer.parseInt(rate.trim().split("_")[0].trim());
        } catch (Exception e) {
            Log.d(TAG, "checkShowInterOpenSplash: ");
            rateInter = 0;
            rateOpen = 0;
        }
        Log.d(TAG, "rateInter: " + rateInter + " - rateOpen: " + rateOpen);
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

    public void showAdsSplashApi(AppCompatActivity activity, AdCallback openCallback, InterCallback interCallback) {
        Log.d(TAG, "state show: "+getState() );
        if (getState() == OPEN)
            AdmobApi.getInstance().loadOpenAppAdSplashFloor(activity, openCallback);
        else if (getState() == INTER)
            AdmobApi.getInstance().loadInterAdSplashFloor(activity, 3000, 20000, interCallback, true);
        else {
            interCallback.onNextAction();
        }
    }

    public void onCheckShowSplashWhenFail(AppCompatActivity activity, AdCallback openCallback, InterCallback interCallback) {
        if (getState() == OPEN)
            AppOpenManager.getInstance().onCheckShowSplashWhenFailNew(activity, openCallback, 1000);
        else if (getState() == INTER)
            Admob.getInstance().onCheckShowSplashWhenFail(activity, interCallback, 1000);
    }

}
