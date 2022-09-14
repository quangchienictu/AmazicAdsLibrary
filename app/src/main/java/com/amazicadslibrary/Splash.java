package com.amazicadslibrary;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.amazic.ads.callback.InterCallback;
import com.amazic.ads.util.Admod;
import com.amazic.ads.util.AppIronSource;
import com.amazic.ads.util.AppOpenManager;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.interstitial.InterstitialAd;

public class Splash extends AppCompatActivity {
    private static final String TAG = "SplashActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        Admod.getInstance().timeLimitAds =60000 ;
        // Admod
        Admod.getInstance().loadSplashInterAds(this,"ca-app-pub-3940256099942544/1033173712",25000,5000, new InterCallback(){
            @Override
            public void onAdClosed() {
                startActivity(new Intent(Splash.this,MainActivity.class));
                finish();
            }

            @Override
            public void onAdFailedToLoad(LoadAdError i) {
                super.onAdFailedToLoad(i);
                Log.e(TAG, "onAdFailedToLoad: ");
                startActivity(new Intent(Splash.this,MainActivity.class));
                finish();
            }

            @Override
            public void onAdFailedToShow(AdError adError) {
                super.onAdFailedToShow(adError);
                Log.e(TAG, "onAdFailedToShow: ");
            }

            @Override
            public void onAdShowSuccess() {
                super.onAdShowSuccess();
                Log.e(TAG, "onAdShowSuccess: ");
            }

            @Override
            public void onInterstitialLoad(InterstitialAd interstitialAd) {
                super.onInterstitialLoad(interstitialAd);
                Log.e(TAG, "onInterstitialLoad: ");
            }
        });
    }
}