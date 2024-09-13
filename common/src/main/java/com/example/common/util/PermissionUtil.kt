package com.example.common.util

import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.activity.result.IntentSenderRequest
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.startIntentSenderForResult
import androidx.core.content.ContextCompat

object PermissionUtil {
    private const val REQUEST_CODE = 100

    // 需要的权限数组
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    val requiredPermissions = arrayOf(
        android.Manifest.permission.ACCESS_NETWORK_STATE,
        android.Manifest.permission.INTERNET,
        android.Manifest.permission.RECORD_AUDIO,
        android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
        android.Manifest.permission.READ_EXTERNAL_STORAGE,
        android.Manifest.permission.SYSTEM_ALERT_WINDOW,
        android.Manifest.permission.MODIFY_AUDIO_SETTINGS,
        android.Manifest.permission.READ_MEDIA_IMAGES,
        android.Manifest.permission.READ_MEDIA_VIDEO,
        android.Manifest.permission.READ_MEDIA_AUDIO,
        android.Manifest.permission.FOREGROUND_SERVICE,
        android.Manifest.permission.CAMERA,
        android.Manifest.permission.VIBRATE
    )

    // 检查是否所有权限都已被授予
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    fun hasPermissions(activity: Activity): Boolean {
        // 如果是低于 Android 6.0 的设备，所有权限默认已授予
        return requiredPermissions.all {
            ContextCompat.checkSelfPermission(activity, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    // 请求未授予的权限
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    fun requestPermissions(activity: Activity) {
        // 如果是低于 Android 6.0 的设备，不需要动态请求权限
        val permissionsToRequest = requiredPermissions.filter {
            ContextCompat.checkSelfPermission(activity, it) != PackageManager.PERMISSION_GRANTED
        }
        if (permissionsToRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                activity,
                permissionsToRequest.toTypedArray(),
                REQUEST_CODE
            )
        }
    }

    // 处理权限请求结果
    fun onRequestPermissionsResult(
        requestCode: Int,
        grantResults: IntArray,
        onPermissionGranted: () -> Unit,
        onPermissionDenied: () -> Unit
    ) {
        if (requestCode == REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                onPermissionGranted()
            } else {
                onPermissionDenied()
            }
        }
    }
}