package com.amazic.ads.organic;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.amazic.ads.util.Admob;
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

    public static TechManager getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new TechManager();
        }
        return INSTANCE;
    }

    public void getResult(Context context, long interInterval, String adjustKey, OnCheckResultCallback onCheckResultCallback) {
        getGAID(context, interInterval, adjustKey, onCheckResultCallback);
    }

    private void getGAID(Context context, long interInterval, String adjustKey, OnCheckResultCallback onCheckResultCallback) {
        executorService.execute(() -> {
            AdvertisingIdClient.Info idInfo;
            try {
                idInfo = AdvertisingIdClient.getAdvertisingIdInfo(context.getApplicationContext());
                advertId = idInfo.getId();
                Log.d(TAG, "getGAID: " + advertId);
            } catch (GooglePlayServicesNotAvailableException |
                     GooglePlayServicesRepairableException | IOException | NullPointerException e) {
                e.printStackTrace();
            }
            handler.post(() -> getAdjustResponse(adjustKey, advertId, new OnResponseCallback() {
                @Override
                public void onResponse(String result) {
                    if (result.equals(Constant.keyCheck)) {
                        if (interInterval > 0) {
                            Admob.getInstance().setTimeInterval(interInterval);
                        }
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
                Constant.apiKey
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
