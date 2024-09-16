package com.example.api.helper

import android.util.Log
import com.example.api.bean.HttpUrl
import com.example.api.retrofit.RetrofitClient
import com.example.api.service.ApiService
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

class ApiServiceHelper {
    private var service: ApiService? = null

    private fun apiClient(): ApiService {
        return RetrofitClient.getService(ApiService::class.java, HttpUrl.URL)
    }

    companion object {
        private val instance by lazy {
            ApiServiceHelper()
        }

        fun service(): ApiService {
            return instance.service ?: instance.apiClient()
        }

        fun initService(url: String) {
            if (url.isEmpty()) {
                Log.d("xht", "Retrofit 初始化默认配置")
                return
            }

            instance.service = RetrofitClient.getService(ApiService::class.java, url)
            if (instance.service != null)
                Log.d("xht", "Retrofit 初始化自定义配置成功")
        }

        fun <T> getRequestBody(attr: T, type: String = "text/plain"): RequestBody {
            return attr.toString().toRequestBody(type.toMediaType())
        }

        fun getBytesRequestBody(attr: ByteArray): RequestBody {
            return attr.toRequestBody(FILE_TYPE, 0, attr.size)
        }

        fun getRequestBodyPart(file: File, fileType: String): MultipartBody.Part {
            // 创建文件RequestBody
            val filePart = file.asRequestBody(fileType.toMediaType()) // 根据文件类型修改
            // 服务器字段
            return MultipartBody.Part.createFormData("fileData", file.name, filePart)
        }

        fun getRequestBodyPart(name: String, requestBody: RequestBody): MultipartBody.Part {
            return MultipartBody.Part.createFormData(
                "fileData",
                name,
                requestBody
            )
        }

        private val FILE_TYPE = "application/octet-stream".toMediaType()
    }
}