package com.amazicadslibrary;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.amazic.ads.service.AdmobApi;
import com.amazic.ads.util.manager.banner.BannerBuilder;
import com.amazic.ads.util.manager.banner.BannerManager;
import com.amazic.ads.util.manager.native_ad.NativeBuilder;
import com.amazic.ads.util.manager.native_ad.NativeManager;

public class MainManagerActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_manager);
        //banner
        BannerManager bannerManager = initBannerManager();
        //native
        NativeManager nativeManager = initNativeManager();
        //

        findViewById(R.id.tv_next_screen).setOnClickListener(v -> {
            bannerManager.setReloadAds();
            nativeManager.setReloadAds();
            startActivity(new Intent(this, MainActivity2.class));
        });
    }

    @NonNull
    private BannerManager initBannerManager() {
        BannerBuilder bannerBuilder = new BannerBuilder(this, this)
                .isIdApi();
        BannerManager bannerManager = new BannerManager(bannerBuilder);
        return bannerManager;
    }

    @NonNull
    private NativeManager initNativeManager() {
        NativeBuilder nativeBuilder = new NativeBuilder(
                this,
                findViewById(R.id.fr_ads),
                R.layout.ads_native_shimer,
                R.layout.ads_native);
        nativeBuilder.setListIdAd(AdmobApi.getInstance().getListIDNativePermission());
        NativeManager nativeManager = new NativeManager(this, this, nativeBuilder);
        return nativeManager;
    }

    int count = 0;

    @Override
    protected void onRestart() {
        super.onRestart();
        count++;
        if (count % 2 == 0)
            startActivity(new Intent(this, ResumeActivity.class));
    }
}