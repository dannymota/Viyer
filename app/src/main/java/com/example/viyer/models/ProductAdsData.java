package com.example.viyer.models;

import com.google.android.gms.ads.formats.UnifiedNativeAd;

public class ProductAdsData {
    public int type;
    public UnifiedNativeAd ads;
    public Product product;

    public int getType() {
        return type;
    }

    public UnifiedNativeAd getAds() {
        return ads;
    }

    public Product getProduct() {
        return product;
    }
}
