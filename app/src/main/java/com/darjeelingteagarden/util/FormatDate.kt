package com.darjeelingteagarden.util

import java.text.SimpleDateFormat
import java.util.*

fun String.toDate(dateFormat: String = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timeZone: TimeZone = TimeZone.getTimeZone("UTC")): Date? {
    val parser = SimpleDateFormat(dateFormat, Locale.getDefault())
    parser.timeZone = timeZone
    return parser.parse(this)
}

fun Date.formatTo(dateFormat: String, timeZone: TimeZone = TimeZone.getDefault()): String {
    val formatter = SimpleDateFormat(dateFormat, Locale.getDefault())
    formatter.timeZone = timeZone
    return formatter.format(this)
}
//class FormatDate {
//
////    fun changeDateFormat(utcDate: String): String {
////
//////        2022-10-17T05:39:07.150Z
//////        012345678901234567890123
////
////        val months = arrayListOf(
////            "January",
////            "February",
////            "March",
////            "April",
////            "May",
////            "June",
////            "July",
////            "August",
////            "September",
////            "October",
////            "November",
////            "December"
////        )
////
////        val year = utcDate.substring(0, 4)
////        val month = utcDate.substring(5, 7)
////        val date = utcDate.substring(8, 10)
////        val hour = utcDate.substring(11, 13)
////        val minute = utcDate.substring(14, 16)
////
////        return "$date ${months[month.toInt()-1]}, $year at $hour:$minute"
////
////    }
//
//
//
//}

