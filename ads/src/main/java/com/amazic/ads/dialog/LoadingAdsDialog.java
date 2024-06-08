package com.amazic.ads.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.LayoutRes;

import com.amazic.ads.R;

public class LoadingAdsDialog extends Dialog {
    private static int layoutLoadingAdsView = -1;

    public static void setLayoutLoadingAdsView(@LayoutRes int layoutLoadingAdsView) {
        LoadingAdsDialog.layoutLoadingAdsView = layoutLoadingAdsView;
    }

    public static void resetLayoutLoadingAdsView() {
        LoadingAdsDialog.layoutLoadingAdsView = -1;
    }

    public LoadingAdsDialog(Context context) {
        super(context, R.style.AppTheme);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (layoutLoadingAdsView == -1) setContentView(R.layout.dialog_loading_ads);
        else setContentView(layoutLoadingAdsView);
    }
}
