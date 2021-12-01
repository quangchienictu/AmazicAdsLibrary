package com.amazicadslibrary;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.FrameLayout;

import com.amazic.ads.callback.NativeCallback;
import com.amazic.ads.util.Admod;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdView;

public class MainActivity3 extends AppCompatActivity {
   FrameLayout fr_ads;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main3);
        fr_ads = findViewById(R.id.fr_ads);

        Admod.getInstance().loadNativeAd(this, getString(R.string.admod_native_id), new NativeCallback() {
            @Override
            public void onNativeAdLoaded(NativeAd nativeAd) {
                NativeAdView adView = ( NativeAdView) LayoutInflater.from(MainActivity3.this).inflate(R.layout.layout_native_custom, null);
                fr_ads.removeAllViews();
                fr_ads.addView(adView);
                Admod.getInstance().pushAdsToViewCustom(nativeAd, adView);
            }
        });

    }
}