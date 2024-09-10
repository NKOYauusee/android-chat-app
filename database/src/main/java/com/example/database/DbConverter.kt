package com.example.database

import androidx.room.TypeConverter
import java.util.Date

// date类型的转换器
class DbConverter {
    @TypeConverter
    fun fromTimeStamp(value: Long?): Date? {
        return value?.let {
            Date(it)
        }
    }

    @TypeConverter
    fun dateToTimeStamp(date: Date?): Long? {
        return date?.time
    }
}