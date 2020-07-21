package com.example.viyer.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.viyer.ProductDetailsActivity;
import com.example.viyer.R;
import com.example.viyer.models.Product;

import org.parceler.Parcels;

import java.util.List;

public class ProductsAdapter extends RecyclerView.Adapter<ProductsAdapter.ViewHolder> {

    List<Product> products;
    Context context;

    public ProductsAdapter(Context context, List<Product> photoUrls) {
        this.products = photoUrls;
        this.context = context;
    }

    @NonNull
    @Override
    public ProductsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_product, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductsAdapter.ViewHolder holder, int position) {
        Product product = products.get(position);

        holder.bind(product);
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    public void clear() {
        products.clear();
        notifyDataSetChanged();
    }

    // Add a list of items -- change to type used
    public void addAll(List<Product> list) {
        products.addAll(list);
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private ImageView ivProduct;

        public ViewHolder(@NonNull View itemView) {
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
                Product product = products.get(position);
                Intent intent = new Intent(context, ProductDetailsActivity.class);
                intent.putExtra(Product.class.getSimpleName(), Parcels.wrap(product));
                context.startActivity(intent);
            }
        }
    }
}
