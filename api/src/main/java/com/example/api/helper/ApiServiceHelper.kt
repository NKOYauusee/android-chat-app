package com.example.api.helper

import com.example.api.bean.HttpUrl
import com.example.api.retrofit.RetrofitClient
import com.example.api.service.ApiService

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
    }
}