package com.amazicadslibrary;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.FrameLayout;

import com.amazic.ads.callback.NativeCallback;
import com.amazic.ads.util.Admob;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity3 extends AppCompatActivity {
   FrameLayout fr_ads;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main3);
        fr_ads = findViewById(R.id.fr_ads);
        List<String> listID = new ArrayList<>();
        listID.add("getString(R.string.ads_test_inter)");
        listID.add(getString(R.string.ads_test_banner));
        listID.add("getString(R.string.admod_banner_collap_id)");
        Admob.getInstance().loadBannerFloor(this, listID);
        Admob.getInstance().loadNativeAd(this, getString(R.string.admod_native_id), new NativeCallback(){
            @Override
            public void onNativeAdLoaded(NativeAd nativeAd) {
                NativeAdView adView = ( NativeAdView) LayoutInflater.from(MainActivity3.this).inflate(R.layout.layout_native_custom, null);
                fr_ads.removeAllViews();
                fr_ads.addView(adView);
                Admob.getInstance().pushAdsToViewCustom(nativeAd, adView);
            }

            @Override
            public void onAdFailedToLoad() {
                fr_ads.removeAllViews();
            }
        });

    }
}