package com.example.viyer;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.example.viyer.adapters.SlidesAdapter;
import com.example.viyer.models.Product;

import org.parceler.Parcels;

public class ProductDetailsActivity extends AppCompatActivity {

    private TextView tvTitle;
    private TextView tvCategory;
    private TextView tvPrice;
    private ViewPager vpImages;
    private Button btnOffer;
    private Button btnBuy;
    private Product product;
    private SlidesAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_details);

        tvTitle = findViewById(R.id.tvTitle);
        tvCategory = findViewById(R.id.tvCategory);
        tvPrice = findViewById(R.id.tvPrice);
        vpImages = findViewById(R.id.vpImages);
        btnOffer = findViewById(R.id.btnOffer);
        btnBuy = findViewById(R.id.btnBuy);

        product = (Product) Parcels.unwrap(getIntent().getParcelableExtra(Product.class.getSimpleName()));

        tvTitle.setText(product.getTitle());
        tvPrice.setText("$" + product.getPrice());

        adapter = new SlidesAdapter(product.getPhotoUrls(), this);
        vpImages.setAdapter(adapter);
    }
}