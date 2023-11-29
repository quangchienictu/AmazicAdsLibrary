package com.amazic.ads.util.manager.open_app;

import android.app.Activity;
import android.app.Application;

import androidx.annotation.NonNull;

import com.amazic.ads.dialog.LoadingAdsDialog;
import com.google.android.gms.ads.AdRequest;

import java.util.ArrayList;
import java.util.List;

public class OpenAppBuilder {
    List<String> listIdAd = new ArrayList<>();
    OpenAppCallback openAppCallback = new OpenAppCallback();
    LoadingAdsDialog dialog;
    Activity currentActivity;

    public OpenAppBuilder(@NonNull Activity activity) {
        this.currentActivity = activity;
        dialog = new LoadingAdsDialog(activity);
    }


    public List<String> getListIdAd() {
        return listIdAd;
    }

    public OpenAppBuilder setId(List<String> listId) {
        this.listIdAd.clear();
        this.listIdAd.addAll(listId);
        return this;
    }

    public OpenAppBuilder setCallback(OpenAppCallback callback) {
        this.openAppCallback = callback;
        return this;
    }

    public OpenAppCallback getCallback() {
        return openAppCallback;
    }

    public AdRequest getAdNewRequest() {
        return new AdRequest.Builder().build();
    }


    public void showLoading() {
        if (dialog != null && !dialog.isShowing())
            dialog.show();
    }

    public void dismissLoading() {
        if (dialog != null && dialog.isShowing())
            dialog.dismiss();
    }
}
