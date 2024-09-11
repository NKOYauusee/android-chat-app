package com.example.common.util

import android.icu.text.SimpleDateFormat
import com.example.common.common.Constants
import java.util.Calendar
import java.util.Date
import java.util.Locale

object DateFormatUtil {

    fun formatTime(time: Long, pattern: String = "yyyy年MM月dd HH:mm"): String {
        val dateFormat = SimpleDateFormat(pattern, Locale.getDefault())
        return dateFormat.format(Date(time))
    }

    fun getFormatTime(time: Long): String {
        // 获取当前日期的零点时间（即今天开始的时间）
        val todayStart = getStartOfDay(System.currentTimeMillis())

        // 获取昨天日期的零点时间（即昨天开始的时间）
        val yesterdayStart = todayStart - Constants.ONE_DAY

        val gap: Long = Date().time - time
        return when {
            (time >= todayStart) -> {
                formatTime(time, "HH:mm")
            }

            (time in yesterdayStart until todayStart) -> {
                "昨天 ${formatTime(time, "HH:mm")}"
            }

            (gap in Constants.ONE_DAY * 30 until Constants.ONE_DAY * 365) -> {
                formatTime(
                    time,
                    "MM月dd HH:mm"
                )
            }

            else -> {
                return formatTime(time)
            }
        }

    }


    fun getStartOfDay(timeMillis: Long): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timeMillis
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

}