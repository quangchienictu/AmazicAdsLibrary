package com.amazicadslibrary;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.amazic.ads.callback.NativeCallback;
import com.amazic.ads.callback.RewardCallback;
import com.amazic.ads.callback.InterCallback;
import com.amazic.ads.util.Admod;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdView;
import com.google.android.gms.ads.rewarded.RewardItem;

public class MainActivity extends AppCompatActivity {
    private InterstitialAd mInterstitialAd;
    private NativeAd nativeAdExit;
    private FrameLayout native_ads;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        native_ads   = findViewById(R.id.native_ads);


        Admod.getInstance().loadBanner(this, getString(R.string.admod_banner_id));
        Admod.getInstance().initRewardAds(this,getString(R.string.admod_app_reward_id));


        loadAdInter();
        loadAdsNative();




        findViewById(R.id.clickFGM).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this,MainActivity2.class));
            }
        });



        findViewById(R.id.btnClickInter).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Admod.getInstance().showInterAds(MainActivity.this, mInterstitialAd, new InterCallback() {
                    @Override
                    public void onAdClosed() {
                        startActivity(new Intent(MainActivity.this,MainActivity3.class));
                        loadAdInter();
                    }

                    @Override
                    public void onAdFailedToLoad(LoadAdError i) {
                        startActivity(new Intent(MainActivity.this,MainActivity3.class));
                        loadAdInter();
                    }

                });
            }
        });


        findViewById(R.id.btnClickReward).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Admod.getInstance().showRewardAds(MainActivity.this,new RewardCallback(){
                    @Override
                    public void onEarnedReward(RewardItem rewardItem) {
                        Toast.makeText(MainActivity.this,"Trả thưởng thành công",Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onAdClosed() {
                        Toast.makeText(MainActivity.this,"Close ads",Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onAdFailedToShow(int codeError) {
                        Toast.makeText(MainActivity.this,"Loa ads err",Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });



    }

    private void loadAdsNative(){
        Admod.getInstance().loadNativeAd(this, getString(R.string.admod_native_id), new NativeCallback() {
            @Override
            public void onNativeAdLoaded(NativeAd nativeAd) {
                NativeAdView adView = ( NativeAdView) LayoutInflater.from(MainActivity.this).inflate(R.layout.layout_native, null);
                native_ads.addView(adView);
                Admod.getInstance().loadNativeAdViewCustom(nativeAd, adView);
            }
        });
    }
    private void loadAdInter() {
        Admod.getInstance().loadInterAds(this, getString(R.string.admod_interstitial_id), new InterCallback() {
            @Override
            public void onInterstitialLoad(InterstitialAd interstitialAd) {
                super.onInterstitialLoad(interstitialAd);
                mInterstitialAd = interstitialAd;
            }
        });
    }
}