package com.drawingapps.tracedrawing.drawingsketch.drawingapps;

import androidx.annotation.NonNull;

import com.amazic.ads.billing.AppPurchase;
import com.amazic.ads.util.Admob;
import com.amazic.ads.util.AdsApplication;
import com.amazic.ads.util.AppOpenManager;

import java.util.ArrayList;
import java.util.List;

public class MyApplication extends AdsApplication {
    public static String PRODUCT_ID_MONTH = "android.test.purchased";
    @Override
    public void onCreate() {
        super.onCreate();
        Admob.getInstance().setTokenEventAdjust("p3w75z");
        AppOpenManager.getInstance().disableAppResumeWithActivity(Splash.class);
        List<String> listINAPId = new ArrayList<>();
        listINAPId.add(PRODUCT_ID_MONTH);
        List<String> listSubsId = new ArrayList<>();
        listSubsId.add(PRODUCT_ID_MONTH);
        AppPurchase.getInstance().initBilling(this, listINAPId, listSubsId);
    }

    @NonNull
    @Override
    public String getAppTokenAdjust() {
        return "n2j8vj5m59mo";
    }

    @NonNull
    @Override
    public String getFacebookID() {
        return "null";
    }

    @Override
    public Boolean buildDebug() {
        return true;
    }
}
