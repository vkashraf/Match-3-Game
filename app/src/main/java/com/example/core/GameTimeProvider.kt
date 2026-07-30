package com.example.core

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object GameTimeProvider {

    fun getCurrentTimeMillis(): Long {
        return System.currentTimeMillis()
    }

    fun getCurrentDateString(timestamp: Long = getCurrentTimeMillis()): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        return sdf.format(Date(timestamp))
    }

    fun isSameDay(timestamp1: Long, timestamp2: Long): Boolean {
        if (timestamp1 <= 0 || timestamp2 <= 0) return false
        val cal1 = Calendar.getInstance().apply { timeInMillis = timestamp1 }
        val cal2 = Calendar.getInstance().apply { timeInMillis = timestamp2 }
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }

    fun daysBetween(startTime: Long, endTime: Long): Int {
        if (startTime <= 0 || endTime <= 0) return 0
        val cal1 = Calendar.getInstance().apply {
            timeInMillis = startTime
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val cal2 = Calendar.getInstance().apply {
            timeInMillis = endTime
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val diffMillis = cal2.timeInMillis - cal1.timeInMillis
        return (diffMillis / (1000 * 60 * 60 * 24)).toInt()
    }
}
