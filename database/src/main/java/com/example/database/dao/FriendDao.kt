package com.example.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.database.bean.UserFriBean

@Dao
interface FriendDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertFriends(friends: MutableList<UserFriBean>)

    @Query("SELECT avatar FROM user_friend WHERE owner = :who AND email = :friend")
    fun getFriendAvatar(who: String, friend: String): String

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertFriend(friend: UserFriBean)

    @Query("SELECT * FROM user_friend WHERE owner = :owner")
    fun selectFriends(owner: String): MutableList<UserFriBean>

    @Query("SELECT * FROM user_friend WHERE owner = :owner AND (email LIKE '%' || :word || '%' OR username LIKE '%' || :word || '%' ) ")
    fun selectFriendsByWord(owner: String, word: String): MutableList<UserFriBean>

    @Query("DELETE FROM user_friend WHERE owner = :owner AND email = :friend")
    fun deleteFriend(owner: String, friend: String)

    @Delete
    fun batchDelete(friendList: MutableList<UserFriBean>)
}