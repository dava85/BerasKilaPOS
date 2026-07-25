package com.example.neotokopos85.utils

import java.text.SimpleDateFormat
import java.util.*

fun getWeekId(): String {

    val cal = Calendar.getInstance()

    // paksa ke Sabtu sebagai awal minggu
    val day = cal.get(Calendar.DAY_OF_WEEK)

    val diff = if (day >= Calendar.SATURDAY) {
        day - Calendar.SATURDAY
    } else {
        day + (7 - Calendar.SATURDAY)
    }

    cal.add(Calendar.DAY_OF_MONTH, -diff)

    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    return sdf.format(cal.time)
}

fun getWeekRange(time: Long): Pair<Long, Long> {

    val cal = Calendar.getInstance()
    cal.timeInMillis = time

    val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK)

    val diff = if (dayOfWeek >= Calendar.SATURDAY) {
        dayOfWeek - Calendar.SATURDAY
    } else {
        dayOfWeek + (7 - Calendar.SATURDAY)
    }

    // pindah ke Sabtu
    cal.add(Calendar.DAY_OF_MONTH, -diff)

    // 🔧 reset ke 00:00:00
    cal.set(Calendar.HOUR_OF_DAY, 0)
    cal.set(Calendar.MINUTE, 0)
    cal.set(Calendar.SECOND, 0)
    cal.set(Calendar.MILLISECOND, 0)

    val start = cal.timeInMillis

    // pindah ke Jumat
    cal.add(Calendar.DAY_OF_MONTH, 6)

    // 🔧 set ke 23:59:59
    cal.set(Calendar.HOUR_OF_DAY, 23)
    cal.set(Calendar.MINUTE, 59)
    cal.set(Calendar.SECOND, 59)
    cal.set(Calendar.MILLISECOND, 999)

    val end = cal.timeInMillis

    return start to end
}
fun getWeekIdFromDate(time: Long): String {

    val cal = Calendar.getInstance()
    cal.timeInMillis = time

    val day = cal.get(Calendar.DAY_OF_WEEK)

    val diff = if (day >= Calendar.SATURDAY) {
        day - Calendar.SATURDAY
    } else {
        day + (7 - Calendar.SATURDAY)
    }

    cal.add(Calendar.DAY_OF_MONTH, -diff)

    return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        .format(cal.time)
}
fun formatRange(start: Long, end: Long): String {
    val sdf = SimpleDateFormat("dd MMM", Locale("in", "ID"))
    return "${sdf.format(Date(start))} – ${sdf.format(Date(end))}"
}
