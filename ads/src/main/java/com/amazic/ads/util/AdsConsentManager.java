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

    private ConsentInformation consentInformation;
    private Activity activity;
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
        consentInformation = UserMessagingPlatform.getConsentInformation(activity);
        if (resetData) {
            consentInformation.reset();
        }

        ConsentInformation.OnConsentInfoUpdateSuccessListener onConsentInfoUpdateSuccessListener = new ConsentInformation.OnConsentInfoUpdateSuccessListener() {
            @Override
            public void onConsentInfoUpdateSuccess() {
                UserMessagingPlatform.loadAndShowConsentFormIfRequired(
                        activity,
                        loadAndShowError -> {
                            if (loadAndShowError != null) {
                                // Consent gathering failed.
                                Log.w("TAG", String.format("%s: %s",
                                        loadAndShowError.getErrorCode(),
                                        loadAndShowError.getMessage()));
                                Toast.makeText(activity, "loadAndShowError", Toast.LENGTH_SHORT).show();
                            }

                            if (!auAtomicBoolean.getAndSet(true)) {
                                umpResultListener.onCheckUMPSuccess(getConsentResult(activity));
                                Toast.makeText(activity, "initAdsNetwork-Success", Toast.LENGTH_SHORT).show();
                            }
                        }
                );
            }
        };

        ConsentInformation.OnConsentInfoUpdateFailureListener onConsentInfoUpdateFailureListener = new ConsentInformation.OnConsentInfoUpdateFailureListener() {
            @Override
            public void onConsentInfoUpdateFailure(@NonNull FormError formError) {
                if (!auAtomicBoolean.getAndSet(true)) {
                    umpResultListener.onCheckUMPSuccess(getConsentResult(activity));
                    Toast.makeText(activity, "initAdsNetwork-Failure", Toast.LENGTH_SHORT).show();
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
        if (consentInformation.canRequestAds() && auAtomicBoolean.getAndSet(true)) {
            Toast.makeText(activity, "initializeMobileAdsSdk2", Toast.LENGTH_SHORT).show();
        }
    }

    public void showPrivacyOption(Activity activity, UMPResultListener umpResultListener) {
        UserMessagingPlatform.showPrivacyOptionsForm(activity, (formError) -> {
            if (formError != null) {
                Log.d("TAG", "showPrivacyOption: " + formError.getMessage() + " - Code" + formError.getErrorCode());
            }

            if (getConsentResult(activity)) {
                Toast.makeText(activity, "initAdsNetwork-PrivacyOption", Toast.LENGTH_SHORT).show();
            }

            UMPResultListener var10000 = umpResultListener;
            var10000.onCheckUMPSuccess(getConsentResult(activity));
        });
    }
}
