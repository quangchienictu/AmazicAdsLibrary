package com.amazic.ads.util.remote_update;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class JsonRemoteUtils {
    private static final String TAG = "JsonUtils";
    private static Map<String, Boolean> remoteUpdate = new HashMap<>();

    public static void jsonToMap(String jsonString) {
        Map<String, Boolean> resultMap = new HashMap<>();

        try {
            JSONArray jsonArray = new JSONArray(jsonString);

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String name = jsonObject.getString("name");
                Boolean apply = jsonObject.get("apply") instanceof Boolean && jsonObject.getBoolean("apply");
                resultMap.put(name, apply);
            }
            Log.d(TAG, "jsonToMap: done");
        } catch (JSONException e) {
            Log.e(TAG, "jsonToMap: Fail\n", e);
        }
        remoteUpdate = resultMap;
    }

    public static boolean checkRemote(String name) {
        return remoteUpdate.get(name) != null && Boolean.TRUE.equals(remoteUpdate.get(name));
    }
}

