package com.example.common.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class BitmapUtil {
    private static final String TAG = "BitmapUtil";
    public static final int BITMAP_MAX_SIZE = 200; //kb

    private BitmapUtil() {
        // 私有构造函数
    }

    /**
     * 更改 bitmap 透明度
     *
     * @param srcBitmap 原始 bitmap
     * @param alpha     透明度
     * @param isRecycle 是否释放原图
     * @return 更改透明度后的 bitmap
     */
    public static Bitmap toAlpha(Bitmap srcBitmap, int alpha, boolean isRecycle) {
        Paint paint = new Paint();
        paint.setAlpha(alpha);
        Bitmap descBitmap = Bitmap.createBitmap(srcBitmap.getWidth(), srcBitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(descBitmap);
        c.drawBitmap(srcBitmap, 0, 0, paint);
        if (isRecycle) {
            recycle(srcBitmap);
        }
        return descBitmap;
    }

    /**
     * 缩放图片
     *
     * @param bitmap      原始 bitmap
     * @param scaleWidth  缩放宽度
     * @param scaleHeight 缩放高度
     * @return 缩放后的 bitmap
     */
    public static Bitmap createBitmap(Bitmap bitmap, float scaleWidth, float scaleHeight) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        return Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
    }

    /**
     * 保存图片到相册
     *
     * @param context 应用程序上下文
     * @param bmp     需要保存的 bitmap
     */
    public static void saveImageToGallery(Context context, Bitmap bmp) {
        File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File appDir = new File(path, "chatapp");

        if (!appDir.exists()) {
            appDir.mkdirs();
        }
        String fileName = "crop_" + System.currentTimeMillis() + ".jpg";
        File file = new File(appDir, fileName);
        try {
            FileOutputStream fos = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
        } catch (Exception e) {
            Log.e(TAG, "saveImageToGallery: ", e);
        }
        MediaScannerConnection.scanFile(context, new String[]{file.toString()}, null, null);
    }

    /**
     * 根据 Bitmap 转换成 Uri
     *
     * @param context 应用程序上下文
     * @param inImage 需要转换的 bitmap
     * @return 转换后的 Uri
     */
    public static Uri bitmapToUri(Context context, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(context.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }

    /**
     * Drawable 转换成 bitmap
     *
     * @param drawable 需要转换的 drawable
     * @return 转换后的 bitmap
     */
    public static Bitmap drawableToBitmap(Drawable drawable) {
        int width = drawable.getIntrinsicWidth();
        int height = drawable.getIntrinsicHeight();
        Bitmap.Config config = drawable.getOpacity() != PixelFormat.OPAQUE ?
                Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565;
        Bitmap bitmap = Bitmap.createBitmap(width, height, config);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, width, height);
        drawable.draw(canvas);
        return bitmap;
    }

    /**
     * bitmap 转换成 byte
     */
    public static byte[] bitmapToBytes(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        return stream.toByteArray();
    }

    /**
     * 从View获取Bitmap
     *
     * @param view 文件路径
     * @return 返回 bitmap
     */
    public static Bitmap getBitmapFromView(View view) {
        Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);
        return bitmap;
    }

    /**
     * 按比例大小压缩Bitmap
     *
     * @param srcBitmap    文件路径
     * @param targetWidth  设置宽度
     * @param targetHeight 设置高度
     * @return 返回 bitmap
     */
    public static Bitmap compressByScale(Bitmap srcBitmap, int targetWidth, int targetHeight) {
        return Bitmap.createScaledBitmap(srcBitmap, targetWidth, targetHeight, true);
    }

    /**
     * 质量压缩图片
     *
     * @param image 需要压缩的 bitmap
     * @return 压缩后的图片 byte 数组
     */
    public static byte[] compressImageByte(Bitmap image) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        int options = 100;
        while (baos.toByteArray().length / 1024 > BITMAP_MAX_SIZE) {
            baos.reset();
            image.compress(Bitmap.CompressFormat.JPEG, options, baos);
            options -= 10;
        }
        return baos.toByteArray();
    }

    /**
     * 获取 bitmap 大小
     *
     * @param bitmap 目标 bitmap
     * @return bitmap 的大小
     */
    public static int getBitmapBytes(Bitmap bitmap) {
        int result = bitmap.getByteCount();
        if (result < 0) {
            throw new IllegalStateException("Negative size: " + bitmap);
        }
        return result;
    }

    /**
     * 释放 bitmap 资源
     *
     * @param bitmap 需要释放的 bitmap
     */
    public static void recycle(Bitmap bitmap) {
        if (bitmap != null && !bitmap.isRecycled()) {
            bitmap.recycle();
        }
    }

    /**
     * 根据图片路径获取 bitmap
     *
     * @param context  应用程序上下文
     * @param filePath 图片文件路径
     * @return 对应的 bitmap
     */
    public static Bitmap getBitmapByUrl(@NonNull Context context, String filePath) {
        File file = new File(filePath);
        Uri uri = Uri.fromFile(file);
        Bitmap bitmap = null;
        try {
            bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), uri);
        } catch (IOException e) {
            Log.e(TAG, "getBitmapByUrl: ", e);
        }
        return bitmap;
    }

    /**
     * bitmap 转文件
     *
     * @param bitmap   需要转换的 bitmap
     * @param filePath 文件路径
     */
    public static void saveBitmapFile(Bitmap bitmap, String filePath) {
        File file = new File(filePath);
        BufferedOutputStream bos = null;
        try {
            bos = new BufferedOutputStream(new FileOutputStream(file));
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
            bos.flush();
        } catch (IOException e) {
            Log.e(TAG, "saveBitmapFile: ", e);
        } finally {
            if (bos != null) {
                try {
                    bos.close();
                } catch (IOException e) {
                    Log.e(TAG, "saveBitmapFile: ", e);
                }
            }
        }
    }

    /**
     * 本地图片转化为Bitmap
     *
     * @param filePath 文件路径
     * @return 返回 bitmap
     */
    public static Bitmap getBitmapFromLocalPath(String filePath) {
        return BitmapFactory.decodeFile(filePath);
    }
}
