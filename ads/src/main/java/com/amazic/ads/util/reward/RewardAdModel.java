package com.amazic.ads.util.reward;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;

import com.adjust.sdk.Adjust;
import com.adjust.sdk.AdjustAdRevenue;
import com.adjust.sdk.AdjustConfig;
import com.amazic.ads.event.AdType;
import com.amazic.ads.event.FirebaseUtil;
import com.amazic.ads.service.AdmobApi;
import com.amazic.ads.util.Constant;
import com.amazic.ads.util.NetworkUtil;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdValue;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;

import java.util.ArrayList;
import java.util.List;

public class RewardAdModel {
    private static final String TAG = "RewardAdModel";
    @Status
    private int status = Status.IDLE;
    private RewardedAd mRewardedAd = null;
    private final String listAdIDName;

    public RewardAdModel(String listAdIDName) {
        this.listAdIDName = listAdIDName;
    }

    public void loadReward(Context context, RewardAdCallback callback) {
        if (status != Status.ON_LOADING) {
            status = Status.ON_LOADING;
            List<String> listID = new ArrayList<>(AdmobApi.getInstance().getListIDByName(listAdIDName));
            loadWithListID(context, listID, callback);
        }
    }

    public void loadAndShowReward(Context context, RewardAdCallback callback) {
        if (mRewardedAd == null) {
            loadReward(context, new RewardAdCallback() {
                @Override
                public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                    super.onAdFailedToLoad(loadAdError);
                    callback.onAdFailedToLoad(loadAdError);
                }

                @Override
                public void onAdLoaded(Boolean isSuccessful) {
                    super.onAdLoaded(isSuccessful);
                    callback.onAdLoaded(isSuccessful);
                    if (isSuccessful) {
                        showReward(context, callback);
                    } else {
                        callback.onNextAction();
                    }
                }
            });
        } else {
            showReward(context, callback);
        }
    }

    public boolean isCalledSuccessfulAd() {
        return mRewardedAd != null;
    }

    public String getListAdIDName() {
        return listAdIDName;
    }

    public void showReward(Context context, RewardAdCallback callback) {
        if (!NetworkUtil.isNetworkActive(context)) {
            status = Status.ON_DISMISS;
            if (callback != null) {
                callback.onAdFailedToShow(Constant.NO_INTERNET_ERROR);
                callback.onNextAction();
            }
            return;
        }
        if (mRewardedAd == null) {
            status = Status.ON_DISMISS;
            if (callback != null) {
                callback.onAdFailedToShow(Constant.AD_NOT_AVAILABLE_ERROR);
                callback.onNextAction();
            }
            return;
        }
        if (status != Status.ON_STARTING_SHOW) {
            status = Status.ON_STARTING_SHOW;
            mRewardedAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                @Override
                public void onAdImpression() {
                    super.onAdImpression();
                    callback.onAdImpression();
                }

                @Override
                public void onAdClicked() {
                    super.onAdClicked();
                    callback.onAdClicked();
                }

                @Override
                public void onAdShowedFullScreenContent() {
                    status = Status.ON_SHOWING;
                    mRewardedAd = null;
                    if (callback != null) {
                        callback.onAdShowed();
                    }
                    loadReward(context, callback);
                }

                @Override
                public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                    status = Status.ON_DISMISS;
                    mRewardedAd = null;
                    if (callback != null) {
                        callback.onAdFailedToShow(adError);
                        callback.onNextAction();
                    }
                }

                @Override
                public void onAdDismissedFullScreenContent() {
                    status = Status.ON_DISMISS;
                    if (callback != null) {
                        callback.onAdDismissed();
                        callback.onNextAction();
                    }
                }
            });
            if (context instanceof Activity) {
                ((Activity) context).runOnUiThread(() -> {
                    mRewardedAd.show((Activity) context, callback::onUserEarnedReward);
                });
            }else {
                callback.onAdFailedToShow(Constant.CONVERT_ACTIVITY_ERROR);
            }
        }
    }

    private void loadWithListID(Context context, List<String> listID, RewardAdCallback callback) {
        if (!NetworkUtil.isNetworkActive(context)) {
            status = Status.ON_LOADED;

            if (callback != null) {
                callback.onAdFailedToLoad(Constant.NO_INTERNET_ERROR);
                callback.onAdLoaded(false);
            }
            return;
        }
        if (listID.isEmpty()) {
            status = Status.ON_LOADED;
            if (callback != null) {
                callback.onAdFailedToLoad(Constant.AD_NOT_HAVE_ID);
                callback.onAdLoaded(false);
            }
            return;
        }
        Log.d(TAG, "loadWithListID: \nname: " + listAdIDName + " - id: " + listID.get(0));
        AdRequest adRequest = new AdRequest.Builder().build();
        RewardedAd.load(context, listID.get(0), adRequest, new RewardedAdLoadCallback() {
            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                super.onAdFailedToLoad(loadAdError);
                if (listID.isEmpty()) {
                    status = Status.ON_LOADED;
                    if (callback != null) {
                        callback.onAdFailedToLoad(loadAdError);
                        callback.onAdLoaded(false);
                    }
                } else {
                    listID.remove(0);
                    loadWithListID(context, listID, callback);
                }
            }

            @Override
            public void onAdLoaded(@NonNull RewardedAd rewardedAd) {
                super.onAdLoaded(rewardedAd);
                status = Status.ON_LOADED;
                mRewardedAd = rewardedAd;
                rewardedAd.setOnPaidEventListener(adValue -> {
                    FirebaseUtil.logPaidAdImpression(context,
                            adValue,
                            rewardedAd.getAdUnitId(),
                            AdType.REWARDED);
                    trackRevenue(adValue);
                });
                if (callback != null) {
                    callback.onAdLoaded(true);
                }
            }
        });
    }

    //push adjust
    private void trackRevenue(AdValue adValue) {
        String adName = mRewardedAd.getResponseInfo().getLoadedAdapterResponseInfo().getAdSourceName().toString();
        double valueMicros = adValue.getValueMicros() / 1000000d;
        Log.d("AdjustRevenue", "adName: " + adName + " - valueMicros: " + valueMicros);
        // send ad revenue info to Adjust
        AdjustAdRevenue adRevenue = new AdjustAdRevenue(AdjustConfig.AD_REVENUE_ADMOB);
        adRevenue.setRevenue(valueMicros, adValue.getCurrencyCode());
        adRevenue.setAdRevenueNetwork(adName);
        Adjust.trackAdRevenue(adRevenue);
    }

    @IntDef({Status.IDLE, Status.ON_LOADING, Status.ON_LOADED, Status.ON_STARTING_SHOW, Status.ON_SHOWING, Status.ON_DISMISS})
    public @interface Status {
        int IDLE = 0;
        int ON_LOADING = 1;
        int ON_LOADED = 2;
        int ON_STARTING_SHOW = 3;
        int ON_SHOWING = 4;
        int ON_DISMISS = 5;
    }
}
