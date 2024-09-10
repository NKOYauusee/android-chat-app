package com.example.mychatapp.listener

import com.example.database.bean.FriendApply

interface FriendApplyStatusListener {
    fun setApplyStatus(friendApply: FriendApply)
}