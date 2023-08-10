package com.amazic.ads.service;

public class AdsModel {
    int id;
    String app_id;
    String name;
    String ads_id;

    public AdsModel(int id, String app_id, String name, String ads_id) {
        this.id = id;
        this.app_id = app_id;
        this.name = name;
        this.ads_id = ads_id;
    }

    public int getId() {return id;}
    public String getName() {
        return name;
    }
    public String getAds_id() {
        return ads_id;
    }
}
