package com.example.mychatapp.listener

import com.example.database.bean.UserFriBean

interface BlackListListener {
    fun deleteBlacklist(friend: UserFriBean, callback: () -> Unit)
}