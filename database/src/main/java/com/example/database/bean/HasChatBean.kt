package com.example.database.bean

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

// 主页已发起聊天的用户

@Entity("user_has_chat")
class HasChatBean {
    //当前登录的用户
    @ColumnInfo("cur_user")
    var user: String? = null

    // 好友昵称
    @ColumnInfo("nickname")
    var nickname: String? = null

    // 好友邮箱
    @PrimaryKey
    @ColumnInfo("email")
    var email: String = ""

    @ColumnInfo("newest_msg")
    var newMsg: String? = null

    @ColumnInfo("send_time")
    var sendTime: Long? = null

    @ColumnInfo("avatar")
    var avatar: String? = null;

    //新消息是否已阅
    @ColumnInfo("isRead")
    var isRead: Boolean = false;
}