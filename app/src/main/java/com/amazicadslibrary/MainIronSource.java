package com.amazicadslibrary;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.amazic.ads.callback.InterCallback;
import com.amazic.ads.callback.RewardCallBackIS;
import com.amazic.ads.callback.RewardCallback;
import com.amazic.ads.util.AppIronSource;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.rewarded.RewardItem;

public class MainIronSource extends AppCompatActivity {
    Button btnShow, btnLoadAndShow,btnShowIS;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_iron_source);

        btnShow = findViewById(R.id.btnShow);
        btnLoadAndShow = findViewById(R.id.btnLoadAndShow);
        btnShowIS = findViewById(R.id.btnShowIS);



        loadInter();
        btnShowIS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    AppIronSource.getInstance().showInterstitial(MainIronSource.this, new InterCallback(){
                        @Override
                        public void onAdClosed() {
                            super.onAdClosed();
                            startActivity(new Intent(MainIronSource.this, MainIronSource2.class));
                            loadInter();
                        }

                        @Override
                        public void onAdFailedToLoad(LoadAdError i) {
                            super.onAdFailedToLoad(i);
                            startActivity(new Intent(MainIronSource.this, MainIronSource2.class));
                            loadInter();
                        }
                    });
            }
        });
        btnLoadAndShow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AppIronSource.getInstance().loadAndShowRewards( new RewardCallBackIS() {
                    @Override
                    public void onEarnedReward() {
                        Log.e("xxxx", "onEarnedReward: ");
                    }

                    @Override
                    public void onAdClosed() {
                        Log.e("xxxx", "onAdClosed: ");
                    }


                    @Override
                    public void onAdFailedToShow() {
                        Log.e("xxxx", "onAdFailedToShow: ");
                    }
                });
            }
        });
    }

    public void loadInter() {
        AppIronSource.getInstance().loadInterstitial(MainIronSource.this, new InterCallback() {});
    }

    @Override
    protected void onStart() {
        super.onStart();
        AppIronSource.getInstance().loadBanner(this);
    }
}