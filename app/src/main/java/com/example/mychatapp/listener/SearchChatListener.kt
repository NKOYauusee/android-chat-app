package com.example.mychatapp.listener

import com.example.database.bean.ChatBean
import com.example.database.bean.UserFriBean

interface SearchChatListener {
    fun toChatActivityWithData(userFriBean: UserFriBean, chat: ChatBean)
}