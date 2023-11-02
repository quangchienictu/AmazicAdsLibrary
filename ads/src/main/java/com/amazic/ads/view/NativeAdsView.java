package com.amazic.ads.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.AppCompatButton;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.amazic.ads.R;
import com.amazic.ads.callback.NativeCallback;
import com.amazic.ads.event.AdmobEvent;
import com.amazic.ads.service.AdmobApi;
import com.amazic.ads.util.Admob;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdView;

import java.util.List;

public class NativeAdsView extends FrameLayout {
    NativeAdView adView;
    boolean typeLayout = false;

    public NativeAdsView(@NonNull Context context) {
        super(context);
        setView(context);
        setContentInView(context);
    }

    @SuppressLint("CustomViewStyleable")
    public NativeAdsView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setView(context);
        TypedArray attributes = context.obtainStyledAttributes(attrs, R.styleable.LayoutNative);
        setContentInView(context, attributes);
    }

    public NativeAdsView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setView(context);
    }

    @SuppressLint("CustomViewStyleable")
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public NativeAdsView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        setView(context);
        TypedArray attributes = context.obtainStyledAttributes(attrs, R.styleable.LayoutNative, defStyleAttr, R.style.LayoutNative);
        setContentInView(context, attributes);
    }

    //set up layout
    private void setView(Context context) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.layout_native, this, true);
    }

    private void setContentInView(Context context) {
        adView = (NativeAdView) LayoutInflater.from(context).inflate(R.layout.ads_native_large, null);
    }

    private void setContentInView(Context context, TypedArray attributes) {
        if (!Admob.isShowAllAds) {
            removeAllViews();
            return;
        }
        int textHeaderColor = attributes.getColor(
                R.styleable.LayoutNative_ln_text_header_color,
                Color.parseColor("#000000"));
        int textDescriptionColor = attributes.getColor(R.styleable.LayoutNative_ln_text_desc_color,
                Color.parseColor("#000000"));
        int textBtnColor = attributes.getColor(R.styleable.LayoutNative_ln_text_btn_color,
                Color.parseColor("#FFFFFF"));

        int styleHeader = attributes.getResourceId(R.styleable.LayoutNative_ln_style_text_header, -1);
        int styleDesc = attributes.getResourceId(R.styleable.LayoutNative_ln_style_text_desc, -1);
        int styleBtn = attributes.getResourceId(R.styleable.LayoutNative_ln_style_btn, -1);

        Drawable backgroundIcon = attributes.getDrawable(R.styleable.LayoutNative_ln_icon_background);
        Drawable backgroundBtn = attributes.getDrawable(R.styleable.LayoutNative_ln_btn_background);
        Drawable backgroundItem = attributes.getDrawable(R.styleable.LayoutNative_ln_native_background);
        typeLayout = attributes.getBoolean(R.styleable.LayoutNative_ln_type, false);
        if (typeLayout) {
            removeAllViews();
            View shimmerView = LayoutInflater.from(context).inflate(R.layout.ads_shimmer_small, null);
            addView(shimmerView);
            adView = (NativeAdView) LayoutInflater.from(context).inflate(R.layout.ads_native_small, null);
        } else {
            adView = (NativeAdView) LayoutInflater.from(context).inflate(R.layout.ads_native_large, null);
        }
        TextView tvHeader = adView.findViewById(R.id.ad_headline);
        if (styleHeader != -1)
            tvHeader.setTextAppearance(context, styleHeader);
        tvHeader.setTextColor(textHeaderColor);

        TextView tvBody = adView.findViewById(R.id.ad_body);
        if (styleDesc != -1)
            tvBody.setTextAppearance(context, styleDesc);
        tvBody.setTextColor(textDescriptionColor);

        if (backgroundIcon != null) {
            TextView tvIcon = adView.findViewById(R.id.tv_icon);
            tvIcon.setBackground(backgroundIcon);
        }
        AppCompatButton btn = adView.findViewById(R.id.ad_call_to_action);
        if (styleBtn != -1)
            btn.setTextAppearance(context, styleBtn);

        if (backgroundBtn != null)
            btn.setBackgroundDrawable(backgroundBtn);
        btn.setTextColor(textBtnColor);

        if (backgroundItem != null) {
            ConstraintLayout ctlRoot = adView.findViewById(R.id.ad_unit_content);
            ctlRoot.setBackground(backgroundItem);
        }
        attributes.recycle();
    }

    public void loadNative(List<String> idAds, String nameEvent) {
        new Thread(() -> {
            if (adView != null && idAds != null) {
                Admob.getInstance().loadNativeAd(getContext(), idAds, new NativeCallback() {
                    @Override
                    public void onNativeAdLoaded(NativeAd nativeAd) {
                        NativeAdsView.this.removeAllViews();
                        addView(adView);
                        Admob.getInstance().pushAdsToViewCustom(nativeAd, adView);
                        if (nameEvent != null)
                            AdmobEvent.logEvent(getContext(), nameEvent + "_native_view", new Bundle());
                    }

                    @Override
                    public void onAdFailedToLoad() {
                        NativeAdsView.this.removeAllViews();
                    }

                    @Override
                    public void onAdClicked() {
                        super.onAdClicked();
                        if (nameEvent != null)
                            AdmobEvent.logEvent(getContext(), nameEvent + "_native_click", new Bundle());
                    }
                });
            } else {
                removeAllViews();
            }
        }).start();
    }

    public void loadNative(String idAds, NativeCallback callback) {
        new Thread(() -> {
            if (adView != null && idAds != null) {
                Admob.getInstance().loadNativeAd(getContext(), idAds, new NativeCallback() {
                    @Override
                    public void onNativeAdLoaded(NativeAd nativeAd) {
                        NativeAdsView.this.removeAllViews();
                        addView(adView);
                        Admob.getInstance().pushAdsToViewCustom(nativeAd, adView);
                        if (callback != null)
                            callback.onNativeAdLoaded(nativeAd);
                    }

                    @Override
                    public void onAdFailedToLoad() {
                        if (callback != null)
                            callback.onAdFailedToLoad();
                        NativeAdsView.this.removeAllViews();
                    }

                    @Override
                    public void onAdClicked() {
                        super.onAdClicked();
                        if (callback != null)
                            callback.onAdClicked();
                    }

                    @Override
                    public void onEarnRevenue(Double Revenue) {
                        super.onEarnRevenue(Revenue);
                        if (callback != null)
                            callback.onEarnRevenue(Revenue);
                    }
                });
            } else {
                removeAllViews();
            }
        }).start();
    }

    public void loadNativeAll(String nameEvent) {
        loadNative(AdmobApi.getInstance().getListIDNativeAll(), nameEvent);
    }

    public void loadNative(List<String> idAds) {
        loadNative(idAds, null);
    }

    public void loadNative(String idAds) {
        loadNative(idAds, null);
    }

    public void loadNativeByName(String nameAds) {
        loadNative(AdmobApi.getInstance().getListIDByName(nameAds), null);
    }

    public void loadNativeAll() {
        loadNativeAll(null);
    }
}
