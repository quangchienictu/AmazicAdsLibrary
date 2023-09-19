package com.amazicadslibrary;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.fragment.app.Fragment;

import com.amazic.ads.callback.InterCallback;
import com.amazic.ads.util.Admob;
import com.amazic.ads.util.BannerGravity;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.interstitial.InterstitialAd;

public class Fragment1 extends Fragment {
    Button btnclick;
    InterstitialAd mInterstitialAd;
    @Override
    public View onCreateView( LayoutInflater inflater, ViewGroup container,  Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment1,container,false);
        Admob.getInstance().loadCollapsibleBannerFragmentFloor(requireActivity(),MainActivity.listID,view, BannerGravity.bottom);
        return view;

    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        Admob.getInstance().loadInterAds(getContext(),getString(R.string.admod_interstitial_id), new InterCallback(){

            @Override
            public void onAdLoadSuccess(InterstitialAd interstitialAd) {
                super.onAdLoadSuccess(interstitialAd);
                mInterstitialAd = interstitialAd;
            }
        });
        btnclick  = view.findViewById(R.id.btnclick);
        btnclick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Admob.getInstance().showInterAds(getActivity(),mInterstitialAd,new InterCallback(){
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

        super.onViewCreated(view, savedInstanceState);
    }
}
