package com.example.mychatapp.util

import com.example.api.bean.HttpUrl
import com.example.common.util.UserStatusUtil
import com.example.database.bean.ChatBean
import com.example.database.enums.MessageType

object ChatHelper {
    // 代发送消息
    fun generateChat(
        receiverEmail: String,
        receiverName: String?,
        msg: String,
        type: Int = 0
    ): ChatBean {
        val chatBean = ChatBean()
        chatBean.receiver = receiverEmail
        chatBean.receiverName = receiverName

        chatBean.sender = UserStatusUtil.getCurLoginUser()
        chatBean.senderName = UserStatusUtil.getUsername()
        //聊天记录所有者
        chatBean.owner = UserStatusUtil.getCurLoginUser()
        chatBean.msgType = type

        val msgType = MessageType.getDescFromType(type)
        if (msgType == null) chatBean.message = msg
        else chatBean.message = "$msgType:${HttpHelper.getFileUrl(null)}$msg"

        return chatBean
    }

    // 获取非文本类型中的地址
    fun getPath(src: String, type: Int): String? {
        val startIdx = MessageType.getDescFromType(type).length + 1
        val endIdx = src.length

        if (startIdx > endIdx)
            return null

        return src.substring(startIdx, endIdx)
    }
}