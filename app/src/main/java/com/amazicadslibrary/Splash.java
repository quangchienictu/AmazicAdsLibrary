package com.amazicadslibrary;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;

import com.amazic.ads.billing.AppPurchase;
import com.amazic.ads.callback.AdCallback;
import com.amazic.ads.callback.BillingListener;
import com.amazic.ads.callback.InterCallback;
import com.amazic.ads.event.AppsflyerEvent;
import com.amazic.ads.util.Admob;
import com.amazic.ads.util.AppOpenManager;
import com.google.android.gms.ads.LoadAdError;

import java.util.ArrayList;
import java.util.List;

public class Splash extends AppCompatActivity {
    private static final String TAG = "SplashActivity";
    AdCallback adCallback;
    public static String PRODUCT_ID_MONTH = "android.test.purchased";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        String android_id = Settings.Secure.getString(this.getContentResolver(),
                Settings.Secure.ANDROID_ID);
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
        List<String> listID = new ArrayList<>();
        listID.add("ca-app-pub-3940256099942544/3419835294");
        adCallback = new AdCallback(){
            @Override
            public void onNextAction() {
                super.onNextAction();
                startActivity(new Intent(Splash.this,MainActivity.class));
                finish();
            }
        };
        AppOpenManager.getInstance().loadOpenAppAdSplash(this,"ca-app-pub-3940256099942544/3419835294",3000,10000,true,adCallback);

        initBilling();
    }

    private void initBilling() {
        List<String> listINAPId = new ArrayList<>();
        listINAPId.add(PRODUCT_ID_MONTH);
        List<String> listSubsId = new ArrayList<>();
        AppPurchase.getInstance().initBilling(getApplication(),listINAPId,listSubsId);

    }

    @Override
    protected void onResume() {
        super.onResume();
        AppOpenManager.getInstance().onCheckShowSplashWhenFail(this,adCallback,1000);
    }
}