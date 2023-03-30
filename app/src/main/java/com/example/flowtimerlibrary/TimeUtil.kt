package com.example.flowtimerlibrary

import java.text.SimpleDateFormat
import java.util.*


fun String.toTimePattern(timeZone: TimeZone = TimeZone.getTimeZone("UTC"), locale: Locale = Locale.getDefault()): TimePattern {
    return object : TimePattern {
        override val pattern: String
            get() = this@toTimePattern
        override val timeZone: TimeZone
            get() = timeZone
        override val locale: Locale
            get() = locale
    }
}

fun TimePattern.getFormatter(): SimpleDateFormat {
    val formatter = SimpleDateFormat(this.pattern, this.locale)
    formatter.timeZone = this.timeZone
    return formatter
}

fun TimePattern.turnSecondIntoString(second: Int): String {
    return getFormatter().format(Date(second * 1000L))
}

fun TimePattern.turnMillisSecondIntoString(millisSecond: Long): String {
    return getFormatter().format(Date(millisSecond))
}

fun TimePattern.getCurrentTime(): String {
    return getFormatter().format(Date())
}

fun TimePattern.isOverSeventyTwoHours(timeString: String): Boolean {

    val formatter = getFormatter()

    val scanTime = formatter.parse(timeString)

    val cal = Calendar.getInstance()
    cal.time = scanTime
    cal.add(Calendar.HOUR, 72)

    //加上72小時後的時間
    val futureDate = cal.time

    return futureDate.before(Calendar.getInstance().time)
}

fun TimePattern.getCalenderString(calendar: Calendar): String {

    val formatter = getFormatter()

    return formatter.format(calendar.time)
}

fun TimePattern.getTimeFormatData(afterTimePattern: String, timeString: String): String? {

    val thisFormat = getFormatter()

    val formatterOut = afterTimePattern.toTimePattern(thisFormat.timeZone, this.locale).getFormatter()

    return try {
        val date = thisFormat.parse(timeString)
        formatterOut.format(date)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

fun TimePattern.transStringDateToCalender(timeString: String): Calendar {
    val calendar = Calendar.getInstance()
    return try {
        val date = this.turnDateTimeStringToDate(timeString)
        calendar.time = date
        calendar
    } catch (e: Exception) {
        e.printStackTrace()
        calendar
    }
}

fun TimePattern.turnDateTimeStringToDate(timeString: String): Date? {
    val simpleDateFormat = getFormatter()
    return simpleDateFormat.parse(timeString)
}

/**
 * ISO8601:                  yyyy.MM.dd'T'HH:MM:ss
 * Date:                     yyyy.MM.dd
 * DateTime:                 yyyy.MM.dd HH:mm:ss
 * DatetimeCompress:         yyyyMMdd_HHmmss
 * DateTimeWithoutSecond:    yyyy.MM.dd HH:mm
 * TimeWithoutHour:          mm:ss
 * TimeWithoutSecond:        HH:mm
 * Second                    ss
 * AlarmDate                 yyyy.MM.dd(`週`day)
 */
object TimeUtil {

    fun getSystemTimeStampString(): String {
        val currentTimestamp = System.currentTimeMillis()

        return currentTimestamp.toString()
    }

    fun checkIsDatesSameDay(cal1: Calendar, cal2: Calendar): Boolean {
        return cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR) &&
                cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR)
    }
}