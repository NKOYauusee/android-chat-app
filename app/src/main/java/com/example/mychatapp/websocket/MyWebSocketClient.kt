package com.example.mychatapp.websocket

import android.util.Log
import com.example.database.bean.ChatBean
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import java.net.URI

class MyWebSocketClient(uri: URI) : WebSocketClient(uri) {
    private val gson = Gson()
    private var reconnect = true
    private var socketManager: WebSocketManager? = null

    constructor(uri: URI, socketManager: WebSocketManager) : this(uri) {
        this.socketManager = socketManager
    }

    override fun onOpen(sh: ServerHandshake) {
        Log.d("xht", "ws client onOpen")
    }

    override fun onMessage(msg: String) {
        //handleMessage(msg)
        loadMsg?.let { it(msg) }
    }

    override fun onClose(code: Int, reason: String?, remote: Boolean) {
        if (reconnect) {
            GlobalScope.launch(Dispatchers.IO) {
                delay(3 * 1000)
                socketManager?.reconnect()
            }
        }
    }

    fun isReconnect(reconnect: Boolean) {
        this.reconnect = reconnect
    }

    override fun onError(e: Exception?) {
    }

    fun sendMsg(chatBean: ChatBean) {
        send(gson.toJson(chatBean))
    }

    // 接收消息
    private fun handleMessage(msg: String) {
        val data = gson.fromJson(msg, MutableMap::class.java)
        Log.d("xht", "handleMessage: $data")
    }

    var loadMsg: ((msg: String) -> Unit)? = null
}