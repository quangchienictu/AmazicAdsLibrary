package com.drawingapps.tracedrawing.drawingsketch.drawingapps;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.FrameLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.amazic.ads.callback.BannerCallBack;
import com.amazic.ads.callback.InterCallback;
import com.amazic.ads.callback.NativeCallback;
import com.amazic.ads.callback.RewardCallback;
import com.amazic.ads.service.AdmobApi;
import com.amazic.ads.util.Admob;
import com.amazic.ads.util.manager.native_ad.NativeBuilder;
import com.amazic.ads.util.manager.native_ad.NativeManager;
import com.ardrawing.tracedrawing.drawingsketch.drawingapps.R;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdView;
import com.google.android.gms.ads.rewarded.RewardItem;

public class MainActivity4 extends AppCompatActivity {
    InterstitialAd mInterstitialAd;
    private AdView adView;

    @Override
    protected void onStop() {
        super.onStop();
        if (adView != null) {
            adView.destroy();
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        adView = AdmobApi.getInstance().loadCollapsibleBannerFloorWithReload(this, new BannerCallBack() {
            @Override
            public void onAdImpression() {
                super.onAdImpression();
                Log.d("TAG", "onAdImpressionxxx: ");
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main4);
        adView = AdmobApi.getInstance().loadCollapsibleBannerFloorWithReload(this, new BannerCallBack() {
            @Override
            public void onAdImpression() {
                super.onAdImpression();
                Log.d("TAG", "onAdImpressionxxx: ");
            }
        });
        AdmobApi.getInstance().loadInterAll(this);
        Admob.getInstance().initRewardAds(this, "ca-app-pub-3940256099942544/5224354917");
        Admob.getInstance().loadInterAds(this, "ca-app-pub-3940256099942544/8691691433", new InterCallback() {
            @Override
            public void onAdLoadSuccess(InterstitialAd interstitialAd) {
                super.onAdLoadSuccess(interstitialAd);
                mInterstitialAd = interstitialAd;
            }
        });
        findViewById(R.id.on_off_ads).setOnClickListener(v -> {
            Admob.getInstance().setOpenShowAllAds(!Admob.isShowAllAds);
            recreate();
        });
        findViewById(R.id.reward).setOnClickListener(v -> {
            Admob.getInstance().showRewardAds(this, new RewardCallback() {
                @Override
                public void onEarnedReward(RewardItem rewardItem) {
                }

                @Override
                public void onAdClosed() {

                }

                @Override
                public void onAdFailedToShow(int codeError) {

                }

                @Override
                public void onAdImpression() {

                }
            });
        });
        findViewById(R.id.inter).setOnClickListener(v -> {
            Admob.getInstance().showInterAds(this, mInterstitialAd, new InterCallback() {
                @Override
                public void onLoadInter() {
                    super.onLoadInter();
                    Admob.getInstance().loadInterAds(MainActivity4.this, "ca-app-pub-3940256099942544/8691691433", new InterCallback() {
                        @Override
                        public void onAdLoadSuccess(InterstitialAd interstitialAd) {
                            super.onAdLoadSuccess(interstitialAd);
                            mInterstitialAd = interstitialAd;
                        }
                    });
                }
            });
        });
        findViewById(R.id.interFloor).setOnClickListener(v -> {
            AdmobApi.getInstance().showInterAll(this, new InterCallback());
        });
        findViewById(R.id.on_off_ads).setOnClickListener(v -> {
            Admob.getInstance().setOpenShowAllAds(!Admob.isShowAllAds);
            recreate();
        });
        loadNative();
        loadNativeFloor();
        loadNativeAuto();
    }

    private void loadNativeAuto() {
        FrameLayout fl_native = findViewById(R.id.fr_native_auto);
        NativeBuilder builder = new NativeBuilder(this, fl_native,
                com.amazic.ads.R.layout.ads_native_shimer_small, com.amazic.ads.R.layout.ads_native_small);
        builder.setListIdAd(AdmobApi.getInstance().getListIDNativeAll());
        NativeManager manager = new NativeManager(this, this, builder, fl_native,
                com.amazic.ads.R.layout.ads_native_shimer_small, com.amazic.ads.R.layout.layout_native_meta);
    }

    private void loadNativeFloor() {
        FrameLayout fl_native = findViewById(R.id.fr_native_floor);
        Admob.getInstance().loadNativeAd(this, AdmobApi.getInstance().getListIDNativeAll(), new NativeCallback() {
            @Override
            public void onNativeAdLoaded(NativeAd nativeAd) {
                NativeAdView adView = (NativeAdView) LayoutInflater.from(MainActivity4.this).inflate(R.layout.ads_native, null);
                fl_native.removeAllViews();
                fl_native.addView(adView);
                Admob.getInstance().pushAdsToViewCustom(nativeAd, adView);
            }

            @Override
            public void onAdFailedToLoad() {
                fl_native.removeAllViews();
            }
        });
    }

    private void loadNative() {
        FrameLayout fl_native = findViewById(R.id.fr_native);
        Admob.getInstance().loadNativeAd(this, getString(R.string.admod_native_id), new NativeCallback() {
            @Override
            public void onNativeAdLoaded(NativeAd nativeAd) {
                NativeAdView adView = (NativeAdView) LayoutInflater.from(MainActivity4.this).inflate(R.layout.ads_native, null);
                fl_native.removeAllViews();
                fl_native.addView(adView);
                Admob.getInstance().pushAdsToViewCustom(nativeAd, adView);
            }

            @Override
            public void onAdFailedToLoad() {
                fl_native.removeAllViews();
            }
        });
    }
}