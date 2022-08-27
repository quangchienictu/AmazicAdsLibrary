package com.amazic.ads.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;

import com.amazic.ads.R;

public class LoadingAdsDialog extends Dialog {
    public LoadingAdsDialog(Context context) {
        super(context, R.style.AppTheme);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_loading_ads);
    }

    @Override
    public void onBackPressed() {}
}
