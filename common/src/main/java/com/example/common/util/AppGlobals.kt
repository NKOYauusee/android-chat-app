package com.zuad.baselib.utils

import android.app.Application

/***
 * 一种通过反射的方式获取全局的Application
 */
object AppGlobals {
    var application: Application? = null
    fun get(): Application? {
        if (application == null) {
            try {
                application = Class.forName("android.app.ActivityThread")
                    .getMethod("currentApplication")
                    .invoke(null) as Application
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
        return application

    }
}