package com.example.mychatapp.listener

import com.example.database.bean.ChatBean
import com.shuyu.gsyvideoplayer.video.StandardGSYVideoPlayer

interface ChatMsgListener {
    // 点击预览
    fun videoPreview(videoPlayer: StandardGSYVideoPlayer, url: String, callback: () -> Unit)
    fun imagePreview(list: MutableList<ChatBean>, position: Int)
    fun download(chat: ChatBean, callback: () -> Unit)

    fun deleteMsg(chat: ChatBean, callback: () -> Unit)
}