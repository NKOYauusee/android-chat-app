package com.example.database.helper

import android.content.Context
import com.example.common.util.UserStatusUtil
import com.example.database.UserDatabase
import com.example.database.bean.UserFriBean

object UserFriendHelper {

    fun insertFriends(context: Context, friends: MutableList<UserFriBean>) {
        UserDatabase.getInstance(context).getFriendDao().insertFriends(friends)
    }

    fun selectFriendAvatar(context: Context, who: String, friend: String): String {
        return UserDatabase.getInstance(context).getFriendDao().getFriendAvatar(who, friend)
    }

    fun insertFriend(context: Context, friend: UserFriBean) {
        UserDatabase.getInstance(context).getFriendDao().insertFriend(friend)
    }

    fun selectFriends(context: Context, email: String): MutableList<UserFriBean> {
        return UserDatabase.getInstance(context).getFriendDao().selectFriends(email)
    }

    fun selectFriendsByWords(
        context: Context,
        email: String,
        keyword: String
    ): MutableList<UserFriBean> {
        return UserDatabase.getInstance(context).getFriendDao().selectFriendsByWord(email, keyword)
    }

    fun deleteFriend(context: Context, friend: UserFriBean) {
        UserDatabase.getInstance(context).getFriendDao()
            .deleteFriend(friend.owner, friend.email)
    }

    fun batchDeleteFriend(context: Context, friendList: MutableList<UserFriBean>) {
        UserDatabase.getInstance(context).getFriendDao()
            .batchDelete( friendList)
    }
}