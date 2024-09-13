package com.example.mychatapp.util;

import android.graphics.Bitmap;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;

import com.bumptech.glide.request.target.ImageViewTarget;
import com.example.common.util.LogUtil;
import com.example.mychatapp.R;

public class TransformationUtil extends ImageViewTarget<Bitmap> {
    private final ImageView target;

    public TransformationUtil(ImageView target) {
        super(target);
        this.target = target;
    }

    @Override
    protected void setResource(Bitmap resource) {
        target.setVisibility(View.VISIBLE);
        if (resource == null) {
            // 加载失败，可以在这里设置错误占位图或者进行其他错误处理
            target.setImageResource(R.drawable.image_placeholder);
            return;
        }

        target.setImageBitmap(resource);

        // 确保imageView已经测量完毕
        if (target.getWidth() == 0 || target.getHeight() == 0) {
            // imageView尚未测量完毕，我们需要等待布局完成
            target.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    target.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    // 现在可以安全地获取imageView的宽度和高度
                    int imageViewWidth = 500;
                    int imageViewHeight = calculateImageViewHeight(resource, imageViewWidth);
                    updateImageViewLayoutParams(imageViewWidth, imageViewHeight);
                }
            });
        } else {
            // imageView的尺寸已经可用，可以直接计算和更新布局参数
            int imageViewWidth = 500;
            int imageViewHeight = calculateImageViewHeight(resource, imageViewWidth);
            updateImageViewLayoutParams(imageViewWidth, imageViewHeight);
        }
    }

    private int calculateImageViewHeight(Bitmap resource, int imageViewWidth) {
        // 获取原图的宽高
        int width = resource.getWidth();
        LogUtil.INSTANCE.info("width: " + width);
        int height = resource.getHeight();
        LogUtil.INSTANCE.info("height: " + height);
        // 计算缩放比例
        float scale = (float) imageViewWidth / (float) width;
        // 计算图片等比例放大后的高
        return (int) (height * scale);
    }

    private void updateImageViewLayoutParams(int imageViewWidth, int imageViewHeight) {
        ViewGroup.LayoutParams params = target.getLayoutParams();
        params.height = imageViewHeight;
        params.width = imageViewWidth;
        target.setLayoutParams(params);
        target.setVisibility(View.VISIBLE);
    }
}
