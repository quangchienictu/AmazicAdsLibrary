package com.amazic.ads.util;

import android.content.Context;
import android.content.SharedPreferences;

public class SharePreferenceHelper {
    public static int getInt(Context context, String name_config, int defaultValue) {
        SharedPreferences pre = context.getSharedPreferences("MY_PRE", Context.MODE_PRIVATE);
        return pre.getInt(name_config, defaultValue);
    }

    public static void setInt(Context context, String name_config, int config) {
        SharedPreferences pre = context.getSharedPreferences("MY_PRE", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pre.edit();
        editor.putInt(name_config, config);
        editor.apply();
    }
}
