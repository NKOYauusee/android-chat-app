package com.example.database.bean

import androidx.room.ColumnInfo
import androidx.room.Entity

@Entity("user_friend_apply_status", primaryKeys = ["time", "target", "applicant"])
class FriendApply {
    @ColumnInfo("target")
    var target: String = ""

    @ColumnInfo("targetAvatar")
    var targetAvatar: String = ""

    @ColumnInfo("applicant")
    var applicant: String = ""

    @ColumnInfo("applicantAvatar")
    var applicantAvatar: String = ""

    @ColumnInfo("applicantName")
    var applicantName: String? = null

    @ColumnInfo("info")
    var info: String? = null

    @ColumnInfo("time")
    var time: Long = 0

    @ColumnInfo("status")
    var status: Int = 0
}