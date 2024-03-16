package com.amazic.ads.iap;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.ProductDetails;
import com.android.billingclient.api.ProductDetailsResponseListener;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesResponseListener;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.QueryProductDetailsParams;
import com.android.billingclient.api.QueryPurchasesParams;
import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IAPManager {
    private static final String TAG = "IAPManager";
    public static final String PRODUCT_ID_TEST = "android.test.purchased";
    private static IAPManager instance;
    private PurchasesUpdatedListener purchasesUpdatedListener;
    private BillingClient billingClient;
    public static String typeIAP = BillingClient.ProductType.INAPP, typeSub = BillingClient.ProductType.SUBS;
    private ArrayList<QueryProductDetailsParams.Product> listIAPProduct = new ArrayList<>();
    private ArrayList<QueryProductDetailsParams.Product> listSubProduct = new ArrayList<>();
    private List<ProductDetails> productDetailsListIAP = new ArrayList<>();
    private List<ProductDetails> productDetailsListSub = new ArrayList<>();
    final private Map<String, ProductDetails> productDetailsINAPMap = new HashMap<>();
    final private Map<String, ProductDetails> productDetailsSubsMap = new HashMap<>();
    private boolean isPurchase = false;
    private PurchaseCallback purchaseCallback;
    private boolean isPurchaseTest = false;

    public static IAPManager getInstance() {
        if (instance == null) {
            instance = new IAPManager();
        }
        return instance;
    }

    public boolean isPurchase() {
        return this.isPurchase;
    }

    public void setPurchase(boolean isPurchase) {
        this.isPurchase = isPurchase;
    }

    public void setPurchaseTest(boolean isPurchaseTest) {
        this.isPurchaseTest = isPurchaseTest;
    }

    public void setPurchaseListener(PurchaseCallback purchaseCallback) {
        this.purchaseCallback = purchaseCallback;
    }

    public void initBilling(Context context, ArrayList<ProductDetailCustom> listProductDetailCustoms, BillingCallback billingCallback) {
        setListProductDetails(listProductDetailCustoms);
        purchasesUpdatedListener = new PurchasesUpdatedListener() {
            @Override
            public void onPurchasesUpdated(@NonNull BillingResult billingResult, @Nullable List<Purchase> list) {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && list != null) {
                    for (Purchase purchase : list) {
                        handlePurchase(purchase);
                    }
                    Log.d(TAG, "onPurchasesUpdated OK");
                } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.USER_CANCELED) {
                    // Handle an error caused by a user cancelling the purchase flow.
                    purchaseCallback.onUserCancelBilling();
                    Log.d(TAG, "user cancelling the purchase flow");
                } else {
                    // Handle any other error codes.
                    Log.d(TAG, "onPurchasesUpdated:... ");
                }
            }
        };
        billingClient = BillingClient.newBuilder(context)
                .setListener(purchasesUpdatedListener)
                .enablePendingPurchases()
                .build();
        connectToGooglePlay(billingCallback);
    }

    private void setListProductDetails(ArrayList<ProductDetailCustom> listProductDetailCustoms) {
        //check case purchase test -> auto add id product test to list
        if (isPurchaseTest) {
            listIAPProduct.add(QueryProductDetailsParams.Product.newBuilder()
                    .setProductId(PRODUCT_ID_TEST)
                    .setProductType(typeIAP)
                    .build());
        }
        for (ProductDetailCustom productDetailCustom : listProductDetailCustoms) {
            if (productDetailCustom.getProductType().equals(typeIAP)) {
                listIAPProduct.add(QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(productDetailCustom.getProductId())
                        .setProductType(productDetailCustom.getProductType())
                        .build());
            } else if (productDetailCustom.getProductType().equals(typeSub)) {
                listSubProduct.add(QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(productDetailCustom.getProductId())
                        .setProductType(productDetailCustom.getProductType())
                        .build());
            }
        }
    }

    private void connectToGooglePlay(BillingCallback billingCallback) {
        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(BillingResult billingResult) {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    billingCallback.onBillingSetupFinished();
                    // The BillingClient is ready. You can query purchases here.
                    verifyPurchased();
                    showProductsAvailableToBuy(listIAPProduct, listSubProduct);
                    Log.d(TAG, "onBillingSetupFinished OK");
                }
            }

            @Override
            public void onBillingServiceDisconnected() {
                billingCallback.onBillingServiceDisconnected();
                // Try to restart the connection on the next request to
                // Google Play by calling the startConnection() method.
                Log.d(TAG, "onBillingServiceDisconnected");
            }
        });
    }

    private void showProductsAvailableToBuy(ArrayList<QueryProductDetailsParams.Product> listIAPProduct, ArrayList<QueryProductDetailsParams.Product> listSubProduct) {
        if (listIAPProduct.size() > 0) {
            QueryProductDetailsParams queryProductDetailsParamsIAP =
                    QueryProductDetailsParams.newBuilder()
                            .setProductList(listIAPProduct)
                            .build();
            billingClient.queryProductDetailsAsync(queryProductDetailsParamsIAP, new ProductDetailsResponseListener() {
                        public void onProductDetailsResponse(BillingResult billingResult, List<ProductDetails> productDetailsList) {
                            // check billingResult
                            // process returned productDetailsList
                            if (productDetailsList != null) {
                                productDetailsListIAP = productDetailsList;
                                addProductDetailsINAPToMap(productDetailsList);
                            }
                        }
                    }
            );
        }
        if (listSubProduct.size() > 0) {
            QueryProductDetailsParams queryProductDetailsParamsSub =
                    QueryProductDetailsParams.newBuilder()
                            .setProductList(listSubProduct)
                            .build();
            billingClient.queryProductDetailsAsync(queryProductDetailsParamsSub, new ProductDetailsResponseListener() {
                        public void onProductDetailsResponse(BillingResult billingResult, List<ProductDetails> productDetailsList) {
                            // check billingResult
                            // process returned productDetailsList
                            if (productDetailsList != null) {
                                productDetailsListSub = productDetailsList;
                                addProductDetailsSubsToMap(productDetailsList);
                            }
                        }
                    }
            );
        }
    }

    private void addProductDetailsINAPToMap(List<ProductDetails> productDetailsList) {
        for (ProductDetails productDetails : productDetailsList) {
            productDetailsINAPMap.put(productDetails.getProductId(), productDetails);
        }
    }

    private void addProductDetailsSubsToMap(List<ProductDetails> productDetailsList) {
        for (ProductDetails productDetails : productDetailsList) {
            productDetailsSubsMap.put(productDetails.getProductId(), productDetails);
        }
    }

    public String purchase(Activity activity, String productId) {
        ProductDetails productDetails = productDetailsINAPMap.get(productId);
        if (isPurchaseTest) {
            PurchaseTestBottomSheet purchaseTestBottomSheet = new PurchaseTestBottomSheet(typeIAP, productDetails, activity, purchaseCallback);
            purchaseTestBottomSheet.show();
            return "Purchase Test BottomSheet";
        }
        if (productDetails == null) {
            return "Product id invalid";
        }
        ImmutableList<BillingFlowParams.ProductDetailsParams> productDetailsParamsList =
                ImmutableList.of(
                        BillingFlowParams.ProductDetailsParams.newBuilder()
                                .setProductDetails(productDetails)
                                .build()
                );

        BillingFlowParams billingFlowParams = BillingFlowParams.newBuilder()
                .setProductDetailsParamsList(productDetailsParamsList)
                .build();

        // Launch the billing flow
        BillingResult billingResult = billingClient.launchBillingFlow(activity, billingFlowParams);
        switch (billingResult.getResponseCode()) {
            case BillingClient.BillingResponseCode.BILLING_UNAVAILABLE:
                return "Billing not supported for type of request";
            case BillingClient.BillingResponseCode.ITEM_NOT_OWNED:
            case BillingClient.BillingResponseCode.DEVELOPER_ERROR:
                return "";
            case BillingClient.BillingResponseCode.ERROR:
                return "Error completing request";
            case BillingClient.BillingResponseCode.FEATURE_NOT_SUPPORTED:
                return "Error processing request.";
            case BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED:
                return "Selected item is already owned";
            case BillingClient.BillingResponseCode.ITEM_UNAVAILABLE:
                return "Item not available";
            case BillingClient.BillingResponseCode.SERVICE_DISCONNECTED:
                return "Play Store service is not connected now";
            case BillingClient.BillingResponseCode.SERVICE_TIMEOUT:
                return "Timeout";
            case BillingClient.BillingResponseCode.SERVICE_UNAVAILABLE:
                return "Network Connection down";
            case BillingClient.BillingResponseCode.USER_CANCELED:
                return "Request Canceled";
            case BillingClient.BillingResponseCode.OK:
                return "Subscribed Successfully";
        }
        return "";
    }

    public String subscribe(Activity activity, String productId) {
        if (isPurchaseTest) {
            purchase(activity, PRODUCT_ID_TEST);
        }
        ProductDetails productDetails = productDetailsSubsMap.get(productId);
        if (productDetails == null) {
            return "Product id invalid";
        }
        List<ProductDetails.SubscriptionOfferDetails> subsDetail = productDetails.getSubscriptionOfferDetails();
        if (subsDetail == null) {
            return "Get Subscription Offer Details fail";
        }
        String offerToken = subsDetail.get(subsDetail.size() - 1).getOfferToken();
        ImmutableList<BillingFlowParams.ProductDetailsParams> productDetailsParamsList =
                ImmutableList.of(
                        BillingFlowParams.ProductDetailsParams.newBuilder()
                                .setProductDetails(productDetails)
                                .setOfferToken(offerToken)
                                .build()
                );

        BillingFlowParams billingFlowParams = BillingFlowParams.newBuilder()
                .setProductDetailsParamsList(productDetailsParamsList)
                .build();

        // Launch the billing flow
        BillingResult billingResult = billingClient.launchBillingFlow(activity, billingFlowParams);
        switch (billingResult.getResponseCode()) {
            case BillingClient.BillingResponseCode.BILLING_UNAVAILABLE:
                return "Billing not supported for type of request";
            case BillingClient.BillingResponseCode.ITEM_NOT_OWNED:
            case BillingClient.BillingResponseCode.DEVELOPER_ERROR:
                return "";
            case BillingClient.BillingResponseCode.ERROR:
                return "Error completing request";
            case BillingClient.BillingResponseCode.FEATURE_NOT_SUPPORTED:
                return "Error processing request.";
            case BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED:
                return "Selected item is already owned";
            case BillingClient.BillingResponseCode.ITEM_UNAVAILABLE:
                return "Item not available";
            case BillingClient.BillingResponseCode.SERVICE_DISCONNECTED:
                return "Play Store service is not connected now";
            case BillingClient.BillingResponseCode.SERVICE_TIMEOUT:
                return "Timeout";
            case BillingClient.BillingResponseCode.SERVICE_UNAVAILABLE:
                return "Network Connection down";
            case BillingClient.BillingResponseCode.USER_CANCELED:
                return "Request Canceled";
            case BillingClient.BillingResponseCode.OK:
                return "Subscribed Successfully";
        }
        return "";
    }

    private void handlePurchase(Purchase purchase) {
        isPurchase = true;
        purchaseCallback.onProductPurchased(purchase.getOrderId(), purchase.getOriginalJson());
    }

    private void verifyPurchased() {
        if (listIAPProduct != null && listIAPProduct.size() > 0) {
            billingClient.queryPurchasesAsync(QueryPurchasesParams.newBuilder().setProductType(typeIAP).build(), new PurchasesResponseListener() {
                @Override
                public void onQueryPurchasesResponse(@NonNull BillingResult billingResult, @NonNull List<Purchase> list) {
                    if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                        for (Purchase purchase : list) {
                            for (QueryProductDetailsParams.Product id : listIAPProduct) {
                                if (purchase.getProducts().contains(id.zza())) {
                                    isPurchase = true;
                                }
                            }
                        }
                    }
                }
            });
        }
        if (listSubProduct != null && listSubProduct.size() > 0) {
            billingClient.queryPurchasesAsync(QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.SUBS).build(), new PurchasesResponseListener() {
                @Override
                public void onQueryPurchasesResponse(@NonNull BillingResult billingResult, @NonNull List<Purchase> list) {
                    if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                        for (Purchase purchase : list) {
                            for (QueryProductDetailsParams.Product id : listSubProduct) {
                                if (purchase.getProducts().contains(id.zza())) {
                                    isPurchase = true;
                                }
                            }
                        }
                    }
                }
            });
        }
    }

    public String getPrice(String productId) {
        ProductDetails productDetails = productDetailsINAPMap.get(productId);
        if (productDetails == null) {
            return "";
        }
        Log.e(TAG, "getPrice: " + productDetails.getOneTimePurchaseOfferDetails().getFormattedPrice());
        return productDetails.getOneTimePurchaseOfferDetails().getFormattedPrice();
    }

    public String getPriceSub(String productId) {
        ProductDetails productDetails = productDetailsSubsMap.get(productId);
        if (productDetails == null)
            return "";
        List<ProductDetails.SubscriptionOfferDetails> subsDetail = productDetails.getSubscriptionOfferDetails();
        if (subsDetail == null) {
            return "";
        }
        List<ProductDetails.PricingPhase> pricingPhaseList = subsDetail.get(subsDetail.size() - 1).getPricingPhases().getPricingPhaseList();
        Log.e(TAG, "getPriceSub: " + pricingPhaseList.get(pricingPhaseList.size() - 1).getFormattedPrice());
        return pricingPhaseList.get(pricingPhaseList.size() - 1).getFormattedPrice();
    }
}
