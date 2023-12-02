package com.amazic.ads.util.manager.native_ad;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.LayoutRes;

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
    ShimmerFrameLayout shimmerFrameLayout;

    public NativeBuilder(Context context, FrameLayout flAd, @LayoutRes int idLayoutShimmer, @LayoutRes int idLayoutNative) {
        setLayoutAds(context, flAd, idLayoutShimmer, idLayoutNative);
    }

    private void setLayoutAds(Context context, FrameLayout flAd, @LayoutRes int idLayoutShimmer, @LayoutRes int idLayoutNative) {
        View viewAd = LayoutInflater.from(context).inflate(idLayoutNative, null);
        View shimmer = LayoutInflater.from(context).inflate(idLayoutShimmer, null);
        flAd.removeAllViews();
        if (viewAd instanceof NativeAdView) {
            Log.d(TAG, "setLayoutAds: NativeAdView");
            nativeAdView = (NativeAdView) viewAd;
            flAd.addView(nativeAdView);
        }
        if (shimmer instanceof ShimmerFrameLayout) {
            Log.d(TAG, "setLayoutAds: ShimmerFrameLayout");
            shimmerFrameLayout = (ShimmerFrameLayout) shimmer;
            flAd.addView(shimmerFrameLayout);
        }
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
    }

    public void showLoading() {
        shimmerFrameLayout.setVisibility(View.VISIBLE);
        nativeAdView.setVisibility(View.GONE);
    }

    public void hideAd() {
        shimmerFrameLayout.setVisibility(View.GONE);
        nativeAdView.setVisibility(View.GONE);
    }
}
