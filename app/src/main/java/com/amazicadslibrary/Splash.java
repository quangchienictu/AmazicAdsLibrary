package com.amazicadslibrary;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.amazic.ads.callback.InterCallback;
import com.amazic.ads.util.Admod;
import com.amazic.ads.util.AppIronSource;
import com.amazic.ads.util.AppOpenManager;
import com.google.android.gms.ads.LoadAdError;

public class Splash extends AppCompatActivity {
    private static final String TAG = "SplashActivity";
    public static String IRON_SOURCE_APP_KEY = "125dc66e5";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        AppIronSource.getInstance().init(this, IRON_SOURCE_APP_KEY, true);
        // Admod
       /* Admod.getInstance().loadSplashInterAds(this,"ca-app-pub-3940256099942544/1033173712",25000,5000, new InterCallback(){
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
        });*/




        //IronSource
        AppIronSource.getInstance().loadSplashInterstitial(Splash.this, new InterCallback() {
            @Override
            public void onAdFailedToLoad(LoadAdError error) {
                startActivity(new Intent(Splash.this,MainActivity.class));
                finish();
            }
            @Override
            public void onAdClosed() {
                startActivity(new Intent(Splash.this,MainActivity.class));
                finish();
                AppOpenManager.getInstance().enableAppResume();
            }
        }, 30000);

    }
}