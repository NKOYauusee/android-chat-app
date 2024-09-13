package com.example.common.util

import android.content.Context
import androidx.annotation.NonNull
import com.tencent.mmkv.MMKV

object SpUtil {
    private var mmkv: MMKV? = null

    /**
     * 保存数据的方法，我们需要拿到保存数据的具体类型，然后根据类型调用不同的保存方法
     *
     * @param key
     * @param obj
     */
    @Synchronized
    fun setParam(key: String?, obj: Any) {
        //LogUtil.info("val: $obj")
        try {
            if (mmkv == null) {
                mmkv = MMKV.defaultMMKV()
            }
            when (obj) {
                is String -> {
                    mmkv?.encode(key, obj)
                }

                is Int -> {
                    mmkv?.encode(key, obj)
                }

                is Boolean -> {
                    mmkv?.encode(key, obj)
                }

                is Float -> {
                    mmkv?.encode(key, obj)
                }

                is Long -> {
                    mmkv?.encode(key, obj)
                }

                is Double -> {
                    mmkv?.encode(key, obj)
                }

                is ByteArray -> {
                    mmkv?.encode(key, obj)
                }

                else -> {
                    mmkv?.encode(key, obj.toString())
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }


    /**
     * 得到保存数据的方法，我们根据默认值得到保存的数据的具体类型，然后调用相对于的方法获取值
     */
    @Synchronized
    fun <T> getParam(key: String?, @NonNull defaultValue: T): T {

        return try {
            if (mmkv == null) {
                mmkv = MMKV.defaultMMKV()
            }
            when (defaultValue) {
                is String -> {
                    mmkv?.decodeString(key, defaultValue) as T
                }

                is Int -> {
                    mmkv?.decodeInt(key, defaultValue) as T
                }

                is Boolean -> {
                    mmkv?.decodeBool(key, defaultValue) as T
                }

                is Float -> {
                    mmkv?.decodeFloat(key, defaultValue) as T
                }

                is Long -> {
                    mmkv?.decodeLong(key, defaultValue) as T
                }

                is Double -> {
                    mmkv?.decodeDouble(key, defaultValue) as T
                }

                is ByteArray -> {
                    mmkv?.decodeBytes(key, defaultValue) as T
                }

                else -> {
                    mmkv?.decodeString(key, "") as T
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            LogUtil.info("akdhalfhalfhalkfalfalfbal")
            defaultValue
        }
    }

    fun clear() {
        mmkv?.clearAll()
    }


    fun clearKey(key: String?) {
        mmkv?.removeValueForKey(key)
    }


    fun initMMKV(context: Context) {
        MMKV.initialize(context)
    }
}