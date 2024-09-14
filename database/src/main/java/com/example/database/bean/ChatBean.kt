package com.example.database.bean

import androidx.room.ColumnInfo
import androidx.room.Entity
import java.io.Serializable


@Entity("user_chat_table", primaryKeys = ["send_time", "message", "owner", "receiver", "sender"])
class ChatBean : Serializable {
    @ColumnInfo("receiver")
    var receiver: String = ""

    @ColumnInfo("receiver_name")
    var receiverName: String? = null

    @ColumnInfo("sender")
    var sender: String = ""

    @ColumnInfo("sender_name")
    var senderName: String? = null

    @ColumnInfo("send_time")
    var sendTime: Long = -1 // YY-MM-dd HH-mm

    @ColumnInfo("message")
    var message: String = ""

    @ColumnInfo("owner")
    var owner: String = ""

    @ColumnInfo("type")
    var type: Int = 0
}