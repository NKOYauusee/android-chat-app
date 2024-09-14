package com.example.mychatapp.listener

import com.example.database.bean.ChatBean
import com.example.database.bean.UserFriBean

interface SearchListener {
    fun jumpToActivity(who: UserFriBean, chats: MutableList<ChatBean>, searchItem: String)
}