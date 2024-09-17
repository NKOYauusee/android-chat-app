package com.example.common.util

import android.content.Context
import android.graphics.Bitmap
import com.example.common.common.Constants

object SettingUtil {
    fun saveToSystemGallery(context: Context, filePath: String) {
        if (!SpUtil.getParam(Constants.IS_SAVE_CROP_IMAGE, false))
            return

        val bitmap: Bitmap = BitmapUtil.getBitmapFromLocalPath(filePath)
        BitmapUtil.saveImageToGallery(context, bitmap)
    }

    fun initSystemUserSetting() {
        SpUtil.setParam(Constants.IS_SAVE_CROP_IMAGE, true)
    }


    fun getServerIpAddress(): String {
        return SpUtil.getParam(Constants.SERVER_IP, "")
    }

    fun setServerIpAddress(serverIp: String) {
        SpUtil.setParam(Constants.SERVER_IP, serverIp)
    }


    fun getServerPort(): String {
        return SpUtil.getParam(Constants.SERVER_PORT, "")
    }

    fun setServerPort(serverPort: String) {
        SpUtil.setParam(Constants.SERVER_PORT, serverPort)
    }

}