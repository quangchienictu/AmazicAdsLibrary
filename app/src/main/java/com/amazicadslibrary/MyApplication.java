package com.amazicadslibrary;

import com.amazic.ads.billing.AppPurchase;
import com.amazic.ads.util.AppOpenManager;
import com.amazic.ads.util.AdsApplication;

import java.util.List;

public class MyApplication extends AdsApplication {
    public static String PRODUCT_ID_YEAR = "android.test.purchased";
    public static String PRODUCT_ID_MONTH = "android.test.purchased";
    @Override
    public void onCreate() {
        super.onCreate();
        AppOpenManager.getInstance().disableAppResumeWithActivity(Splash.class);
        //test
        AppPurchase.getInstance().initBilling(this);
        AppPurchase.getInstance().addProductId(PRODUCT_ID_MONTH);
        AppPurchase.getInstance().addProductId(PRODUCT_ID_YEAR);
         AppPurchase.getInstance().setDiscount(0.5); // giảm giá 50%

        //real
        /*AppPurchase.getInstance().addSubcriptionId(Constants.PRODUCT_ID_MONTH);
        AppPurchase.getInstance().addSubcriptionId(Constants.PRODUCT_ID_YEAR);
        AppPurchase.getInstance().setDiscount(0.5); // giảm giá 50%*/
    }

    @Override
    public boolean enableAdsResume() {
        return true;
    }

    @Override
    public List<String> getListTestDeviceId() {
        return null;
    }

    @Override
    public String getResumeAdId() {
        return getString(R.string.admod_app_open_ad_id);
    }
}
