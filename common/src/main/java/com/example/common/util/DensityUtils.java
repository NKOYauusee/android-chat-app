package com.example.common.util;

import android.app.Activity;
import android.app.Application;
import android.app.Dialog;
import android.content.ComponentCallbacks;
import android.content.Context;
import android.content.res.Configuration;
import android.util.DisplayMetrics;

import androidx.annotation.NonNull;

/**
 * 修改density，densityDpi值-直接更改系统内部对于目标尺寸而言的像素密度
 */
public class DensityUtils {
    private static final float WIDTH = 420;//参考像素密度(dp)
    private static float appDensity;//表示屏幕密度
    private static float appScaleDensity;//字体缩放比例，默认为appDensity

    public static void setDensity(final Application application, Activity activity) {

        //获取当前屏幕信息
        DisplayMetrics displayMetrics = application.getResources().getDisplayMetrics();
        if (appDensity == 0) {
            //初始化赋值
            appDensity = displayMetrics.density;
            appScaleDensity = displayMetrics.scaledDensity;
            //监听字体变化
            application.registerComponentCallbacks(new ComponentCallbacks() {
                @Override
                public void onConfigurationChanged(@NonNull Configuration newConfig) {
                    //字体发生更改，重新计算scaleDensity
                    if (newConfig.fontScale > 0) {
                        appScaleDensity = application.getResources().getDisplayMetrics().scaledDensity;
                    }
                }

                @Override
                public void onLowMemory() {

                }
            });
        }
        //计算目标density scaledDensity


        float targetDensity = Math.min(displayMetrics.widthPixels, displayMetrics.heightPixels) / WIDTH;//1080/360=3;
        float targetScaleDensity = targetDensity * (appScaleDensity / appDensity);
        int targetDensityDpi = (int) (targetDensity * 160);
        //替换Activity的值
        //px = dp * (dpi / 160)
        DisplayMetrics dm = activity.getResources().getDisplayMetrics();
        dm.density = targetDensity;  //(dpi/160) 后得到的值
        dm.scaledDensity = targetScaleDensity;
        dm.densityDpi = targetDensityDpi;  //dpi

    }

    public static void setDensity(final Application application, Dialog dialog) {

        //获取当前屏幕信息
        DisplayMetrics displayMetrics = application.getResources().getDisplayMetrics();
        if (appDensity == 0) {
            //初始化赋值
            appDensity = displayMetrics.density;
            appScaleDensity = displayMetrics.scaledDensity;
            //监听字体变化
            application.registerComponentCallbacks(new ComponentCallbacks() {
                @Override
                public void onConfigurationChanged(@NonNull Configuration newConfig) {
                    //字体发生更改，重新计算scaleDensity
                    if (newConfig.fontScale > 0) {
                        appScaleDensity = application.getResources().getDisplayMetrics().scaledDensity;
                    }
                }

                @Override
                public void onLowMemory() {

                }
            });
        }
        //计算目标density scaledDensity


        float targetDensity = Math.min(displayMetrics.widthPixels, displayMetrics.heightPixels) / WIDTH;//1080/360=3;
        float targetScaleDensity = targetDensity * (appScaleDensity / appDensity);
        int targetDensityDpi = (int) (targetDensity * 160);
        //替换Activity的值
        //px = dp * (dpi / 160)
        DisplayMetrics dm = dialog.getContext().getResources().getDisplayMetrics();
        dm.density = targetDensity;  //(dpi/160) 后得到的值
        dm.scaledDensity = targetScaleDensity;
        dm.densityDpi = targetDensityDpi;  //dpi

    }

    /**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     */
    public static int dip2px(Context context, float dpValue) {

        final float scale = context.getResources().getDisplayMetrics().density;

        return (int) (dpValue * scale + 0.5f);

    }

    /**
     * 根据手机的分辨率从 px(像素) 的单位 转成为 dp
     */
    public static int px2dip(Context context, float pxValue) {

        final float scale = context.getResources().getDisplayMetrics().density;

        return (int) (pxValue / scale + 0.5f);

    }
}
