package com.amazic.ads.util.manager.native_ad;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.LayoutRes;

import com.amazic.ads.R;
import com.amazic.ads.callback.NativeCallback;
import com.amazic.ads.service.AdmobApi;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.gms.ads.nativead.NativeAdView;

import java.util.ArrayList;
import java.util.List;

public class NativeBuilder {
    private static final String TAG = "NativeBuilder";
    private NativeCallback callback = new NativeCallback();
    List<String> listIdAd = new ArrayList<>();
    NativeAdView nativeAdView;
    NativeAdView nativeMetaAdView;
    ShimmerFrameLayout shimmerFrameLayout;

    public NativeBuilder(Context context, FrameLayout flAd, @LayoutRes int idLayoutShimmer, @LayoutRes int idLayoutNative, @LayoutRes int idLayoutNativeMeta) {
        setLayoutAds(context, flAd, idLayoutShimmer, idLayoutNative, idLayoutNativeMeta);
    }

    private void setLayoutAds(Context context, FrameLayout flAd, @LayoutRes int idLayoutShimmer, @LayoutRes int idLayoutNative, @LayoutRes int idLayoutNativeMeta) {
        try {
            nativeAdView = (NativeAdView) LayoutInflater.from(context).inflate(idLayoutNative, null);
            nativeMetaAdView = (NativeAdView) LayoutInflater.from(context).inflate(idLayoutNativeMeta, null);
            shimmerFrameLayout = (ShimmerFrameLayout) LayoutInflater.from(context).inflate(idLayoutShimmer, null);
        } catch (ClassCastException classCastException) {
            nativeAdView = (NativeAdView) LayoutInflater.from(context).inflate(R.layout.ads_native_large, null);
            nativeMetaAdView = (NativeAdView) LayoutInflater.from(context).inflate(R.layout.ads_native_meta_large, null);
            shimmerFrameLayout = (ShimmerFrameLayout) LayoutInflater.from(context).inflate(R.layout.ads_shimmer_large, null);
        }
        flAd.removeAllViews();
        flAd.addView(nativeMetaAdView);
        flAd.addView(nativeAdView);
        flAd.addView(shimmerFrameLayout);
        showLoading();
    }

    public void setListIdAd(List<String> listIdAd) {
        this.listIdAd.clear();
        this.listIdAd.addAll(listIdAd);
    }

    public void setListIdAd(String nameIdAd) {
        this.listIdAd.clear();
        this.listIdAd.addAll(AdmobApi.getInstance().getListIDByName(nameIdAd));
    }

    public NativeCallback getCallback() {
        return callback;
    }

    public void setCallback(NativeCallback callback) {
        this.callback = callback;
    }

    public void showAd() {
        nativeAdView.setVisibility(View.VISIBLE);
        shimmerFrameLayout.setVisibility(View.GONE);
        nativeMetaAdView.setVisibility(View.GONE);
    }

    public void showAdMeta() {
        nativeMetaAdView.setVisibility(View.VISIBLE);
        nativeAdView.setVisibility(View.GONE);
        shimmerFrameLayout.setVisibility(View.GONE);
    }

    public void showLoading() {
        shimmerFrameLayout.setVisibility(View.VISIBLE);
        nativeAdView.setVisibility(View.GONE);
        nativeMetaAdView.setVisibility(View.GONE);
    }

    public void hideAd() {
        shimmerFrameLayout.setVisibility(View.GONE);
        nativeAdView.setVisibility(View.GONE);
    }
}
