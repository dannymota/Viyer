package com.example.viyer.adapters;

import android.graphics.Bitmap;
import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.viyer.R;
import com.example.viyer.layouts.ImagePreviewView;

import java.util.List;

public class ProductsAdapter extends RecyclerView.Adapter<ProductsAdapter.ViewHolder> {

    List<Bitmap> photos;
    List<Uri> filePaths;

    public ProductsAdapter(List<Bitmap> photos, List<Uri> filePaths) {
        this.photos = photos;
        this.filePaths = filePaths;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ImagePreviewView productView = new ImagePreviewView(parent.getContext());
        return new ViewHolder(productView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Bitmap item = photos.get(position);
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return photos.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private ImagePreviewView productView;
        private View mBtnClose;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            productView = (ImagePreviewView) itemView;
            mBtnClose = itemView.findViewById(R.id.btnClose);
            itemView.setOnClickListener(this);
        }

        public void bind(final Bitmap item) {
            productView.setImg(item);

            mBtnClose.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = getAdapterPosition();
                    photos.remove(position);
                    filePaths.remove(position);
                    ProductsAdapter.this.notifyDataSetChanged();
                }
            });
        }

        @Override
        public void onClick(View view) {

        }
    }
}
