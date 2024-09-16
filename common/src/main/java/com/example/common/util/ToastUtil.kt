package com.example.common.util

import android.content.Context
import android.widget.Toast
import com.zuad.baselib.utils.AppGlobals

object ToastUtil {

    fun showToastMsg(msg: String, context: Context) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
    }

    fun showToastMsg(msg: String) {
        Toast.makeText(AppGlobals.application, msg, Toast.LENGTH_SHORT).show()
    }
}