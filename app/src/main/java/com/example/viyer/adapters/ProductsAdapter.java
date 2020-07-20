package com.example.viyer.adapters;

import android.graphics.Bitmap;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.viyer.layouts.ImagePreviewView;

import java.util.List;

public class ProductsAdapter extends RecyclerView.Adapter<ProductsAdapter.ViewHolder> {

    List<Bitmap> photoUrls;

    public ProductsAdapter(List<Bitmap> photoUrls) {
        this.photoUrls = photoUrls;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ImagePreviewView productView = new ImagePreviewView(parent.getContext());
        return new ViewHolder(productView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Bitmap item = photoUrls.get(position);
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return photoUrls.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private ImagePreviewView productView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            productView = (ImagePreviewView) itemView;
        }

        public void bind(Bitmap item) {
            productView.setImg(item);
        }
    }
}
