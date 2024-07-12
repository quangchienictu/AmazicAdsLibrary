package com.amazic.ads.util.manager.banner;

import android.app.Activity;

import androidx.lifecycle.LifecycleOwner;

import com.amazic.ads.callback.BannerCallBack;
import com.amazic.ads.service.AdmobApi;

import java.util.ArrayList;
import java.util.List;

public class BannerBuilder {
    private BannerCallBack callBack = new BannerCallBack();
    private final List<String> listId = new ArrayList<>();

    public BannerBuilder() {
    }

    public BannerBuilder setListId(List<String> listId) {
        this.listId.clear();
        this.listId.addAll(listId);
        return this;
    }

    public BannerBuilder setCallBack(BannerCallBack callBack) {
        this.callBack = callBack;
        return this;
    }

    public BannerBuilder isIdApi() {
        this.listId.clear();
        this.listId.addAll(AdmobApi.getInstance().getListIDBannerAll());
        return this;
    }

    public void setListIdAd(String nameIdAd) {
        this.listId.clear();
        this.listId.addAll(AdmobApi.getInstance().getListIDByName(nameIdAd));
    }

    public BannerCallBack getCallBack() {
        return callBack;
    }

    public List<String> getListId() {
        return listId;
    }
}