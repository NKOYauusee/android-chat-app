package com.example.mychatapp.listener

import com.example.database.bean.UserFriBean


interface UserListener {
    fun onUserClicked(friend: UserFriBean)

    fun blackListFriend(friend: UserFriBean, callback: (() -> Unit) = {})
    fun deleteFriend(friend: UserFriBean, callback: (() -> Unit) = {})
}