package com.example.mychatapp.listener

import com.example.database.bean.UserBean

interface ApplyListener {
    fun sendApply(applyTarget: UserBean, info: String, callback: () -> Unit)
}