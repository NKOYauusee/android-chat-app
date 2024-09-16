package com.example.database.bean

import androidx.room.ColumnInfo
import androidx.room.Entity
import com.example.database.enums.MessageType
import com.flyjingfish.openimagelib.beans.OpenImageUrl
import com.flyjingfish.openimagelib.enums.MediaType
import java.io.Serializable


@Entity("user_chat_table", primaryKeys = ["send_time", "message", "owner", "receiver", "sender"])
class ChatBean : Serializable, OpenImageUrl {
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

    @ColumnInfo("msgType")
    var msgType: Int = 0
    override fun getImageUrl(): String {
        return getPath(this.message, this.msgType) ?: ""
    }

    override fun getVideoUrl(): String {
        return getPath(this.message, this.msgType) ?: ""
    }

    override fun getCoverImageUrl(): String {
        return getPath(this.message, this.msgType) ?: ""
    }

    override fun getType(): MediaType {
        //
        return if (this.msgType == MessageType.IMAGE.type)
            MediaType.IMAGE
        else MediaType.NONE
    }

    private fun getPath(src: String, type: Int): String? {
        if (type == MessageType.TEXT.type || type == MessageType.FILE.type)
            return ""

        //LogUtil.info("path -> $src")
        val startIdx = MessageType.getDescFromType(type).length + 1
        val endIdx = src.length

        if (startIdx > endIdx)
            return null

        return src.substring(startIdx, endIdx)
    }

    private fun extractFileName(url: String): String {
        return url.substringAfterLast("/")
    }
}