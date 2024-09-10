package com.example.common.util

import java.net.NetworkInterface
import java.net.SocketException


object DeviceUtil {

    var deviceId = ""

    /**
     * 获取mac地址有一点需要注意的就是android 6.0版本后，以下注释方法不再适用，不管任何手机都会返回"02:00:00:00:00:00"这个默认的mac地址，这是google官方为了加强权限管理而禁用了getSystemService(Context.WIFI_SERVICE)方法来获得mac地址。
     *
     * 获取设备id
     */

    fun getMacAddress(): String {

        val macAddress: String
        val buf = StringBuffer()
        var networkInterface: NetworkInterface?
        try {
            networkInterface = NetworkInterface.getByName("eth0")
            if (networkInterface == null) {
                networkInterface = NetworkInterface.getByName("eth1")
            }
            if (networkInterface == null) {
                networkInterface = NetworkInterface.getByName("wlan0")
            }
            if (networkInterface == null) {
                return ""
            }
            val addr = networkInterface.hardwareAddress
            for (b in addr) {
                buf.append(String.format("%02X:", b))
            }
            if (buf.isNotEmpty()) {
                buf.deleteCharAt(buf.length - 1)
            }
            macAddress = buf.toString()
        } catch (e: SocketException) {
            e.printStackTrace()
            return ""
        }
        return macAddress
    }
}