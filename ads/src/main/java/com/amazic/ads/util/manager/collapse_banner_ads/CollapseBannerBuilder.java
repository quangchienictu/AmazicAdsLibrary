package com.amazic.ads.util.manager.collapse_banner_ads;

import com.amazic.ads.callback.BannerCallBack;
import com.amazic.ads.service.AdmobApi;

import java.util.ArrayList;
import java.util.List;

public class CollapseBannerBuilder {
    private BannerCallBack callBack = new BannerCallBack();
    private final List<String> listId = new ArrayList<>();
    private String bannerGravity = "bottom";
    private String collapseTypeClose = CollapseBannerHelper.COUNT_DOWN;
    private long valueCountDownOrCountClick = 1;

    public CollapseBannerBuilder() {
    }

    public void setCollapseTypeClose(String collapseTypeClose) {
        this.collapseTypeClose = collapseTypeClose;
    }

    public String getCollapseTypeClose() {
        return this.collapseTypeClose;
    }

    public void setValueCountDownOrCountClick(long valueCountDownOrCountClick) {
        this.valueCountDownOrCountClick = valueCountDownOrCountClick;
    }

    public long getValueCountDownOrCountClick() {
        return this.valueCountDownOrCountClick;
    }
    public void setBannerGravity(String bannerGravity) {
        this.bannerGravity = bannerGravity;
    }

    public String getBannerGravity() {
        return this.bannerGravity;
    }

    public CollapseBannerBuilder setListId(List<String> listId) {
        this.listId.clear();
        this.listId.addAll(listId);
        return this;
    }

    public CollapseBannerBuilder setCallBack(BannerCallBack callBack) {
        this.callBack = callBack;
        return this;
    }

    public CollapseBannerBuilder isIdApi() {
        this.listId.clear();
        this.listId.addAll(AdmobApi.getInstance().getListIDCollapseBannerAll());
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