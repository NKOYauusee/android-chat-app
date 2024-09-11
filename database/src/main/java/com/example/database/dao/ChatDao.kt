package com.example.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.database.bean.ChatBean

@Dao
interface ChatDao {

    //加载和某人最近2天的10条聊天记录
    @Query("SELECT * FROM user_chat_table WHERE send_time > :sendTime AND `owner` = :key AND (receiver = :friend OR sender = :friend) ORDER BY send_time DESC LIMIT 10")
    fun loadRecentMsg(key: String, sendTime: Long, friend: String): MutableList<ChatBean>


    @Query("SELECT * FROM user_chat_table WHERE `owner` = :key ORDER BY send_time DESC LIMIT 1")
    fun loadNewestMsg(key: String): ChatBean

    //加载之前10条的聊天记录

    //加载最近2天的聊天记录

    //删除聊天记录
    //@Query("delete FROM user_chat_table WHERE `order` = :order")
    //fun deleteOneMsg(order: Int)

    //TODO 批量删除聊天记录
    //@Query("delete FROM user_chat_table WHERE )
    //fun deleteMsgList()

    //删除所有聊天记录
    @Query("DELETE FROM user_chat_table WHERE (receiver = :curUser OR sender = :curUser)")
    fun deleteAllMsg(curUser: String)

    //删除和某人的聊天记录
    @Query("delete FROM user_chat_table WHERE `owner` = :key")
    fun deleteMsg(key: String)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertChats(chats: MutableList<ChatBean>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertChat(chat: ChatBean)


    @Query("SELECT * FROM user_chat_table WHERE owner = :owner AND (receiver = :who OR sender = :who) AND  message LIKE '%' || :keyword || '%'")
    fun loadRelativeMsgWithSb(owner: String, who: String, keyword: String): MutableList<ChatBean>
}