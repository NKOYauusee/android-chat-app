package com.example.database.bean

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity("user_chat_table")
class ChatBean {
    @ColumnInfo("receiver")
    var receiver: String? = null

    @ColumnInfo("receiver_name")
    var receiverName: String? = null

    @ColumnInfo("sender")
    var sender: String? = null

    @ColumnInfo("sender_name")
    var senderName: String? = null

    @PrimaryKey
    @ColumnInfo("send_time")
    var sendTime: Long = -1 // YY-MM-dd HH-mm

    @ColumnInfo("message")
    var message: String? = null

    @ColumnInfo("owner")
    var owner: String = ""

    @ColumnInfo("type")
    var type: Int = 0
}