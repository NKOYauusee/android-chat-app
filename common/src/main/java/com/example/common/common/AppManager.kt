package com.example.common.common

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.os.Process
import android.text.TextUtils
import java.util.Stack
import kotlin.system.exitProcess


class AppManager private constructor() {
    private val activityStack = Stack<Activity>()

    companion object {
        val instant by lazy { AppManager() }
    }

    //入栈
    fun addActivity(activity: Activity) {
        activityStack.add(activity)
    }

    //出栈
    fun finishActivity(activity: Activity) {
        activity.finish()
        activityStack.remove(activity)

    }

    //出栈
    fun finishActivity(activityClass: Class<*>) {
        for (activity in activityStack) {
            if (TextUtils.equals(activity::class.java.name, activityClass.name)) {
                activity.finish()
                activityStack.remove(activity)
                break
            }
        }

    }

    //出栈
    fun finishActivity(activityName: String) {
        for (activity in activityStack) {
            if (TextUtils.equals(activity::class.java.name, activityName)) {
                activity.finish()
                activityStack.remove(activity)
                break
            }
        }

    }


    //获取当前栈顶的activity
    fun currentActivity(): Activity {
        //最后一个元素 压栈
        return activityStack.lastElement()
    }

    // 清理所有栈 出栈
    fun finishAllActivity() {
        for (activity in activityStack) {
            activity.finish()
        }
        activityStack.clear()
    }


    //获取这个activity是否在栈内
    fun getActivityIsShow(activityClass: Class<*>): Boolean {
        for (activity in activityStack) {
            if (TextUtils.equals(activity::class.java.name, activityClass.name)) {
                return true
            }
        }
        return false
    }


    @SuppressLint("MissingPermission")
    //退出app
    fun exitApp(context: Context) {
        finishAllActivity()
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        activityManager.killBackgroundProcesses(context.packageName)
        exitProcess(0)
        //android.os.Process.killProcess(android.os.Process.myPid());

    }


    fun restartApp(context: Context) {
        val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)
        if (intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }

        // 杀掉当前进程
        Process.killProcess(Process.myPid())
        exitProcess(0)
    }
}