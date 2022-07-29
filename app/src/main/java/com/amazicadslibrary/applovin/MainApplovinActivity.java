package com.amazicadslibrary.applovin;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.amazic.ads.applovin.AppLovin;
import com.amazic.ads.applovin.AppLovinCallback;
import com.amazic.ads.callback.InterCallback;
import com.amazicadslibrary.R;
import com.applovin.mediation.ads.MaxInterstitialAd;
import com.applovin.mediation.ads.MaxRewardedAd;
import com.applovin.mediation.nativeAds.MaxNativeAdView;
import com.facebook.shimmer.ShimmerFrameLayout;

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
        interstitialAd = AppLovin.getInstance().getInterstitialAds(this, getString(R.string.admod_interstitial_id));
        AppLovin.getInstance().loadBanner(this, getString(R.string.applovin_test_banner));

        //load reward ad
        btnLoadReward = findViewById(R.id.btnLoadReward);
        btnLoadReward.setOnClickListener(view -> {
            if (maxRewardedAd != null && maxRewardedAd.isReady()) {
                AppLovin.getInstance().showRewardAd(this, maxRewardedAd);
            } else {
                maxRewardedAd = AppLovin.getInstance().getRewardAd(this, getString(R.string.applovin_test_reward), new AppLovinCallback() {
                    @Override
                    public void onAdLoaded() {
                        Toast.makeText(MainApplovinActivity.this, "reward loaded", Toast.LENGTH_SHORT).show();
                        btnLoadReward.setText("Show Reward");
                    }

                    @Override
                    public void onAdClosed() {
                        startActivity(new Intent(MainApplovinActivity.this, SimpleListActivity.class));
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
                            startActivity(new Intent(MainApplovinActivity.this, SimpleListActivity.class));
                        }
                    }, true);
                } else {
                    Toast.makeText(MainApplovinActivity.this, "interstitial not loaded", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(MainApplovinActivity.this, SimpleListActivity.class));
                }
            }
        });
        //get native and add to view
/*        AppLovin.getInstance().loadNativeAd(this, "c810c577b4c36ee5", com.ads.control.R.layout.max_native_custom_ad_view, new AppLovinCallback() {
            @Override
            public void onUnifiedNativeAdLoaded(MaxNativeAdView unifiedNativeAd) {
                super.onUnifiedNativeAdLoaded(unifiedNativeAd);
                findViewById(R.id.shimmer_container_native).setVisibility(View.GONE);
                FrameLayout fl = findViewById(R.id.fl_adplaceholder);
                fl.setVisibility(View.VISIBLE);
                fl.addView(unifiedNativeAd);
            }
        });*/

        AppLovin.getInstance().loadNativeAd(this, frAds, getString(R.string.applovin_test_native), R.layout.layout_native_custom);

    }

    @Override
    protected void onResume() {
        super.onResume();

    }
}