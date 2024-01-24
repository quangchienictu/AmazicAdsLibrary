package com.amazic.ads.util.remote_config;

import android.content.Context;
import android.content.SharedPreferences;

public class SharePreRemoteConfig {
    private static final String NAME_SHARE_PRE_REMOTE_CONFIG = "NAME_SHARE_PRE_REMOTE_CONFIG";

    public static void setConfig(Context context, String key, String value) {
        SharedPreferences pre = context.getSharedPreferences(NAME_SHARE_PRE_REMOTE_CONFIG, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pre.edit();
        editor.putString(key, value);
        editor.commit();
    }

    public static String getConfigString(Context context, String key) {
        SharedPreferences pre = context.getSharedPreferences(NAME_SHARE_PRE_REMOTE_CONFIG, Context.MODE_PRIVATE);
        return pre.getString(key, "");
    }

    public static Boolean getConfigBoolean(Context context, String key) {
        return Boolean.valueOf(getConfigString(context, key));
    }

    public static Float getConfigFloat(Context context, String key) {
        try {
            return Float.valueOf(getConfigString(context, key));
        } catch (NumberFormatException e) {
            return 0.0f;
        }
    }

    public static Integer getConfigInt(Context context, String key) {
        try {
            String value = getConfigString(context, key);
            if (value.contains("."))
                return Math.round(Float.parseFloat(value));
            else
                return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
