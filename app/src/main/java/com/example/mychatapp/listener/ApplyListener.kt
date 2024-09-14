package com.example.mychatapp.listener

import com.example.database.bean.UserBean

interface ApplyListener {
    fun loadMore()
    fun sendApply(applyTarget: UserBean, info: String, callback: () -> Unit)
}