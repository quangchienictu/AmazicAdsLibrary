package com.amazic.ads.util.detect_test_ad;

public class DetectTestAd {
    public static DetectTestAd INSTANCE;

    private boolean showAds = false;
    private boolean isTestAd = false;

    public void setShowAds() {
        this.showAds = true;
    }

    public static DetectTestAd getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new DetectTestAd();
        }
        return INSTANCE;
    }

    public void detectedTestAd(boolean isTestAd) {
        this.isTestAd = isTestAd;
    }

    public boolean isTestAd() {
        return this.isTestAd && !this.showAds;
    }
}
