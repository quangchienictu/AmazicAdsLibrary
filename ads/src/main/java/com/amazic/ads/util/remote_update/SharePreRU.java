package com.amazic.ads.util.remote_update;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;

public class SharePreRU {

    public static String getRemoteString(Context context) {
        SharedPreferences pre = context.getSharedPreferences("remote_fill", Context.MODE_PRIVATE);
        return pre.getString("remote_update", "");
    }

    public static void setRemoteString(Context context, String config) {
        SharedPreferences pre = context.getSharedPreferences("remote_fill", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pre.edit();
        editor.putString("remote_update", config);
        editor.commit();
    }
}
