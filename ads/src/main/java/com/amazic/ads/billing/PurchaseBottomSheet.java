package com.amazic.ads.billing;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.amazic.ads.R;
import com.amazic.ads.callback.PurchaseListener;
import com.android.billingclient.api.ProductDetails;
import com.google.android.material.bottomsheet.BottomSheetDialog;

public class PurchaseBottomSheet extends BottomSheetDialog {
    private ProductDetails productDetails;
    private int typeIap;
    private TextView txtTitle;
    private TextView txtDescription;
    private TextView txtId;
    private TextView txtPrice;
    private TextView txtContinuePurchase;
    private PurchaseListener purchaseListener;

    public PurchaseBottomSheet(int typeIap, ProductDetails productDetails, @NonNull Context context, PurchaseListener purchaseListener) {
        super(context);
        this.productDetails = productDetails;
        this.typeIap = typeIap;
        this.purchaseListener = purchaseListener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_billing_test);
        txtTitle = findViewById(R.id.txtTitle);
        txtDescription = findViewById(R.id.txtDescription);
        txtId = findViewById(R.id.txtId);
        txtPrice = findViewById(R.id.txtPrice);
        txtContinuePurchase = findViewById(R.id.txtContinuePurchase);
        if (productDetails == null) {

        } else {
            txtTitle.setText(productDetails.getTitle());
            txtDescription.setText(productDetails.getDescription());
            txtId.setText(productDetails.getProductId());
            if (typeIap == AppPurchase.TYPE_IAP.PURCHASE)
                txtPrice.setText(productDetails.getOneTimePurchaseOfferDetails().getFormattedPrice());
            else
                txtPrice.setText(productDetails.getSubscriptionOfferDetails().get(0).getPricingPhases().getPricingPhaseList().get(0).getFormattedPrice());

            txtContinuePurchase.setOnClickListener(v -> {
                if (purchaseListener != null) {
                    AppPurchase.getInstance().setPurchase(true);
                    purchaseListener.onProductPurchased(productDetails.getProductId(), "{\"productId\":\"android.test.purchased\",\"type\":\"inapp\",\"title\":\"Tiêu đề mẫu\",\"description\":\"Mô tả mẫu về sản phẩm: android.test.purchased.\",\"skuDetailsToken\":\"AEuhp4Izz50wTvd7YM9wWjPLp8hZY7jRPhBEcM9GAbTYSdUM_v2QX85e8UYklstgqaRC\",\"oneTimePurchaseOfferDetails\":{\"priceAmountMicros\":23207002450,\"priceCurrencyCode\":\"VND\",\"formattedPrice\":\"23.207 ₫\"}}', parsedJson={\"productId\":\"android.test.purchased\",\"type\":\"inapp\",\"title\":\"Tiêu đề mẫu\",\"description\":\"Mô tả mẫu về sản phẩm: android.test.purchased.\",\"skuDetailsToken\":\"AEuhp4Izz50wTvd7YM9wWjPLp8hZY7jRPhBEcM9GAbTYSdUM_v2QX85e8UYklstgqaRC\",\"oneTimePurchaseOfferDetails\":{\"priceAmountMicros\":23207002450,\"priceCurrencyCode\":\"VND\",\"formattedPrice\":\"23.207 ₫\"}}, productId='android.test.purchased', productType='inapp', title='Tiêu đề mẫu', productDetailsToken='AEuhp4Izz50wTvd7YM9wWjPLp8hZY7jRPhBEcM9GAbTYSdUM_v2QX85e8UYklstgqaRC', subscriptionOfferDetails=null}");
                }
                dismiss();
            });
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        int w = ViewGroup.LayoutParams.MATCH_PARENT;
        int h = ViewGroup.LayoutParams.WRAP_CONTENT;
        getWindow().setLayout(w, h);
    }
}
