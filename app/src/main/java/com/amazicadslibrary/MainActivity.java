package com.amazicadslibrary;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.amazic.ads.callback.InterCallback;
import com.amazic.ads.iap.IAPManager;
import com.amazic.ads.iap.PurchaseCallback;
import com.amazic.ads.service.AdmobApi;
import com.amazic.ads.util.Admob;
import com.amazic.ads.util.reward.RewardAdCallback;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.interstitial.InterstitialAd;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private InterstitialAd mInterstitialAd;
    private FrameLayout native_ads;

    public static String PRODUCT_ID_YEAR = "android.test.purchased";
    public static String PRODUCT_ID_MONTH = "android.test.purchased";
    public static List<String> listID;

    boolean firstItem = true;

    private boolean detectTestAd(ViewGroup viewGroup) {
        Log.d("detectTestAd", "viewGroup: " + viewGroup.getClass());
        for (int i = 0; i < viewGroup.getChildCount(); i++) {
            View viewChild = viewGroup.getChildAt(i);
            Log.d("detectTestAd", "viewChild: " + viewChild.getClass());
            if (viewChild instanceof ViewGroup) {
                if (detectTestAd((ViewGroup) viewChild))
                    return true;
            }
            if (viewChild instanceof TextView) {
                return true;
            }
        }
        return false;
    }
    boolean showAfter = true;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        native_ads = findViewById(R.id.native_ads);
        listID = new ArrayList<>();
        listID.add(getString(R.string.admod_banner_collap_id));
        AdmobApi.getInstance().loadBanner(this);
        Admob.getInstance().initRewardAds(this, getString(R.string.admod_app_reward_id));
        loadAdInter();
        loadAdsNative();
//        AppOpenManager.getInstance().disableAppResumeWithActivity(getClass());
        Log.d("fakljfksdafkas", "onCreate: " + detectTestAd(findViewById(R.id.layout_check)));
        findViewById(R.id.clickFGM).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, MainActivity2.class));
            }
        });
        findViewById(R.id.check_remote).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });



        findViewById(R.id.btnClickInter).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAfter = !showAfter;
                Admob.getInstance().setOpenActivityAfterShowInterAds(showAfter);
                AdmobApi.getInstance().showInterAll(MainActivity.this, new InterCallback() {
                    @Override
                    public void onNextAction() {
                        super.onNextAction();
                        Log.d(TAG, "onNextAction: ");
                        startActivity(new Intent(MainActivity.this, MainActivity3.class));
                    }

                    @Override
                    public void onLoadInter() {
                        super.onLoadInter();
                        Log.d(TAG, "onLoadInter: ");
                    }
                });
            }
        });
        final List<String> idReward = new ArrayList<>();
        idReward.add("rewarded_vip_1");
        idReward.add("rewarded_vip");
        findViewById(R.id.btnClickReward).setOnClickListener(v -> {
            String name = idReward.get(1);
            if (firstItem) {
                name = idReward.get(0);
            }
            firstItem = !firstItem;
            Log.d("RewardAdModel_Check", "onCreate: " + name);

            Admob.getInstance().loadAndShowReward(this, name, new RewardAdCallback() {
                @Override
                public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                    super.onAdFailedToLoad(loadAdError);
                    Log.e("RewardAdModel_Check", "onAdFailedToLoad: " + loadAdError.getMessage());
                }

                @Override
                public void onAdFailedToShow(@NonNull AdError adError) {
                    super.onAdFailedToShow(adError);
                    Log.e("RewardAdModel_Check", "onAdFailedToShow: " + adError.getMessage());
                }

                @Override
                public void onAdShowed() {
                    super.onAdShowed();
                    Log.d("RewardAdModel_Check", "onAdShowed: ");
                }

                @Override
                public void onNextAction() {
                    super.onNextAction();
                    Log.d("RewardAdModel_Check", "onNextAction: ");
                }

                @Override
                public void onAdLoaded(Boolean isSuccessful) {
                    super.onAdLoaded(isSuccessful);
                    Log.d("RewardAdModel_Check", "onAdLoaded: " + isSuccessful);
                }
            });
        });


        findViewById(R.id.btnClickLoadAndShow).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Admob.getInstance().loadAndShowInter(MainActivity.this, getString(R.string.admod_interstitial_id), 0, 10000, new InterCallback() {
                    @Override
                    public void onAdClosed() {
                        super.onAdClosed();
                        startActivity(new Intent(MainActivity.this, MainActivity2.class));
                    }

                    @Override
                    public void onAdFailedToLoad(LoadAdError i) {
                        super.onAdFailedToLoad(i);
                        startActivity(new Intent(MainActivity.this, MainActivity2.class));
                    }
                });
            }
        });


        findViewById(R.id.btnBilding).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //AppPurchase.getInstance().consumePurchase(PRODUCT_ID_MONTH);
                //AppPurchase.getInstance().purchase(MainActivity.this, PRODUCT_ID_MONTH);
                //AppPurchase.getInstance().subscribe(MainActivity.this, PRODUCT_ID_MONTH);
                //real
                // AppPurchase.getInstance().subscribe(MainActivity.this, SubID);
                IAPManager.getInstance().purchase(MainActivity.this, IAPManager.PRODUCT_ID_TEST);
            }
        });

        IAPManager.getInstance().setPurchaseListener(new PurchaseCallback() {
            @Override
            public void onProductPurchased(String productId, String transactionDetails) {
                super.onProductPurchased(productId, transactionDetails);
                Toast.makeText(MainActivity.this, "Purchase success: " + IAPManager.getInstance().isPurchase(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onUserCancelBilling() {
                super.onUserCancelBilling();
                Toast.makeText(MainActivity.this, "Purchase cancel", Toast.LENGTH_SHORT).show();
            }
        });


        /*AppPurchase.getInstance().setPurchaseListener(new PurchaseListener() {
            @Override
            public void onProductPurchased(String productId,String transactionDetails) {
               Toast.makeText(MainActivity.this,"Purchase success",Toast.LENGTH_SHORT).show();
            }

            @Override
            public void displayErrorMessage(String errorMsg) {
                Toast.makeText(MainActivity.this,"Purchase fall",Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onUserCancelBilling() {

                Toast.makeText(MainActivity.this,"Purchase cancel",Toast.LENGTH_SHORT).show();
            }
        });*/
        // reset pay Purchase
       /*AppPurchase.getInstance().consumePurchase(Constants.PRODUCT_ID_MONTH);
        AppPurchase.getInstance().consumePurchase(Constants.PRODUCT_ID_YEAR);
        AppPurchase.getInstance().setPurchaseListioner(new PurchaseListioner() {
            @Override
            public void onProductPurchased(String productId,String transactionDetails) {

            }

            @Override
            public void displayErrorMessage(String errorMsg) {
                Log.e("PurchaseListioner","displayErrorMessage:"+ errorMsg);
            }

            @Override
            public void onUserCancelBilling() {

            }
        });*/

    }

    private void loadAdsNative() {
        /*List<String> listID = new ArrayList<>();
        listID.add("1");
        listID.add("2");

        // Admob.getInstance().loadNativeAdFloor(this, listID, native_ads,R.layout.ads_native_btn_ads_top);
        Admob.getInstance().loadNativeAd(this, listID, new NativeCallback() {
            @Override
            public void onNativeAdLoaded(NativeAd nativeAd) {
                super.onNativeAdLoaded(nativeAd);
                NativeAdView adView = (NativeAdView) LayoutInflater.from(MainActivity.this).inflate(R.layout.ads_native, null);
                native_ads.addView(adView);
                Admob.getInstance().pushAdsToViewCustom(nativeAd, adView);
            }

            @Override
            public void onAdFailedToLoad() {
                super.onAdFailedToLoad();
                Log.e("xxxx native", "onAdFailedToLoad");
            }

            @Override
            public void onEarnRevenue(Double Revenue) {
                super.onEarnRevenue(Revenue);
                Log.e("xxxx native", "onEarnRevenue");
            }

            @Override
            public void onAdClicked() {
                super.onAdClicked();
                Log.e("xxxx native", "onAdClicked");
            }
        });


        Admob.getInstance().loadNativeAd(this, "id native", native_ads, R.layout.ads_native);*/

//        FrameLayout fl_native = findViewById(R.id.native_ads);
//        NativeBuilder builder = new NativeBuilder(this, fl_native,
//                com.amazic.ads.R.layout.ads_native_shimer, R.layout.ads_native);
//        builder.setListIdAd(AdmobApi.getInstance().getListIDNativeAll());
//        NativeManager manager = new NativeManager(this, this, builder, fl_native,
//                com.amazic.ads.R.layout.ads_native_shimer, com.amazic.ads.R.layout.layout_native_meta);
//        manager.setIntervalReloadNative(5000);
    }

    private void loadAdInter() {
        AdmobApi.getInstance().loadInterAll(this, new InterCallback() {
            @Override
            public void onAdLoadSuccess(InterstitialAd interstitialAd) {
                super.onAdLoadSuccess(interstitialAd);
                Log.d(TAG, "onAdLoadSuccess: ");
            }
        });
    }

    private static final String TAG = "MainActivityX";
}