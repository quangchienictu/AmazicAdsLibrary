package com.amazic.ads.util.manager.open_app;

import android.app.Application;

import com.google.android.gms.ads.AdRequest;

import java.util.ArrayList;
import java.util.List;

public class OpenAppBuilder {
    List<String> listIdAd = new ArrayList<>();
    OpenAppCallback openAppCallback = new OpenAppCallback();
    Application application;

    public OpenAppBuilder(Application application) {
        this.application = application;
    }

    public Application getApplication() {
        return application;
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
}
