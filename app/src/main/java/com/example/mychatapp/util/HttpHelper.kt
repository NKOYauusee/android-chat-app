package com.example.mychatapp.util

import com.example.api.bean.HttpUrl
import com.example.common.util.SettingUtil

object HttpHelper {

    fun getHttpBaseUrl(): String {
        return "http://${baseUrlPart()}"
    }

    fun getWebsocketUrl(): String {
        return "ws://${baseUrlPart()}websocket/"
    }

    fun getFileUrl(address: String?): String {
        return "${getHttpBaseUrl()}files/${address ?: ""}"
    }

    private fun baseUrlPart(): String {
        val ip: String = SettingUtil.getServerIpAddress().ifEmpty { HttpUrl.IP }
        val port: String = SettingUtil.getServerPort().ifEmpty { HttpUrl.PORT }

        return "$ip:$port/nko-api/android/"
    }
}