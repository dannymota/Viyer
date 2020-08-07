package com.example.viyer.adapters;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityOptionsCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.viyer.ProductDetailsActivity;
import com.example.viyer.R;
import com.example.viyer.models.Product;
import com.example.viyer.models.ProductAdsData;
import com.google.android.gms.ads.formats.MediaView;
import com.google.android.gms.ads.formats.NativeAd;
import com.google.android.gms.ads.formats.UnifiedNativeAd;
import com.google.android.gms.ads.formats.UnifiedNativeAdView;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;

public class ProductsAdapter extends RecyclerView.Adapter<ProductsAdapter.ProductViewHolder> {

    List<Product> products;
    List<ProductAdsData> productsCopy;
    List<ProductAdsData> productAds;
    Context context;
    public static final int UNIFIED_ADS_VIEW  = 1;
    public static final int PRODUCT_ITEM_VIEW = 2;

    public ProductsAdapter(Context context, List<ProductAdsData> productAds) {
        this.productAds = productAds;
        this.context = context;
        productsCopy = new ArrayList<>();
    }

    public void copyProducts(List<ProductAdsData> products) {
        productsCopy.addAll(products);
    }

    @Override
    public int getItemViewType (int position) {
        if(productAds.get(position).getType() == 1) {
            return UNIFIED_ADS_VIEW;
        } else {
            return PRODUCT_ITEM_VIEW;
        }
    }

    public void setProductAdsList(List<ProductAdsData> productAds) {
        this.productAds = productAds;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        switch (viewType) {
            case UNIFIED_ADS_VIEW:
                View unifiedNativeLayoutView = LayoutInflater.from(context).inflate(R.layout.item_ad, parent, false);
                return new UnifiedNativeAdViewHolder(unifiedNativeLayoutView);
            case PRODUCT_ITEM_VIEW:
            default:
                View view = LayoutInflater.from(context).inflate(R.layout.item_product, parent, false);
                return new ProductViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull ProductsAdapter.ProductViewHolder holder, int position) {
//        StaggeredGridLayoutManager.LayoutParams layoutParams = new StaggeredGridLayoutManager.LayoutParams(holder.itemView.getLayoutParams());
        int viewType = getItemViewType(position);
        switch (viewType) {
            case UNIFIED_ADS_VIEW:
                Log.d("Products", "loading an ad");
//                layoutParams.setFullSpan(true);
                UnifiedNativeAd nativeAd = (UnifiedNativeAd) productAds.get(position).getAds();
                populateNativeAdView(nativeAd, ((UnifiedNativeAdViewHolder) holder).getAdView());
                break;
            case PRODUCT_ITEM_VIEW:
            default:
                Log.d("Products", "Loading a product");
//                layoutParams.setFullSpan(false);
                Product product = productAds.get(position).getProduct();
                holder.bind(product);
        }
//        holder.itemView.setLayoutParams(layoutParams);
    }

    @Override
    public int getItemCount() {
        return productAds.size();
    }

    private void populateNativeAdView(UnifiedNativeAd nativeAd,
                                      UnifiedNativeAdView adView) {
        // Some assets are guaranteed to be in every UnifiedNativeAd.
        ((TextView) adView.getHeadlineView()).setText(nativeAd.getHeadline());
        ((TextView) adView.getBodyView()).setText(nativeAd.getBody());
        ((Button) adView.getCallToActionView()).setText(nativeAd.getCallToAction());

        // These assets aren't guaranteed to be in every UnifiedNativeAd, so it's important to
        // check before trying to display them.
        NativeAd.Image icon = nativeAd.getIcon();

        if (icon == null) {
            adView.getIconView().setVisibility(View.INVISIBLE);
        } else {
            ((ImageView) adView.getIconView()).setImageDrawable(icon.getDrawable());
            adView.getIconView().setVisibility(View.VISIBLE);
        }

        if (nativeAd.getPrice() == null) {
            adView.getPriceView().setVisibility(View.INVISIBLE);
        } else {
            adView.getPriceView().setVisibility(View.VISIBLE);
            ((TextView) adView.getPriceView()).setText(nativeAd.getPrice());
        }

        if (nativeAd.getStore() == null) {
            adView.getStoreView().setVisibility(View.INVISIBLE);
        } else {
            adView.getStoreView().setVisibility(View.VISIBLE);
            ((TextView) adView.getStoreView()).setText(nativeAd.getStore());
        }

        if (nativeAd.getStarRating() == null) {
            adView.getStarRatingView().setVisibility(View.INVISIBLE);
        } else {
            ((RatingBar) adView.getStarRatingView())
                    .setRating(nativeAd.getStarRating().floatValue());
            adView.getStarRatingView().setVisibility(View.VISIBLE);
        }

        if (nativeAd.getAdvertiser() == null) {
            adView.getAdvertiserView().setVisibility(View.INVISIBLE);
        } else {
            ((TextView) adView.getAdvertiserView()).setText(nativeAd.getAdvertiser());
            adView.getAdvertiserView().setVisibility(View.VISIBLE);
        }

        // Assign native ad object to the native view.
        adView.setNativeAd(nativeAd);
    }

    public void filter(String text) {
        Log.d("Adapter", "Size productsCopy: " + productsCopy.size());
        productAds.clear();
        if(text.isEmpty()){
            productAds.addAll(productsCopy);
        } else {
            text = text.toLowerCase();
            for(ProductAdsData product: productsCopy) {
                if(product.getProduct().getTitle().toLowerCase().contains(text) || product.getProduct().getTitle().toLowerCase().contains(text)) {
                    productAds.add(product);
                }
            }
        }
        notifyDataSetChanged();
    }

    public void clear() {
        productAds.clear();
        notifyDataSetChanged();
    }

    public class ProductViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private ImageView ivProduct;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProduct = itemView.findViewById(R.id.ivProduct);
            itemView.setOnClickListener(this);
        }

        public void bind(Product product) {
            Glide.with(context).load(product.getPhotoUrls().get(0)).into(ivProduct);
        }

        @Override
        public void onClick(View view) {
            int position = getAdapterPosition();
            if (position != RecyclerView.NO_POSITION) {
                ProductAdsData product = productAds.get(position);
                Intent intent = new Intent(context, ProductDetailsActivity.class);
                intent.putExtra(Product.class.getSimpleName(), Parcels.wrap(product.getProduct()));
                Bundle bundle = ActivityOptionsCompat.makeCustomAnimation(context,
                        android.R.anim.fade_in, android.R.anim.fade_out).toBundle();
                context.startActivity(intent, bundle);
            }
        }
    }

    public class UnifiedNativeAdViewHolder extends ProductsAdapter.ProductViewHolder {

        private UnifiedNativeAdView adView;

        public UnifiedNativeAdView getAdView() {
            return adView;
        }

        public UnifiedNativeAdViewHolder(View view) {
            super(view);
            adView = (UnifiedNativeAdView) view.findViewById(R.id.ad_view);

            adView.setMediaView((MediaView) adView.findViewById(R.id.ad_media));

            // Register the view used for each individual asset.
            adView.setHeadlineView(adView.findViewById(R.id.ad_headline));
            adView.setBodyView(adView.findViewById(R.id.ad_body));
            adView.setCallToActionView(adView.findViewById(R.id.ad_call_to_action));
            adView.setIconView(adView.findViewById(R.id.ad_icon));
            adView.setPriceView(adView.findViewById(R.id.ad_price));
            adView.setStarRatingView(adView.findViewById(R.id.ad_stars));
            adView.setStoreView(adView.findViewById(R.id.ad_store));
            adView.setAdvertiserView(adView.findViewById(R.id.ad_advertiser));
        }
    }
}
