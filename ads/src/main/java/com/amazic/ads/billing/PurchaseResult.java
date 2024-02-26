package com.amazic.ads.billing;

import java.util.List;

public class PurchaseResult {
    private String orderId;
    private String packageName;
    private List<String> productId;
    private long purchaseTime;
    private int purchaseState;
    private String purchaseToken;
    private int quantity;
    private boolean autoRenewing;
    private boolean acknowledged;

    public PurchaseResult(String packageName, List<String> productId, int purchaseState, boolean autoRenewing) {
        this.packageName = packageName;
        this.productId = productId;
        this.purchaseState = purchaseState;
        this.autoRenewing = autoRenewing;
    }

    public PurchaseResult(String orderId, String packageName, List<String> productId, long purchaseTime,
                          int purchaseState, String purchaseToken, int quantity, boolean autoRenewing, boolean acknowledged) {
        this.orderId = orderId;
        this.packageName = packageName;
        this.productId = productId;
        this.purchaseTime = purchaseTime;
        this.purchaseState = purchaseState;
        this.purchaseToken = purchaseToken;
        this.quantity = quantity;
        this.autoRenewing = autoRenewing;
        this.acknowledged = acknowledged;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public List<String> getProductId() {
        return productId;
    }

    public void setProductId(List<String> productId) {
        this.productId = productId;
    }

    public int getPurchaseState() {
        return purchaseState;
    }

    public void setPurchaseState(int purchaseState) {
        this.purchaseState = purchaseState;
    }

    public boolean isAutoRenewing() {
        return autoRenewing;
    }

    public void setAutoRenewing(boolean autoRenewing) {
        this.autoRenewing = autoRenewing;
    }

}
