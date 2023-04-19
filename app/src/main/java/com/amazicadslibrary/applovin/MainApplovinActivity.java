package com.amazicadslibrary.applovin;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.amazic.ads.applovin.AppLovin;
import com.amazic.ads.applovin.AppLovinCallback;
import com.amazic.ads.callback.InterCallback;
import com.amazic.ads.callback.NativeCallback;
import com.amazic.ads.util.Admob;
import com.amazicadslibrary.R;
import com.applovin.mediation.MaxError;
import com.applovin.mediation.MaxReward;
import com.applovin.mediation.ads.MaxInterstitialAd;
import com.applovin.mediation.ads.MaxRewardedAd;
import com.applovin.mediation.nativeAds.MaxNativeAdView;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdView;

public class MainApplovinActivity extends AppCompatActivity {

    private FrameLayout frAds;
    private ShimmerFrameLayout shimmerFrameLayout;
    private MaxInterstitialAd interstitialAd;
    private Button btnLoadReward;
    private MaxRewardedAd maxRewardedAd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_applovin);
        frAds = findViewById(R.id.fr_ads);
        shimmerFrameLayout = findViewById(R.id.shimmer_container_native);
        interstitialAd = AppLovin.getInstance().getInterstitialAds(this, getString(R.string.applovin_test_inter));
        AppLovin.getInstance().loadBanner(this, getString(R.string.applovin_test_banner));

        //load reward ad
        btnLoadReward = findViewById(R.id.btnLoadReward);
        btnLoadReward.setOnClickListener(view -> {
            if (maxRewardedAd != null && maxRewardedAd.isReady()) {
                AppLovin.getInstance().showRewardAd(this, maxRewardedAd,new AppLovinCallback(){
                    @Override
                    public void onAdClosed() {
                        super.onAdClosed();
                        startActivity(new Intent(MainApplovinActivity.this, MainActivityApplovin2.class));
                    }

                    @Override
                    public void onUserRewarded(MaxReward reward) {
                        super.onUserRewarded(reward);
                    }
                });
            } else {
                maxRewardedAd = AppLovin.getInstance().getRewardAd(this, getString(R.string.applovin_test_reward), new AppLovinCallback() {
                    @Override
                    public void onAdLoaded() {
                        Toast.makeText(MainApplovinActivity.this, "reward loaded", Toast.LENGTH_SHORT).show();
                        btnLoadReward.setText("Show Reward");
                    }

                    @Override
                    public void onAdClosed() {
                        startActivity(new Intent(MainApplovinActivity.this, MainActivityApplovin2.class));
                    }
                });
            }
        });
        //load interstitial
        findViewById(R.id.btnLoadInter).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (interstitialAd.isReady()) {
                    AppLovin.getInstance().forceShowInterstitial(MainApplovinActivity.this, interstitialAd, new InterCallback() {
                        @Override
                        public void onAdClosed() {
                            super.onAdClosed();
                            startActivity(new Intent(MainApplovinActivity.this, MainActivityApplovin2.class));
                        }
                    }, true);
                } else {
                    Toast.makeText(MainApplovinActivity.this, "interstitial not loaded", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(MainApplovinActivity.this, MainActivityApplovin2.class));
                }
            }
        });
        Admob.getInstance().loadNativeAd(this, getString(R.string.admod_native_id), new NativeCallback() {
            @Override
            public void onNativeAdLoaded(NativeAd nativeAd) {
                NativeAdView adView = (NativeAdView) LayoutInflater.from(MainApplovinActivity.this).inflate(R.layout.ads_native, null);
                frAds.removeAllViews();
                frAds.addView(adView);
                Admob.getInstance().pushAdsToViewCustom(nativeAd, adView);
            }

            @Override
            public void onAdFailedToLoad() {
                frAds.removeAllViews();
            }
        });
       /* AppLovin.getInstance().loadNativeAd(this,  getString(R.string.applovin_test_native), R.layout.layout_native_custom, new AppLovinCallback(){
            @Override
            public void onUnifiedNativeAdLoaded(MaxNativeAdView unifiedNativeAd) {
                super.onUnifiedNativeAdLoaded(unifiedNativeAd);
                frAds.removeAllViews();
                frAds.addView(unifiedNativeAd);
            }
        });
*/


    }

    @Override
    protected void onResume() {
        super.onResume();

    }
}