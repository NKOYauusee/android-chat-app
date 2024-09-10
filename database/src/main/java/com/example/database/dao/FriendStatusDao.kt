package com.example.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.database.bean.FriendApply


@Dao
interface FriendStatusDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertFriendApplyStatus(friendApply: FriendApply)

    @Query("DELETE FROM user_friend_apply_status WHERE time < :date AND (target = :who OR applicant = :who)")
    fun deleteFriendApply(who: String, date: Long)
}