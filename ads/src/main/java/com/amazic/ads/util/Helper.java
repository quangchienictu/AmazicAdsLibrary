package com.amazic.ads.util;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

public class Helper {
    private static final String FILE_SETTING = "setting.pref";
    private static final String FILE_SETTING_Admob = "setting_Admob.pref";
    private static final String IS_FIRST_OPEN = "IS_FIRST_OPEN";
    private static final String KEY_FIRST_TIME = "KEY_FIRST_TIME";

  
    /**
     * Trả về số click của 1 ads nào đó
     *
     * @param context
     * @param idAds
     * @return
     */
    public static int getNumClickAdsPerDay(Context context, String idAds) {
        return context.getSharedPreferences(FILE_SETTING_Admob, Context.MODE_PRIVATE).getInt(idAds, 0);
    }


    /**
     * nếu lần đầu mở app lưu thời gian đầu tiên vào SharedPreferences
     * nếu thời gian hiện tại so với thời gian đầu được 1 ngày thì reset lại data của Admob.
     *
     * @param context
     */
    public static void setupAdmobData(Context context) {
        if (isFirstOpenApp(context)) {
            context.getSharedPreferences(FILE_SETTING_Admob, Context.MODE_PRIVATE).edit().putLong(KEY_FIRST_TIME, System.currentTimeMillis()).apply();
            context.getSharedPreferences(FILE_SETTING, Context.MODE_PRIVATE).edit().putBoolean(IS_FIRST_OPEN, true).apply();
            return;
        }
        long firstTime = context.getSharedPreferences(FILE_SETTING_Admob, Context.MODE_PRIVATE).getLong(KEY_FIRST_TIME, System.currentTimeMillis());
        long rs = System.currentTimeMillis() - firstTime;
       /*
       qua q ngày reset lại data
        */
        if (rs >= 24 * 60 * 60 * 1000) {
            resetAdmobData(context);
        }
    }


    private static void resetAdmobData(Context context) {
        context.getSharedPreferences(FILE_SETTING_Admob, Context.MODE_PRIVATE).edit().clear().apply();
        context.getSharedPreferences(FILE_SETTING_Admob, Context.MODE_PRIVATE).edit().putLong(KEY_FIRST_TIME, System.currentTimeMillis()).apply();
    }

    private static boolean isFirstOpenApp(Context context) {
        return context.getSharedPreferences(FILE_SETTING, Context.MODE_PRIVATE).getBoolean(IS_FIRST_OPEN, false);
    }
}
