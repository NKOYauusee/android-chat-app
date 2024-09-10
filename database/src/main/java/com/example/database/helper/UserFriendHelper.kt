package com.example.database.helper

import android.content.Context
import com.example.database.UserDatabase
import com.example.database.bean.UserFriBean

object UserFriendHelper {

    fun insertFriends(context: Context, friends: MutableList<UserFriBean>) {
        UserDatabase.getInstance(context).getFriendDao().insertFriends(friends)
    }

    fun insertFriend(context: Context, friend: UserFriBean) {
        UserDatabase.getInstance(context).getFriendDao().insertFriend(friend)
    }

    fun selectFriends(context: Context, email: String): MutableList<UserFriBean> {
        return UserDatabase.getInstance(context).getFriendDao().selectFriends(email)
    }

    fun deleteFriend(context: Context, friend: UserFriBean) {
        UserDatabase.getInstance(context).getFriendDao()
            .deleteFriend(friend.owner!!, friend.email)
    }
}