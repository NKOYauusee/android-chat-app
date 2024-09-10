package com.example.common.baselib;

import android.view.View;

public class ViewWrapper {
    private final View mTarget;

    public ViewWrapper(View view) {
        mTarget = view;
    }

    public void setWidth(int width) {
        mTarget.getLayoutParams().width = width;
        mTarget.requestLayout();//必须调用，否则宽度改变但UI没有刷新
    }

    public int getWidth() {
        return mTarget.getLayoutParams().width;
    }
}