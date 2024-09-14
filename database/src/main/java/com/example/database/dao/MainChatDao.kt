package com.example.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.database.bean.HasChatBean

@Dao
interface MainChatDao {
    @Query("SELECT * FROM user_has_chat WHERE cur_user = :user ORDER BY send_time DESC")
    fun selectAll(user: String): MutableList<HasChatBean>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(hasChatBean: HasChatBean)

    @Delete
    fun delete(hasChatBean: HasChatBean)

    @Query("DELETE FROM user_has_chat WHERE cur_user = :owner AND email = :who")
    fun deleteMainChatShow(owner: String, who: String)

    @Query("UPDATE user_has_chat SET newest_msg = :msg, send_time = :date WHERE email = :email AND cur_user = :owner")
    fun update(owner: String, msg: String, date: Long, email: String)

    @Query("UPDATE user_has_chat SET avatar = :avatar WHERE email = :friend AND cur_user = :curLoginUser")
    fun insertProfile(curLoginUser: String, friend: String, avatar: String)
}