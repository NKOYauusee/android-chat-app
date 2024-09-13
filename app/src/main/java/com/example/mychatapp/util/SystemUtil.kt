package com.example.mychatapp.util

import android.content.Context
import android.graphics.Bitmap
import com.example.common.common.Constants
import com.example.common.util.BitmapUtil
import com.example.common.util.SpUtil

object SystemUtil {
    fun saveToSystemGallery(context: Context, filePath: String) {
        if (!SpUtil.getParam(Constants.IS_SAVE_CROP_IMAGE, false))
            return

        val bitmap: Bitmap = BitmapUtil.getBitmapFromLocalPath(filePath)
        BitmapUtil.saveImageToGallery(context, bitmap)
    }

    fun initSystemUserSetting() {
        SpUtil.setParam(Constants.IS_SAVE_CROP_IMAGE, true)

    }
}