package com.amazic.ads.callback;

public interface PurchaseListioner {
    void onProductPurchased(String productId, String transactionDetails);
    void displayErrorMessage(String errorMsg );
    void onUserCancelBilling( );
}
