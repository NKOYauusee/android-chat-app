package com.example.api.helper

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
    private fun apiClient(): ApiService {
        return RetrofitClient.getService(ApiService::class.java, HttpUrl.URL)
    }

    companion object {
        private val instance by lazy {
            ApiServiceHelper()
        }

        fun service(): ApiService {
            return instance.apiClient()
        }

        fun <T> getRequestBody(attr: T, type: String): RequestBody {
            return attr.toString().toRequestBody(type.toMediaType())
        }

        fun getRequestBodyPart(file: File, fileType: String): MultipartBody.Part {
            // 创建文件RequestBody
            val filePart = file.asRequestBody(fileType.toMediaType()) // 根据文件类型修改
            // 服务器字段
            return MultipartBody.Part.createFormData("fileData", file.name, filePart)
        }
    }
}