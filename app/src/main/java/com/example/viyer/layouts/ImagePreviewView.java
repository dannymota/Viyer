package com.example.viyer.layouts;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.viyer.R;

public class ImagePreviewView extends RelativeLayout {
    private ImageView mImgPhoto;
    private TextView tvMainPhoto;
    private View mBtnClose;

    // override all constructors to ensure custom logic runs in all cases
    public ImagePreviewView(Context context) {
        this(context, null);
    }
    public ImagePreviewView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }
    public ImagePreviewView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }
    public ImagePreviewView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        inflate(getContext(), R.layout.layout_image_preview, this);
        mImgPhoto = (ImageView) findViewById(R.id.imgPhoto);
        mBtnClose = (View) findViewById(R.id.btnClose);
//        tvMainPhoto = (TextView) findViewById(R.id.tvMainPhoto);
    }

    public void setImg(Bitmap bitmap) {
        Glide.with(getContext()).load(bitmap).into(mImgPhoto);
    }
}