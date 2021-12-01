package com.amazicadslibrary;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.amazic.ads.callback.InterCallback;
import com.amazic.ads.callback.NativeCallback;
import com.amazic.ads.util.Admod;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdView;

public class Fragment1 extends Fragment {
    Button btnclick;
    InterstitialAd mInterstitialAd;
    private FrameLayout native_ads;
    @Override
    public View onCreateView( LayoutInflater inflater, ViewGroup container,  Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment1,container,false);
        Admod.getInstance().loadBannerFragment(requireActivity(),getString(R.string.admod_banner_id),view);

        return view;

    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        Admod.getInstance().loadInterAds(getContext(),getString(R.string.admod_interstitial_id), new InterCallback(){
            @Override
            public void onInterstitialLoad(InterstitialAd interstitialAd) {
                super.onInterstitialLoad(interstitialAd);
                mInterstitialAd = interstitialAd;
            }
        });


        native_ads = view.findViewById(R.id.native_ads);
        btnclick  = view.findViewById(R.id.btnclick);
        btnclick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Admod.getInstance().showInterAds(getActivity(),mInterstitialAd,new InterCallback(){
                    @Override
                    public void onAdClosed() {
                        super.onAdClosed();
                        ((MainActivity2)getActivity()).showFragment(new Fragment2(),"BlankFragment2");
                    }

                    @Override
                    public void onAdFailedToLoad(LoadAdError i) {
                        super.onAdFailedToLoad(i);
                        ((MainActivity2)getActivity()).showFragment(new Fragment2(),"BlankFragment2");
                    }
                });
            }
        });


        Admod.getInstance().loadNativeAd(getContext(), getString(R.string.admod_native_id), new NativeCallback() {
            @Override
            public void onNativeAdLoaded(NativeAd nativeAd) {
                NativeAdView adView = ( NativeAdView) LayoutInflater.from(getActivity()).inflate(R.layout.layout_native_custom, null);
                native_ads.addView(adView);
                Admod.getInstance().pushAdsToViewCustom(nativeAd, adView);
            }
        });
        super.onViewCreated(view, savedInstanceState);
    }
}
