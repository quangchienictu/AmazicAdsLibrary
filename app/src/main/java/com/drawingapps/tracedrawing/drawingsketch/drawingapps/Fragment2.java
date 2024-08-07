package com.drawingapps.tracedrawing.drawingsketch.drawingapps;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.fragment.app.Fragment;

import com.amazic.ads.callback.NativeCallback;
import com.amazic.ads.util.Admob;
import com.ardrawing.tracedrawing.drawingsketch.drawingapps.R;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdView;

public class Fragment2 extends Fragment {
    FrameLayout fr_ads1;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment2,container,false);
        fr_ads1 = view.findViewById(R.id.fr_ads1);
        try {
            Admob.getInstance().loadNativeAd(getContext(), getString(R.string.admod_native_id), new NativeCallback() {
                @Override
                public void onNativeAdLoaded(NativeAd nativeAd) {
                    NativeAdView adView = (NativeAdView) LayoutInflater.from(getActivity()).inflate(R.layout.ads_native, null);
                    fr_ads1.removeAllViews();
                    fr_ads1.addView(adView);
                    Admob.getInstance().pushAdsToViewCustom(nativeAd, adView);
                }

                @Override
                public void onAdFailedToLoad() {
                    fr_ads1.removeAllViews();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            fr_ads1.removeAllViews();
        }
        return view;
    }

    @Override
    public void onViewCreated(View view,Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

      //  Admob.getInstance().loadNativeFragment(getActivity(),getString(R.string.admod_native_id),view);
    }
}
