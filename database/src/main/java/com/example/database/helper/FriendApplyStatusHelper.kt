package com.example.database.helper

import android.content.Context
import com.example.common.common.Constants
import com.example.database.UserDatabase
import com.example.database.bean.FriendApply
import java.util.Date

object FriendApplyStatusHelper {

    fun insertFriendApply(context: Context, friendApply: FriendApply) {
        UserDatabase.getInstance(context).getFriendStatus().insertFriendApplyStatus(friendApply)
    }

    fun deleteExpiredFriendApply(context: Context, who: String) {
        val recent2day = Date().time - Constants.ONE_DAY * 2
        UserDatabase.getInstance(context).getFriendStatus().deleteFriendApply(who, recent2day)
    }
}