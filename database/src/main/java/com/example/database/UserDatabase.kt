package com.example.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.database.bean.ChatBean
import com.example.database.bean.FriendApply
import com.example.database.bean.HasChatBean
import com.example.database.bean.UserBean
import com.example.database.bean.UserFriBean
import com.example.database.dao.ChatDao
import com.example.database.dao.FriendDao
import com.example.database.dao.FriendStatusDao
import com.example.database.dao.MainChatDao

@Database(
    entities = [ChatBean::class, HasChatBean::class, UserBean::class, UserFriBean::class, FriendApply::class],
    version = 3, exportSchema = false
)
@TypeConverters(DbConverter::class)
abstract class UserDatabase : RoomDatabase() {

    abstract fun getChatDao(): ChatDao
    abstract fun getMainChatDao(): MainChatDao
    abstract fun getFriendDao(): FriendDao
    abstract fun getFriendStatus(): FriendStatusDao

    companion object {
        @Volatile
        private var instance: UserDatabase? = null

        @Synchronized
        fun getInstance(context: Context): UserDatabase {
            return instance ?: synchronized(this) {
                val ins = Room.databaseBuilder(
                    context.applicationContext,
                    UserDatabase::class.java,
                    "user_database"    //数据库名称
                )
                    // 使用销毁迁移
                    .fallbackToDestructiveMigration()
                    .build()
                instance = ins
                ins //返回唯一实例
            }
        }
    }
}