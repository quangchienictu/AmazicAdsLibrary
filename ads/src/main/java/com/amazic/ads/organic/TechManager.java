package com.amazic.ads.organic;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.amazic.ads.BuildConfig;
import com.amazic.ads.util.AdsConsentManager;
import com.amazic.ads.util.NetworkUtil;
import com.google.android.gms.ads.identifier.AdvertisingIdClient;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TechManager {
    private String advertId = "";
    private ExecutorService executorService = Executors.newSingleThreadExecutor();
    private Handler handler = new Handler(Looper.getMainLooper());
    public static TechManager INSTANCE;
    public String TAG = "TechManager";
    public String CALLED_API = "calledApi";

    public static TechManager getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new TechManager();
        }
        return INSTANCE;
    }

    private void detectedTech(Context context, boolean isDetected) {
        SharedPreferences.Editor editor = context.getSharedPreferences("MY_PRE", Context.MODE_PRIVATE).edit();
        editor.putBoolean(TAG, isDetected);
        editor.apply();
        Log.d(TAG, "detectedTech: " + isDetected);
    }

    public void calledApi(Context context) {
        SharedPreferences.Editor editor = context.getSharedPreferences("MY_PRE", Context.MODE_PRIVATE).edit();
        editor.putBoolean(CALLED_API, true);
        editor.apply();
        Log.d(TAG, "calledApi.");
    }

    public boolean isCalledApi(Context context) {
        Log.d(TAG, "calledApi: " + context.getSharedPreferences("MY_PRE", Context.MODE_PRIVATE).getBoolean(CALLED_API, false));
        return context.getSharedPreferences("MY_PRE", Context.MODE_PRIVATE).getBoolean(CALLED_API, false);
    }

    public boolean isTech(Context context) {
        Log.d(TAG, "isTech: " + context.getSharedPreferences("MY_PRE", Context.MODE_PRIVATE).getBoolean(TAG, false));
        return context.getSharedPreferences("MY_PRE", Context.MODE_PRIVATE).getBoolean(TAG, false);
    }

    public void getResult(boolean isDebug, Context context, String adjustKey, OnCheckResultCallback onCheckResultCallback) {
        if (isDebug) {
            onCheckResultCallback.onResult(false);
        } else {
            if (isTech(context)) {
                Log.d(TAG, "getResult: isTech = " + isTech(context));
                onCheckResultCallback.onResult(true);
            } else {
                Log.d(TAG, "getResult: isTech = " + isTech(context));
                if (isCalledApi(context)) {
                    onCheckResultCallback.onResult(false);
                } else {
                    if (NetworkUtil.isNetworkActive(context)) {
                        getGAID(context, adjustKey, onCheckResultCallback);
                    } else {
                        onCheckResultCallback.onResult(false);
                    }
                }
                Log.d(TAG, "getResult: isCalledApi = " + isCalledApi(context));
            }
        }
    }

    public void getResult(Context context, String adjustKey, OnCheckResultCallback onCheckResultCallback) {
        if (isTech(context)) {
            onCheckResultCallback.onResult(true);
            Log.d(TAG, "getResult1: " + isTech(context));
        } else {
            getGAID(context, adjustKey, onCheckResultCallback);
            Log.d(TAG, "getResult2: " + isTech(context));
        }
    }

    private void getGAID(Context context, String adjustKey, OnCheckResultCallback onCheckResultCallback) {
        executorService.execute(() -> {
            AdvertisingIdClient.Info idInfo;
            try {
                idInfo = AdvertisingIdClient.getAdvertisingIdInfo(context.getApplicationContext());
                advertId = idInfo.getId();
                Log.d(TAG, "getGAID: " + advertId);
            } catch (GooglePlayServicesNotAvailableException |
                     GooglePlayServicesRepairableException | IOException | NullPointerException e) {
                e.printStackTrace();
                Log.d(TAG, "getGAID fail");
            }
            handler.post(() -> getAdjustResponse(adjustKey, advertId, new OnResponseCallback() {
                @Override
                public void onResponse(String result) {
                    //set called api = true
                    calledApi(context);
                    //end
                    Log.d(TAG, "onResponse " + result);
                    if (result.equals(Constant.keyCheck)) {
                        detectedTech(context, true);
                    } else {
                        detectedTech(context, false);
                    }
                    onCheckResultCallback.onResult(result.equals(Constant.keyCheck));
                }
            }));
        });
    }

    private void getAdjustResponse(String adjustKey, String advertId, OnResponseCallback onResponse) {
        ApiServiceAdjust apiServiceAdjust = RetrofitClientAdjust.createService();
        Call<AdjustOutputModel> callAdjust = apiServiceAdjust.sendData(
                advertId,
                adjustKey,
                BuildConfig.apiKey
        );
        callAdjust.enqueue(new Callback<AdjustOutputModel>() {
            @Override
            public void onResponse(Call<AdjustOutputModel> call, Response<AdjustOutputModel> response) {
                if (response.isSuccessful()) {
                    AdjustOutputModel responseData = response.body();
                    if (responseData != null && responseData.getTrackerName() != null) {
                        Log.d(TAG, "onResponse Success" + responseData.getTrackerName());
                        onResponse.onResponse(responseData.getTrackerName());
                    } else {
                        Log.d(TAG, "onResponse Success null");
                        onResponse.onResponse("");
                    }
                } else {
                    Log.d(TAG, "onResponse not Success" + response.message());
                    onResponse.onResponse("");
                }
            }

            @Override
            public void onFailure(Call<AdjustOutputModel> call, Throwable t) {
                Log.d(TAG, "onFailure" + t.getMessage());
                onResponse.onResponse("");
            }
        });
    }

    interface OnResponseCallback {
        void onResponse(String result);
    }

    public interface OnCheckResultCallback {
        void onResult(Boolean result);
    }
}
