package com.example.common.util

import android.content.Context
import android.widget.Toast

object ToastUtil {

    fun showToastMsg(msg: String, context: Context) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
    }
}