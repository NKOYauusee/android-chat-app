package com.example.database.bean

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity("user_friend_apply_status")
class FriendApply {
    @ColumnInfo("target")
    var target: String? = null

    @ColumnInfo("applicant")
    var applicant: String? = null

    @ColumnInfo("applicantName")
    var applicantName: String? = null

    @ColumnInfo("info")
    var info: String? = null

    @PrimaryKey
    @ColumnInfo("time")
    var time: Long = 0

    @ColumnInfo("status")
    var status: Int = 0
}