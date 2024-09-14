package com.example.mychatapp.listener

import com.example.database.bean.HasChatBean

interface MainChatListener {
    fun onClicked(hasChat: HasChatBean)

    fun deleteMsg(target: HasChatBean, callback: () -> Unit)
}