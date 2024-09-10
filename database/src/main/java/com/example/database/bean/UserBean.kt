package com.example.database.bean

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity("user_info")
class UserBean {
    @ColumnInfo("id")
    var id: Int? = null

    @ColumnInfo("username")
    var username: String? = null

    @PrimaryKey
    @ColumnInfo("email")
    var email: String = ""

    @ColumnInfo("phone")
    var phone: String? = null

    @ColumnInfo("token")
    var token: String? = null

    @ColumnInfo("avatar")
    var avatar: String? = null
}