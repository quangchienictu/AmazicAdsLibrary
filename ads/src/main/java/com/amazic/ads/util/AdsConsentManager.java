package com.amazic.ads.util;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.ump.ConsentDebugSettings;
import com.google.android.ump.ConsentInformation;
import com.google.android.ump.ConsentRequestParameters;
import com.google.android.ump.FormError;
import com.google.android.ump.UserMessagingPlatform;

import java.util.concurrent.atomic.AtomicBoolean;

public class AdsConsentManager {
    private static final String TAG = "AdsConsentManager";
    private final Activity activity;
    private final AtomicBoolean auAtomicBoolean;

    public interface UMPResultListener {
        public void onCheckUMPSuccess(boolean result);
    }

    public AdsConsentManager(Activity activity) {
        this.activity = activity;
        this.auAtomicBoolean = new AtomicBoolean(false);
    }

    public void requestUMP(UMPResultListener umpResultListener) {
        this.requestUMP(false, "", false, umpResultListener);
    }

    public static boolean getConsentResult(Context context) {
        String consentResult = context.getSharedPreferences(context.getPackageName() + "_preferences", 0).getString("IABTCF_PurposeConsents", "");
        return consentResult.isEmpty() || String.valueOf(consentResult.charAt(0)).equals("1");
    }

    public void requestUMP(Boolean enableDebug, String testDevice, Boolean resetData, UMPResultListener umpResultListener) {
        ConsentDebugSettings debugSettings = new ConsentDebugSettings.Builder(activity)
                .setDebugGeography(ConsentDebugSettings.DebugGeography.DEBUG_GEOGRAPHY_EEA)
                .addTestDeviceHashedId(testDevice)
                .build();
        // Set tag for under age of consent. false means users are not under age
        // of consent.
        ConsentRequestParameters.Builder params = new ConsentRequestParameters.Builder();
        params.setTagForUnderAgeOfConsent(false);
        if (enableDebug) {
            params.setConsentDebugSettings(debugSettings);
        }
        ConsentRequestParameters consentRequestParameters = params.build();
        ConsentInformation consentInformation = UserMessagingPlatform.getConsentInformation(activity);
        if (resetData) {
            consentInformation.reset();
        }

        ConsentInformation.OnConsentInfoUpdateSuccessListener onConsentInfoUpdateSuccessListener = () -> UserMessagingPlatform.loadAndShowConsentFormIfRequired(
                activity,
                loadAndShowError -> {
                    if (loadAndShowError != null)
                        Log.e(TAG, "onConsentInfoUpdateSuccess: " + loadAndShowError.getMessage());
                    if (!auAtomicBoolean.getAndSet(true)) {
                        umpResultListener.onCheckUMPSuccess(getConsentResult(activity));
                    }
                }
        );

        ConsentInformation.OnConsentInfoUpdateFailureListener onConsentInfoUpdateFailureListener = new ConsentInformation.OnConsentInfoUpdateFailureListener() {
            @Override
            public void onConsentInfoUpdateFailure(@NonNull FormError formError) {
                if (!auAtomicBoolean.getAndSet(true)) {
                    Log.e(TAG, "onConsentInfoUpdateFailure: " + formError.getMessage());
                    umpResultListener.onCheckUMPSuccess(getConsentResult(activity));
                }
            }
        };

        consentInformation.requestConsentInfoUpdate(
                activity,
                consentRequestParameters,
                onConsentInfoUpdateSuccessListener,
                onConsentInfoUpdateFailureListener);

        // Check if you can initialize the Google Mobile Ads SDK in parallel
        // while checking for new consent information. Consent obtained in
        // the previous session can be used to request ads.
        /*if (consentInformation.canRequestAds() && !auAtomicBoolean.getAndSet(true)) {
            umpResultListener.onCheckUMPSuccess(getConsentResult(activity));
            Log.d(TAG, "requestUMP: ");
        }*/
    }

    public void showPrivacyOption(Activity activity, UMPResultListener umpResultListener) {
        UserMessagingPlatform.showPrivacyOptionsForm(activity, (formError) -> {
            Log.d(TAG, "showPrivacyOption: " + getConsentResult(activity));

            UMPResultListener var10000 = umpResultListener;
            var10000.onCheckUMPSuccess(getConsentResult(activity));
        });
    }
}
