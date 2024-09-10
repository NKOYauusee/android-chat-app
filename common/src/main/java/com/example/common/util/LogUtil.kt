package com.example.common.util

import android.util.Log

object LogUtil {

    fun info(msg: String) {
        Log.d("xht", msg)
    }

    fun error(msg: String) {
        Log.e(msg, msg)
    }
}