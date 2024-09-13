package com.example.database.bean

import androidx.room.ColumnInfo
import androidx.room.Entity
import java.io.Serializable

@Entity("user_friend", primaryKeys = ["owner", "email"])
class UserFriBean : Serializable {
    // 好友邮箱
    @ColumnInfo("email")
    var email: String = ""

    // 好友头像
    @ColumnInfo("avatar")
    var avatar: String? = null

    // 好友昵称
    @ColumnInfo("username")
    var username: String? = null

    // 好友状态
    @ColumnInfo("status")
    var status: Int? = 0

    // 当前哪个用户的好友 用户隔离
    @ColumnInfo("owner")
    var owner: String = ""
}