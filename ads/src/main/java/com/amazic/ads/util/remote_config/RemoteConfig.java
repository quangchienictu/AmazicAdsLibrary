package com.amazic.ads.util.remote_config;


import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigValue;

import java.util.Map;

public class RemoteConfig {
    private static final String TAG = "RemoteConfigLog";
    private static RemoteConfig INSTANCE = null;
    private final MutableLiveData<Boolean> isFinishedCallRemote = new MutableLiveData<>(false);

    public static RemoteConfig getInstance() {
        if (INSTANCE == null)
            INSTANCE = new RemoteConfig();
        return INSTANCE;
    }

    public void initFirebaseConfig(Context context, boolean isSetUp) {
        Log.d(TAG, "isSetUp: " + isSetUp);
        if (isSetUp) {
            FirebaseRemoteConfig remoteConfig = FirebaseRemoteConfig.getInstance();
            remoteConfig.reset();
            FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                    .setMinimumFetchIntervalInSeconds(3600)
                    .build();
            remoteConfig.setConfigSettingsAsync(configSettings);
            remoteConfig.fetchAndActivate().addOnCompleteListener(task -> {
                Log.d(TAG, "initFirebaseConfig: ");
                if (task.getResult() != null && task.getResult()) {
                    isFinishedCallRemote.setValue(false);
                    fetchDataRemote(context);
                }
                isFinishedCallRemote.postValue(true);
            });
        } else {
            isFinishedCallRemote.postValue(true);
        }
    }

    private void fetchDataRemote(Context context) {
        Log.d(TAG, "fetchDataRemote: ");
        FirebaseRemoteConfig remoteConfig = FirebaseRemoteConfig.getInstance();
        Map<String, FirebaseRemoteConfigValue> allValues = remoteConfig.getAll();
        for (String key : allValues.keySet()) {
            FirebaseRemoteConfigValue value = allValues.get(key);
            if (value != null) {
                SharePreRemoteConfig.setConfig(context, key, value.asString());
            }
        }
    }

    public void onRemoteConfigFetched(@NonNull LifecycleOwner owner, @NonNull OnCompleteListener listener) {
        isFinishedCallRemote.observe(owner, isFinish -> {
            if (isFinish) {
                listener.onComplete();
            }
        });
    }

    public interface OnCompleteListener {
        void onComplete();
    }
}