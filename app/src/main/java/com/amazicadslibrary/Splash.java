package com.amazicadslibrary;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.amazic.ads.callback.AdCallback;
import com.amazic.ads.callback.ApiCallBack;
import com.amazic.ads.callback.InterCallback;
import com.amazic.ads.service.AdmobApi;
import com.amazic.ads.util.Admob;
import com.amazic.ads.util.AdsConsentManager;
import com.amazic.ads.util.AppOpenManager;

public class Splash extends AppCompatActivity {
    private static final String TAG = "SplashActivity";
    AdCallback adCallback;
    public static String PRODUCT_ID_MONTH = "android.test.purchased";
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
        Admob.getInstance().setOpenActivityAfterShowInterAds(false);
//        AppOpenManager.getInstance().init(Splash.this.getApplication(), getString(R.string.ads_test_resume));
        AdmobApi.getInstance().init(this, null, getString(R.string.app_id), new ApiCallBack() {
            @Override
            public void onReady() {
                super.onReady();
                runOnUiThread(() -> {
                    AppOpenManager.getInstance().init(Splash.this.getApplication(), getString(R.string.ads_test_resume));
                });
                AdmobApi.getInstance().loadInterAdSplashFloor(Splash.this,3000,200000, interCallback,true);
            }
        });


        initBilling();

    }

    private void setUpUMP() {
        AdsConsentManager adsConsentManager = new AdsConsentManager(this);
        adsConsentManager.requestUMP(true, "33BE2250B43518CCDA7DE426D04EE231", true, result -> {
            Log.d("TAG1111", "setUpUMP: " + result);
            Log.d("TAG1111", "setUpUMP: " + AdsConsentManager.getConsentResult(this));
        });
    }

    private void initBilling() {
       /* List<String> listINAPId = new ArrayList<>();
        listINAPId.add(PRODUCT_ID_MONTH);
        List<String> listSubsId = new ArrayList<>();
        AppPurchase.getInstance().initBilling(getApplication(),listINAPId,listSubsId);*/

    }

    @Override
    protected void onResume() {
        super.onResume();
        Admob.getInstance().onCheckShowSplashWhenFail(this, interCallback, 1000);
    }
}