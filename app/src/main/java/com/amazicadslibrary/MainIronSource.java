package com.amazicadslibrary;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.amazic.ads.util.AppIronSource;

public class MainIronSource extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_iron_source);
        AppIronSource.getInstance().loadBanner(this);
    }
}