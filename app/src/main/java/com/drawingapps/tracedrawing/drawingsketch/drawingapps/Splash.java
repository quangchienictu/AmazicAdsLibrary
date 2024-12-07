package com.drawingapps.tracedrawing.drawingsketch.drawingapps;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LifecycleOwnerKt;

import com.amazic.ads.callback.AdCallback;
import com.amazic.ads.callback.ApiCallBack;
import com.amazic.ads.callback.InterCallback;
import com.amazic.ads.iap.BillingCallback;
import com.amazic.ads.iap.IAPManager;
import com.amazic.ads.iap.ProductDetailCustom;
import com.amazic.ads.organic.TechManager;
import com.amazic.ads.service.AdmobApi;
import com.amazic.ads.util.Admob;
import com.amazic.ads.util.AdsConsentManager;
import com.amazic.ads.util.AdsSplash;
import com.amazic.ads.util.AppOpenManager;
import com.amazic.ads.util.AsyncSplash;
import com.ardrawing.tracedrawing.drawingsketch.drawingapps.R;

import java.util.ArrayList;

import kotlin.Unit;
import kotlin.jvm.functions.Function0;

public class Splash extends AppCompatActivity {
    private static final String TAG = "SplashActivity";
    AdCallback adCallback;
    InterCallback interCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        Admob.getInstance().setOpenShowAllAds(true);
        Admob.getInstance().setDisableAdResumeWhenClickAds(true);
        Admob.getInstance().setOpenEventLoadTimeLoadAdsSplash(true);
        Admob.getInstance().setOpenEventLoadTimeShowAdsInter(true);

        adCallback = new AdCallback() {
            @Override
            public void onNextAction() {
                super.onNextAction();
                startActivity(new Intent(Splash.this, MainManagerActivity.class));
                finish();
            }
        };
        interCallback = new InterCallback() {
            @Override
            public void onNextAction() {
                super.onNextAction();
                startActivity(new Intent(Splash.this, MainManagerActivity.class));
                finish();
            }
        };

        AsyncSplash.Companion.getInstance().init(this, adCallback, interCallback, "c193nrau3dhc", "", "", "");
        AsyncSplash.Companion.getInstance().setTimeOutSplash(12000);
        //if app use IAP
        ArrayList<ProductDetailCustom> listIAP = new ArrayList<>();
        listIAP.add(new ProductDetailCustom(IAPManager.PRODUCT_ID_TEST, IAPManager.typeSub));
        AsyncSplash.Companion.getInstance().setUseBilling(listIAP);
        //init resume ads
        AsyncSplash.Companion.getInstance().setInitResumeAdsNormal();
        //use for TechManager
        AsyncSplash.Companion.getInstance().setDebug(true);
        ArrayList<String> listTurnOffRemote = new ArrayList<>();
        listTurnOffRemote.add("banner_splash");
        AsyncSplash.Companion.getInstance().setListTurnOffRemoteKeys(listTurnOffRemote); //set list off remote of TechManager
        //handle async
        AsyncSplash.Companion.getInstance().handleAsync(this, LifecycleOwnerKt.getLifecycleScope(this), new Function0<Unit>() {
            @Override
            public Unit invoke() {
                interCallback.onNextAction();
                return null;
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        AsyncSplash.Companion.getInstance().checkShowSplashWhenFail();
    }
}