package com.amazicadslibrary;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.amazic.ads.callback.AdCallback;
import com.amazic.ads.callback.ApiCallBack;
import com.amazic.ads.callback.InterCallback;
import com.amazic.ads.iap.BillingCallback;
import com.amazic.ads.iap.IAPManager;
import com.amazic.ads.iap.ProductDetailCustom;
import com.amazic.ads.service.AdmobApi;
import com.amazic.ads.util.Admob;
import com.amazic.ads.util.AdsConsentManager;
import com.amazic.ads.util.AdsSplash;
import com.amazic.ads.util.AppOpenManager;

import java.util.ArrayList;

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
        // Admob
      /*  AppPurchase.getInstance().setBillingListener(new BillingListener() {
            @Override
            public void onInitBillingListener(int code) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Admob.getInstance().loadSplashInterAds(Splash.this,"ca-app-pub-3940256099942544/1033173712",25000,5000, new InterCallback(){
                            @Override
                            public void onAdClosed() {
                                startActivity(new Intent(Splash.this,MainActivity.class));
                                finish();
                            }

                            @Override
                            public void onAdFailedToLoad(LoadAdError i) {
                                super.onAdFailedToLoad(i);
                                startActivity(new Intent(Splash.this,MainActivity.class));
                                finish();
                            }
                        });
                    }
                });
            }
        }, 5000);*/

        adCallback = new AdCallback() {
            @Override
            public void onNextAction() {
                super.onNextAction();
                startActivity(new Intent(Splash.this, MainActivity.class));
                finish();
            }
        };
        interCallback = new InterCallback() {
            @Override
            public void onNextAction() {
                super.onNextAction();
                startActivity(new Intent(Splash.this, MainActivity.class));
                finish();
            }
        };
        AdmobApi.getInstance().setListIDOther("native_home");
//        AppOpenManager.getInstance().init(Splash.this.getApplication(), getString(R.string.ads_test_resume));

        initBilling();

    }

    private void setUpUMP() {
        AdsConsentManager adsConsentManager = new AdsConsentManager(this);
        adsConsentManager.requestUMP(true, "33BE2250B43518CCDA7DE426D04EE231", true, result -> {
            Log.d("TAG1111", "setUpUMP: " + result);
            Log.d("TAG1111", "setUpUMP: " + AdsConsentManager.getConsentResult(this));
        });
    }

    private void loadAndShowSplashAds() {
        Admob.getInstance().initAdmod(this);
        AdmobApi.getInstance().init(this, null, getString(R.string.app_id), new ApiCallBack() {
            @Override
            public void onReady() {
                super.onReady();
//                RemoteConfig.getInstance().onRemoteConfigFetched(Splash.this, () -> {
                Admob.getInstance().setOpenActivityAfterShowInterAds(true);
                AppOpenManager.getInstance().initApi(getApplication());
                AdsSplash adsSplash = AdsSplash.init(true, false, "30_70");
                adsSplash.showAdsSplashApi(Splash.this, adCallback, interCallback);
            }
        });
    }

    private void initBilling() {
        /*AppPurchase.getInstance().setBillingListener(new BillingListener() {
            @Override
            public void onInitBillingFinished(int resultCode) {
                Log.d(TAG, "onInitBillingFinished: " + resultCode);
            }
        }, 5000);*/

        ArrayList<ProductDetailCustom> listProductDetailCustoms = new ArrayList<>();
        listProductDetailCustoms.add(new ProductDetailCustom(IAPManager.typeSub, IAPManager.PRODUCT_ID_TEST));
        IAPManager.getInstance().setPurchaseTest(true);
        IAPManager.getInstance().initBilling(this, listProductDetailCustoms, new BillingCallback() {
            @Override
            public void onBillingSetupFinished(int resultCode) {
                super.onBillingSetupFinished(resultCode);
                runOnUiThread(() -> {
                    Toast.makeText(Splash.this, "IAPManager: " + IAPManager.getInstance().isPurchase(), Toast.LENGTH_SHORT).show();
                    loadAndShowSplashAds();
                });
            }

            @Override
            public void onBillingServiceDisconnected() {
                super.onBillingServiceDisconnected();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        Admob.getInstance().onCheckShowSplashWhenFail(this, interCallback, 1000);
    }
}