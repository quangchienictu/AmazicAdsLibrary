package com.amazic.ads.util.remote_update;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.amazic.ads.R;
import com.google.firebase.FirebaseApp;
import com.google.firebase.remoteconfig.ConfigUpdate;
import com.google.firebase.remoteconfig.ConfigUpdateListener;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigException;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;

public class UpdateRemoteConfig {
    private static final String TAG = "UpdateRemoteConfig";

    public static void init(@NonNull Context context) {
        FirebaseApp.initializeApp(context);
        FirebaseRemoteConfig mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        initFirebase(context, mFirebaseRemoteConfig);
        mFirebaseRemoteConfig.addOnConfigUpdateListener(new ConfigUpdateListener() {
            @Override
            public void onUpdate(@NonNull ConfigUpdate configUpdate) {
                fetchData(context, mFirebaseRemoteConfig);
            }

            @Override
            public void onError(@NonNull FirebaseRemoteConfigException error) {
                Log.e(TAG, "onError: ", error);
            }
        });
    }

    private static void initFirebase(@NonNull Context context, FirebaseRemoteConfig mFirebaseRemoteConfig) {
        mFirebaseRemoteConfig.reset();
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder().setMinimumFetchIntervalInSeconds(3600).build();
        mFirebaseRemoteConfig.setConfigSettingsAsync(configSettings);
        mFirebaseRemoteConfig.setDefaultsAsync(R.xml.remote_config_defaults);
        fetchData(context, mFirebaseRemoteConfig);
    }

    private static void fetchData(@NonNull Context context, FirebaseRemoteConfig mFirebaseRemoteConfig) {
        mFirebaseRemoteConfig.fetchAndActivate().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                getRemoteConfigString(mFirebaseRemoteConfig, context);
            }
            Log.d(TAG, "init: " + SharePreRU.getRemoteString(context));
            JsonRemoteUtils.jsonToMap(SharePreRU.getRemoteString(context));
        });
    }

    private static void getRemoteConfigString(FirebaseRemoteConfig mFirebaseRemoteConfig, Context context) {
        String value = mFirebaseRemoteConfig.getString("remote_update");
        SharePreRU.setRemoteString(context, value);
    }
}
