package com.amazicadslibrary;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.amazic.ads.callback.InterCallback;
import com.amazic.ads.util.AppIronSource;
import com.google.android.gms.ads.LoadAdError;

public class MainIronSource extends AppCompatActivity {
    Button btnShow,btnLoadAndShow;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_iron_source);

        btnShow = findViewById(R.id.btnShow);
        btnLoadAndShow = findViewById(R.id.btnLoadAndShow);

        loadInter();

        btnShow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (AppIronSource.getInstance().isInterstitialReady()){
                    AppIronSource.getInstance().showInterstitial();
                }else{
                    Toast.makeText(MainIronSource.this, "Ad not loaded", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnLoadAndShow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AppIronSource.getInstance().loadAndShowInter(MainIronSource.this,15000, new InterCallback(){
                    @Override
                    public void onAdClosed() {
                        super.onAdClosed();
                        startActivity(new Intent(MainIronSource.this,MainIronSource2.class));
                    }

                    @Override
                    public void onAdFailedToLoad(LoadAdError i) {
                        super.onAdFailedToLoad(i);
                        startActivity(new Intent(MainIronSource.this,MainIronSource2.class));
                    }
                });
            }
        });
    }

    public void loadInter(){
        if(!AppIronSource.getInstance().isInterstitialReady()){
            AppIronSource.getInstance().loadInterstitial(MainIronSource.this, new InterCallback(){
                @Override
                public void onAdClosed() {
                    super.onAdClosed();
                    AppIronSource.getInstance().destroyBanner(); // destroy banner nếu xử dụng banner trong 1 activity khác
                    startActivity(new Intent(MainIronSource.this,MainIronSource2.class));
                    loadInter();  // load lại nếu sử dụng tiếp mà k finish activity
                }

                @Override
                public void onAdFailedToLoad(LoadAdError i) {
                    super.onAdFailedToLoad(i);
                }
            });
        }
    }
    @Override
    protected void onStart() {
        super.onStart();
        AppIronSource.getInstance().destroyBanner();
        AppIronSource.getInstance().loadBanner(this);
    }
}