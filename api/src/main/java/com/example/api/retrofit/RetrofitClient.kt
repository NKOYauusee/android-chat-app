package com.example.api.retrofit

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class RetrofitClient {
    companion object {
        // 拦截请求，并为该请求添加请求头
        private fun okHttpClient(
            headerMap: MutableMap<String, String> = mutableMapOf()
        ): OkHttpClient {
            val builder = OkHttpClient.Builder()
            if (headerMap.isNotEmpty()) {
                builder.addInterceptor { chain ->
                    val request = chain.request().newBuilder()
                    for (item in headerMap) {
                        request.addHeader(item.key, item.value)
                    }

                    chain.proceed(request.build())
                }
            }

            builder.connectTimeout(10_000, TimeUnit.MILLISECONDS)
            builder.readTimeout(10_000, TimeUnit.MILLISECONDS)
            builder.writeTimeout(10_000, TimeUnit.MILLISECONDS)

            return builder.build()
        }

        // 获取 Retrofit
        private fun getRetrofit(
            baseUrl: String,
            headerMap: MutableMap<String, String> = mutableMapOf()
        ): Retrofit {
            return Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(okHttpClient(headerMap))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                // 添加转换器
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }

        fun <T> getService(
            clazz: Class<T>,
            baseUrl: String,
            headerMap: MutableMap<String, String> = mutableMapOf()
        ): T {
            val retrofit = getRetrofit(baseUrl, headerMap)
            return retrofit.create(clazz)
        }

    }

}