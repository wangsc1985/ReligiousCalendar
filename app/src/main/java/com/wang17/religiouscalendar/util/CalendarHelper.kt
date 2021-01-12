package com.wang17.religiouscalendar.util

import java.util.*

/**
 * Created by 阿弥陀佛 on 2015/6/23.
 */
object CalendarHelper {
    /**
     * 比较两个时间是否在同一天。
     * 注：只比较年、月、日是否相同，忽略时、分、秒、毫秒、微妙。
     *
     * @param calendar1
     * @param calendar2
     * @return
     */
    fun isSameDate(calendar1: Calendar, calendar2: Calendar): Boolean {
        return if (calendar1[Calendar.YEAR] == calendar2[Calendar.YEAR] && calendar1[Calendar.MONTH] == calendar2[Calendar.MONTH] && calendar1[Calendar.DAY_OF_MONTH] == calendar2[Calendar.DAY_OF_MONTH]) {
            true
        } else {
            false
        }
    }

    /**
     * 计算两个时间变量之间相隔的天数，计算精确到毫秒。
     *
     * @param calendar1
     * @param calendar2
     * @return
     */
    fun spanTotalDays(calendar1: Calendar?, calendar2: Calendar?): Long {
        return (calendar1!!.timeInMillis - calendar2!!.timeInMillis) / 1000 / 60 / 60 / 24
    }

    fun spanTotalHours(calendar1: Calendar?, calendar2: Calendar?): Long {
        return (calendar1!!.timeInMillis - calendar2!!.timeInMillis) / 1000 / 60 / 60
    }

    /**
     * 得到calendar的日期（时、分、秒、毫秒清零后的calendar）。
     * @param calendar
     * @return
     */
    fun getDate(calendar: Calendar): Calendar {
        val cal = calendar.clone() as Calendar
        cal[Calendar.HOUR_OF_DAY] = 0
        cal[Calendar.MINUTE] = 0
        cal[Calendar.SECOND] = 0
        cal[Calendar.MILLISECOND] = 0
        return cal
    }
}