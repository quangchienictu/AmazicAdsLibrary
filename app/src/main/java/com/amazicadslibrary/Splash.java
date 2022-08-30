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
            public void onAdFailedToLoadIs() {
                super.onAdFailedToLoadIs();
                startActivity(new Intent(Splash.this,MainActivity.class));
                finish();
            }
        });
    }
}