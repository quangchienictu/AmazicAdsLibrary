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
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.QueryProductDetailsParams;
import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IAPManager {
    private static final String TAG = "IAPManager";
    private static IAPManager instance;
    private PurchasesUpdatedListener purchasesUpdatedListener;
    private BillingClient billingClient;
    public static String typeIAP = BillingClient.ProductType.INAPP, typeSub = BillingClient.ProductType.SUBS;
    private ArrayList<QueryProductDetailsParams.Product> listIAPProduct;
    private ArrayList<QueryProductDetailsParams.Product> listSubProduct;
    private List<ProductDetails> productDetailsListIAP;
    private List<ProductDetails> productDetailsListSub;
    final private Map<String, ProductDetails> productDetailsINAPMap = new HashMap<>();
    final private Map<String, ProductDetails> productDetailsSubsMap = new HashMap<>();

    public static IAPManager getInstance() {
        if (instance == null) {
            instance = new IAPManager();
        }
        return instance;
    }

    public void initBilling(Context context, ArrayList<ProductDetailCustom> listProductDetailCustoms, IAPCallback iapCallback) {
        setListProductDetails(listProductDetailCustoms);
        purchasesUpdatedListener = new PurchasesUpdatedListener() {
            @Override
            public void onPurchasesUpdated(@NonNull BillingResult billingResult, @Nullable List<Purchase> list) {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && list != null) {
                    for (Purchase purchase : list) {
                        handlePurchase(purchase);
                    }
                } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.USER_CANCELED) {
                    // Handle an error caused by a user cancelling the purchase flow.
                    iapCallback.onUserCancelBilling();
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
        connectToGooglePlay(iapCallback);
    }

    public void setListProductDetails(ArrayList<ProductDetailCustom> listProductDetailCustoms) {
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

    public void connectToGooglePlay(IAPCallback iapCallback) {
        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(BillingResult billingResult) {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    iapCallback.onBillingSetupFinished();
                    // The BillingClient is ready. You can query purchases here.
                    showProductsAvailableToBuy(listIAPProduct, listSubProduct);
                }
            }

            @Override
            public void onBillingServiceDisconnected() {
                iapCallback.onBillingServiceDisconnected();
                // Try to restart the connection on the next request to
                // Google Play by calling the startConnection() method.
            }
        });
    }

    public void showProductsAvailableToBuy(ArrayList<QueryProductDetailsParams.Product> listIAPProduct, ArrayList<QueryProductDetailsParams.Product> listSubProduct) {
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
