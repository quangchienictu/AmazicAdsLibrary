package com.amazicadslibrary;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import com.amazic.ads.callback.InterCallback;
import com.amazic.ads.util.Admob;
import com.amazic.ads.util.AppIronSource;
import com.google.android.gms.ads.LoadAdError;

public class SplashIS extends AppCompatActivity {
    public String IRON_SOURCE_APP_KEY = "85460dcd";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_is);
        AppIronSource.getInstance().init(this, IRON_SOURCE_APP_KEY, true);
        AppIronSource.getInstance().setOpenActivityAfterShowInterAds(true);
       /* AppIronSource.getInstance().setTimeLimit(60000);
        AppIronSource.getInstance().setShowAllAds(false);*/
        Admob.getInstance().setShowAllAds(false);
        loadAndShowInterAds();
    }

    private void loadAndShowInterAds() {
        AppIronSource.getInstance().loadSplashInterstitial(this, new InterCallback(){
            @Override
            public void onNextAction() {
                super.onNextAction();
                startActivity(new Intent(SplashIS.this,MainIronSource.class));
            }
        }, 30000);
    }
}