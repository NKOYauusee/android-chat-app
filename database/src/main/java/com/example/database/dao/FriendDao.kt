package com.example.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.database.bean.UserFriBean

@Dao
interface FriendDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertFriends(friends: MutableList<UserFriBean>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertFriend(friend: UserFriBean)

    @Query("SELECT * FROM user_friend WHERE owner = :owner")
    fun selectFriends(owner: String): MutableList<UserFriBean>

    @Query("SELECT * FROM user_friend WHERE owner = :owner AND (email LIKE '%' || :word || '%' OR username LIKE '%' || :word || '%' ) ")
    fun selectFriendsByWord(owner: String, word: String): MutableList<UserFriBean>

    @Query("DELETE FROM user_friend WHERE owner = :owner AND email = :friend")
    fun deleteFriend(owner: String, friend: String)
}