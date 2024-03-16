package com.amazic.ads.iap;

public class ProductDetailCustom {
    private String productId;
    private String productType;

    public ProductDetailCustom(String productId, String productType) {
        this.productId = productId;
        this.productType = productType;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getProductType() {
        return productType;
    }

    public void setProductType(String productType) {
        this.productType = productType;
    }
}
