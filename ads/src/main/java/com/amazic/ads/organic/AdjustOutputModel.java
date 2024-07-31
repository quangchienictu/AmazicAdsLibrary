package com.amazic.ads.organic;

public class AdjustOutputModel {
    private String Adid;
    private String AdvertisingId;
    private String Tracker;
    private String TrackerName;
    private String FirstTracker;
    private String FirstTrackerName;
    private String ClickTime;
    private String InstallTime;
    private String LastAppVersion;
    private String LastAppVersionShort;
    private String LastSessionTime;
    private String PushToken;
    private String State;
    private String InstallState;
    private String SignatureAcceptanceStatus;
    private String SignatureVerificationResult;

    public AdjustOutputModel() {

    }

    public AdjustOutputModel(String adid, String advertisingId, String tracker, String trackerName, String firstTracker, String firstTrackerName, String clickTime, String installTime, String lastAppVersion, String lastAppVersionShort, String lastSessionTime, String pushToken, String state, String installState, String signatureAcceptanceStatus, String signatureVerificationResult) {
        Adid = adid;
        AdvertisingId = advertisingId;
        Tracker = tracker;
        TrackerName = trackerName;
        FirstTracker = firstTracker;
        FirstTrackerName = firstTrackerName;
        ClickTime = clickTime;
        InstallTime = installTime;
        LastAppVersion = lastAppVersion;
        LastAppVersionShort = lastAppVersionShort;
        LastSessionTime = lastSessionTime;
        PushToken = pushToken;
        State = state;
        InstallState = installState;
        SignatureAcceptanceStatus = signatureAcceptanceStatus;
        SignatureVerificationResult = signatureVerificationResult;
    }

    public String getAdid() {
        return Adid;
    }

    public void setAdid(String adid) {
        Adid = adid;
    }

    public String getAdvertisingId() {
        return AdvertisingId;
    }

    public void setAdvertisingId(String advertisingId) {
        AdvertisingId = advertisingId;
    }

    public String getTracker() {
        return Tracker;
    }

    public void setTracker(String tracker) {
        Tracker = tracker;
    }

    public String getTrackerName() {
        return TrackerName;
    }

    public void setTrackerName(String trackerName) {
        TrackerName = trackerName;
    }

    public String getFirstTracker() {
        return FirstTracker;
    }

    public void setFirstTracker(String firstTracker) {
        FirstTracker = firstTracker;
    }

    public String getFirstTrackerName() {
        return FirstTrackerName;
    }

    public void setFirstTrackerName(String firstTrackerName) {
        FirstTrackerName = firstTrackerName;
    }

    public String getClickTime() {
        return ClickTime;
    }

    public void setClickTime(String clickTime) {
        ClickTime = clickTime;
    }

    public String getInstallTime() {
        return InstallTime;
    }

    public void setInstallTime(String installTime) {
        InstallTime = installTime;
    }

    public String getLastAppVersion() {
        return LastAppVersion;
    }

    public void setLastAppVersion(String lastAppVersion) {
        LastAppVersion = lastAppVersion;
    }

    public String getLastAppVersionShort() {
        return LastAppVersionShort;
    }

    public void setLastAppVersionShort(String lastAppVersionShort) {
        LastAppVersionShort = lastAppVersionShort;
    }

    public String getLastSessionTime() {
        return LastSessionTime;
    }

    public void setLastSessionTime(String lastSessionTime) {
        LastSessionTime = lastSessionTime;
    }

    public String getPushToken() {
        return PushToken;
    }

    public void setPushToken(String pushToken) {
        PushToken = pushToken;
    }

    public String getState() {
        return State;
    }

    public void setState(String state) {
        State = state;
    }

    public String getInstallState() {
        return InstallState;
    }

    public void setInstallState(String installState) {
        InstallState = installState;
    }

    public String getSignatureAcceptanceStatus() {
        return SignatureAcceptanceStatus;
    }

    public void setSignatureAcceptanceStatus(String signatureAcceptanceStatus) {
        SignatureAcceptanceStatus = signatureAcceptanceStatus;
    }

    public String getSignatureVerificationResult() {
        return SignatureVerificationResult;
    }

    public void setSignatureVerificationResult(String signatureVerificationResult) {
        SignatureVerificationResult = signatureVerificationResult;
    }
}

