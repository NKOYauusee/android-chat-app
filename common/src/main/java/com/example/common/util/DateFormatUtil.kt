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
        val gap: Long = Date().time - time
        return when {
            (gap < Constants.ONE_DAY) -> {
                formatTime(time, "HH:mm")
            }

            (gap <= (Constants.ONE_DAY * 2)) -> {
                formatTime(time, "昨天 ${formatTime(time, "HH:mm")}")
            }

            (gap <= (Constants.ONE_DAY * 30)) -> {
                formatTime(time, "MM月dd HH:mm")
            }

            else -> {
                formatTime(time)
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