package com.example.mychatapp.viewmodel

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.example.api.bean.HttpUrl
import com.example.common.util.SettingUtil
import com.example.common.viewmodel.BaseViewModel

class SettingViewModel(application: Application) : BaseViewModel(application) {

    val serverIp = MutableLiveData<String>("")
    val serverPort = MutableLiveData<String>("")

    fun initServerInfo() {
        serverIp.postValue(SettingUtil.getServerPort() ?: HttpUrl.IP)
        serverPort.postValue(SettingUtil.getServerPort() ?: HttpUrl.PORT)
    }

    fun setServerIp(ip: String) {
        serverIp.postValue(ip)
        SettingUtil.setServerIpAddress(ip)
    }

    fun setServerPort(port: String) {
        serverPort.postValue(port)
        SettingUtil.setServerPort(port)
    }
}