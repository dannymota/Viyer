package com.example.viyer.layouts;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.viyer.R;

public class ImagePreviewLayout extends FrameLayout {
    private View mRoot;
    private ImageView mImgPhoto;
    private View mBtnClose;
    private TextView tvMainPhoto;
    private LayoutInflater inflater;
    private Bitmap imgPhoto;
    private Boolean isMainPhoto;
    private Context mContext;

    public ImagePreviewLayout(final Context context, Bitmap imgPhoto, Boolean isMainPhoto) {
        super(context, null, 0);
        this.imgPhoto = imgPhoto;
        this.isMainPhoto = isMainPhoto;
        init(context);
    }

    private void init(final Context context) {
        if (isInEditMode())
            return;

        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View customView = null;

        if (inflater != null)
            customView = inflater.inflate(R.layout.layout_image_preview, this);

        if (customView == null)
            return;

        mRoot = customView.findViewById(R.id.action_post);
        mImgPhoto = customView.findViewById(R.id.imgPhoto);
        mBtnClose = customView.findViewById(R.id.btnClose);
        tvMainPhoto = customView.findViewById(R.id.tvMainPhoto);
        mImgPhoto.setImageBitmap(imgPhoto);

        tvMainPhoto.setVisibility(View.GONE);

        if (isMainPhoto) {
            tvMainPhoto.setVisibility(View.VISIBLE);
        }
    }

    public View getRoot() {
        return mRoot;
    }

    public ImageView getImgPhoto() {
        return mImgPhoto;
    }

    public View getBtnClose() {
        return mBtnClose;
    }
}
