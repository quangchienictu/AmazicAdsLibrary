package com.amazicadslibrary;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import com.amazic.ads.callback.InterCallback;
import com.amazic.ads.util.Admod;
import com.amazic.ads.util.AppIronSource;
import com.google.android.gms.ads.LoadAdError;

public class SplashIS extends AppCompatActivity {
    public String IRON_SOURCE_APP_KEY = "85460dcd";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_is);
        AppIronSource.getInstance().init(this, IRON_SOURCE_APP_KEY, true);
        AppIronSource.getInstance().setTimeLimit(60000);
        AppIronSource.getInstance().setShowAllAds(false);
        Admod.getInstance().setShowAllAds(false);
        loadAndShowInterAds();
    }

    private void loadAndShowInterAds() {
        AppIronSource.getInstance().loadSplashInterstitial(this, new InterCallback(){
            @Override
            public void onAdClosed() {
                super.onAdClosed();
                startActivity(new Intent(SplashIS.this,MainIronSource.class));
            }

            @Override
            public void onAdFailedToLoad(LoadAdError i) {
                super.onAdFailedToLoad(i);
                startActivity(new Intent(SplashIS.this,MainIronSource.class));
            }
        }, 30000);
    }
}