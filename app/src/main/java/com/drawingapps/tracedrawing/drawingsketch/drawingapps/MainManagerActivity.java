package com.drawingapps.tracedrawing.drawingsketch.drawingapps;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.amazic.ads.callback.BannerCallBack;
import com.amazic.ads.service.AdmobApi;
import com.amazic.ads.util.Admob;
import com.amazic.ads.util.manager.banner.BannerBuilder;
import com.amazic.ads.util.manager.banner.BannerManager;
import com.amazic.ads.util.manager.native_ad.NativeBuilder;
import com.amazic.ads.util.manager.native_ad.NativeManager;
import com.ardrawing.tracedrawing.drawingsketch.drawingapps.R;
import com.google.android.gms.ads.AdView;

import java.lang.reflect.Field;
import java.util.ArrayList;

public class MainManagerActivity extends AppCompatActivity {
    private AdView adView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_manager);
        //banner
        //BannerManager bannerManager = initBannerManager();
        loadCollapse();
        //Admob.getInstance().loadBannerFloor(this, AdmobApi.getInstance().getListIDBannerAll());
        //native
        //NativeManager nativeManager = initNativeManager();
        //

        findViewById(R.id.tv_next_screen).setOnClickListener(v -> {
            //bannerManager.setReloadAds();
            //nativeManager.setReloadAds();
            startActivity(new Intent(this, MainActivity2.class));
        });
        findViewById(R.id.tv).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPopupWindow(findViewById(R.id.tv));
            }
        });
    }

    private void showPopupWindow(View anchorView) {
        // Inflate the popup_layout.xml
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.popup_layout, null);

        // Create the PopupWindow
        final PopupWindow popupWindow = new PopupWindow(popupView,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                true); // true makes it focusable

        // Set a dismiss listener for the popup
        popupWindow.setOnDismissListener(() -> {
            // Handle actions when the popup is dismissed (if needed)
        });

        // Show the popup window at the center of the screen
        popupWindow.showAtLocation(anchorView, Gravity.CENTER, 0, 0);

        // Set up the content inside the popup
        TextView popupText = popupView.findViewById(R.id.popup_text);
        popupText.setText("Hello! This is a custom popup!");

        // Set up the close button
        Button closeButton = popupView.findViewById(R.id.close_button);
        closeButton.setOnClickListener(v -> popupWindow.dismiss());
    }

    private void loadCollapse() {
        /*if (adView != null) {
            adView.destroy();
        }*/
        adView = Admob.getInstance().loadCollapsibleBannerFloorWithReload(this, AdmobApi.getInstance().getListIDCollapseBannerAll(), "bottom", new BannerCallBack() {
            @Override
            public void onAdImpression() {
                super.onAdImpression();
            }

            @Override
            public void onAdOpened() {
                super.onAdOpened();
            }
        }, Admob.COUNT_CLICK, 3);
    }

    @NonNull
    private BannerManager initBannerManager() {
        BannerBuilder bannerBuilder = new BannerBuilder()
                .isIdApi();
        BannerManager bannerManager = new BannerManager(this, this, bannerBuilder);
        bannerManager.setAlwaysReloadOnResume(true);
        return bannerManager;
    }

    @NonNull
    private NativeManager initNativeManager() {
        NativeBuilder nativeBuilder = new NativeBuilder(
                this,
                findViewById(R.id.fr_ads),
                com.amazic.ads.R.layout.ads_native_shimer,
                R.layout.ads_native,
                R.layout.ads_native);
        nativeBuilder.setListIdAd(AdmobApi.getInstance().getListIDNativePermission());
        NativeManager nativeManager = new NativeManager(this, this, nativeBuilder);
        return nativeManager;
    }

    int count = 0;

    @Override
    protected void onRestart() {
        super.onRestart();
        loadCollapse();
        /*count++;
        if (count % 2 == 0)
            startActivity(new Intent(this, ResumeActivity.class));*/
    }
}