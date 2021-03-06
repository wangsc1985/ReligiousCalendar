package com.wang17.religiouscalendar.model

import com.wang17.religiouscalendar.util._String
import java.util.*

/**
 * Created by 阿弥陀佛 on 2015/6/24.
 */
class DateTime : GregorianCalendar {
    constructor() {
        this.timeZone = TimeZone.getTimeZone("Asia/Shanghai")
    }

    constructor(timeInMillis: Long) {
        this.timeZone = TimeZone.getTimeZone("Asia/Shanghai")
        this.timeInMillis = timeInMillis
    }

    constructor(year: Int, month: Int, day: Int) {
        this.timeZone = TimeZone.getTimeZone("Asia/Shanghai")
        this[year, month, day, 0, 0] = 0
        this[MILLISECOND] = 0
    }

    constructor(year: Int, month: Int, day: Int, hour: Int, minute: Int, second: Int) {
        this.timeZone = TimeZone.getTimeZone("Asia/Shanghai")
        this[year, month, day, hour, minute] = second
        this[MILLISECOND] = 0
    }

    /**
     * 返回一个时、分、秒、毫秒置零的此DateTime副本。
     *
     * @return
     */
    fun getDate(): DateTime {
        return DateTime(get(YEAR), get(MONTH), get(DAY_OF_MONTH))
    }

    fun addMonths(months: Int): DateTime {
        val dateTime = clone() as DateTime
        dateTime.add(MONTH, months)
        return dateTime
    }

    fun addDays(days: Int): DateTime {
        val dateTime = clone() as DateTime
        dateTime.add(DAY_OF_MONTH, days)
        return dateTime
    }

    fun addHours(hours: Int): DateTime {
        val dateTime = clone() as DateTime
        dateTime.add(HOUR_OF_DAY, hours)
        return dateTime
    }

    fun getYear(): Int {
        return this[YEAR]
    }

    fun getMonth(): Int {
        return this[MONTH]
    }

    fun getDay(): Int {
        return this[DAY_OF_MONTH]
    }

    fun getHour(): Int {
        return this[HOUR_OF_DAY]
    }

    fun getMinite(): Int {
        return this[MINUTE]
    }

    fun getSecond(): Int {
        return this[SECOND]
    }

    fun toShortDateString(): String {
        return _String.concat(getYear(), "年", getMonth() + 1, "月", getDay(), "日")
    }

    /**
     * 格式：****年**月**日  **:**:**
     *
     * @return
     */
    fun toLongDateString(): String {
        return _String.concat(toShortDateString(), "  ", toTimeString())
    }

    /**
     * 格式：**:**:**
     *
     * @return
     */
    fun toTimeString(): String {
        return _String.concat(getHourStr(), ":", getMiniteStr(), ":", getSecondStr())
    }

    fun getMonthStr(): String {
        val tt = getMonth() + 1
        return if (tt < 10) "0$tt" else "" + tt
    }

    fun getDayStr(): String {
        val tt = getDay()
        return if (tt < 10) "0$tt" else "" + tt
    }

    fun getHourStr(): String {
        val tt = getHour()
        return if (tt < 10) "0$tt" else "" + tt
    }

    fun getMiniteStr(): String {
        val tt = getMinite()
        return if (tt < 10) "0$tt" else "" + tt
    }

    fun getSecondStr(): String {
        val tt = getSecond()
        return if (tt < 10) "0$tt" else "" + tt
    }

    companion object {
        fun getToday(): DateTime {
            val today = DateTime()
            return today.getDate()
        }

        /**
         * 格式：*天*小时
         *
         * @param timeInHours
         * @return
         */
        fun toSpanString(timeInHours: Int): String {
            var resutl = ""
            val day = (timeInHours / 24)
            val hour = (timeInHours % 24)
            resutl += if (day > 0) day.toString() + "天" else ""
            resutl += if (hour > 0) hour.toString() + "小时" else ""
            if (day == 0 && hour == 0) resutl = hour.toString() + "小时"
            return resutl
        }
    }
}