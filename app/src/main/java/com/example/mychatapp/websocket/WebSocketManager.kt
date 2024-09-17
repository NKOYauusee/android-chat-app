package com.example.mychatapp.websocket

import android.content.Context
import android.util.Log
import com.example.common.util.LogUtil
import com.example.common.util.UserStatusUtil
import com.example.database.bean.ChatBean
import com.example.database.helper.ChatListHelper
import com.example.database.helper.MainUserSelectHelper
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.net.URI

class WebSocketManager {
    private var websocketClient: MyWebSocketClient? = null
    private val gson = Gson()

    fun connect(url: String, token: String) {
        if (websocketClient?.isOpen == true)
            stopConn()

        val uri = URI(url)
        websocketClient = MyWebSocketClient(uri, this)
        websocketClient!!.addHeader("token", token)
        websocketClient!!.connect()
    }

    fun stopConn() {
        websocketClient?.isReconnect(false)
        websocketClient?.close()
        websocketClient = null
    }

    fun getClientState(): Boolean {
        if (websocketClient == null)
            return false

        return websocketClient!!.isOpen
    }

    fun reconnect() {
        GlobalScope.launch {
            websocketClient?.reconnect()
        }
    }

    fun sendMsg(chatBean: ChatBean, callback: () -> Unit = {}) {
        LogUtil.info("send-> " + gson.toJson(chatBean))
        try {
            websocketClient?.sendMsg(chatBean)
        } catch (e: Exception) {
            callback()
            Log.e("xht", "sendMsg: ", e)
        }
    }

    fun receiveMessage(context: Context, callback: (chatBean: ChatBean) -> Unit) {
        websocketClient?.loadMsg = {
            val chat = gson.fromJson(it, ChatBean::class.java)
            chat.owner = UserStatusUtil.getCurLoginUser()
            LogUtil.info("消息：${gson.toJson(chat)}")
            GlobalScope.launch(Dispatchers.IO) {
//                val friend =
//                    if (chat.sender == UserStatusUtil.getCurLoginUser()) chat.receiver
//                    else chat.sender
//                ChatListHelper.saveOneChat(context, chat, friend!!)
                ChatListHelper.saveOneChat(context, chat)
                MainUserSelectHelper.updateMainUser(context, chat)
            }

            callback(chat)
        }
    }

    companion object {
        val instance by lazy {
            WebSocketManager()
        }
    }
}