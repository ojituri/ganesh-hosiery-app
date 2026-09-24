package com.ganeshhosiery.autoreply.core

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object TimeUtils {

    /** 12:00 midnight at the start of today, in milliseconds. */
    fun startOfToday(): Long {
        val c = Calendar.getInstance()
        c.set(Calendar.HOUR_OF_DAY, 0)
        c.set(Calendar.MINUTE, 0)
        c.set(Calendar.SECOND, 0)
        c.set(Calendar.MILLISECOND, 0)
        return c.timeInMillis
    }

    fun timeText(millis: Long): String =
        SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(millis))

    fun dateTimeText(millis: Long): String =
        SimpleDateFormat("d MMM yyyy, h:mm a", Locale.getDefault()).format(Date(millis))

    /** "Today", "Yesterday" or "12 Sep 2026". */
    fun dayLabel(millis: Long): String {
        val today = startOfToday()
        val oneDay = 24L * 60 * 60 * 1000
        return when {
            millis >= today -> "Today"
            millis >= today - oneDay -> "Yesterday"
            else -> SimpleDateFormat("d MMM yyyy", Locale.getDefault()).format(Date(millis))
        }
    }
}
