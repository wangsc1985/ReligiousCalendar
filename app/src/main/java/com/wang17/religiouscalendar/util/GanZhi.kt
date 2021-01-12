package com.wang17.religiouscalendar.util

import com.wang17.religiouscalendar.emnu.SolarTerm
import com.wang17.religiouscalendar.model.DateTime
import java.util.*

/**
 * Created by 阿弥陀佛 on 2015/6/19.
 */
class GanZhi(dateTime: DateTime, solarTermTreeMap: TreeMap<DateTime, SolarTerm>) {
    private val solarTermMap: Map<DateTime, SolarTerm>
    private val tianGan = arrayOf("癸", "甲", "乙", "丙", "丁", "戊", "己", "庚", "辛", "壬", "癸")
    private val diZhi = arrayOf("亥", "子", "丑", "寅", "卯", "辰", "巳", "午", "未", "申", "酉", "戌", "亥")
    private val monthDiZhi = arrayOf("丑", "寅", "卯", "辰", "巳", "午", "未", "申", "酉", "戌", "亥", "子", "丑")
    var tianGanYear: String? = null
        private set
    var tianGanMonth: String? = null
        private set
    var tianGanDay: String? = null
        private set
    var tianGanHour: String? = null
        private set
    var diZhiYear: String? = null
        private set
    var diZhiMonth: String? = null
        private set
    var diZhiDay: String? = null
        private set
    var diZhiHour: String? = null
        private set
    val zodiac: String
        get() {
            when (diZhiYear) {
                "亥" -> return "猪"
                "子" -> return "鼠"
                "丑" -> return "牛"
                "寅" -> return "虎"
                "卯" -> return "兔"
                "辰" -> return "龙"
                "巳" -> return "蛇"
                "午" -> return "马"
                "未" -> return "羊"
                "申" -> return "猴"
                "酉" -> return "鸡"
                "戌" -> return "狗"
            }
            return ""
        }

    /// <summary>
    /// 根据公历日期得到四柱天干、农历月、农历日
    /// </summary>
    /// <param name="dateTime"></param>
    @Throws(Exception::class)
    private fun convertToGanZhi(dateTime: DateTime) {
        if (dateTime.compareTo(minimalDate) == -1 || dateTime.compareTo(maximalDate) == 1) {
            throw Exception(_String.concat("当前时间：【", dateTime.toShortDateString(), "】超出时间允许范围【",
                    minimalDate.getYear(), "年", minimalDate.getMonth(), "月", minimalDate.getDay(), "日",
                    " - ",
                    maximalDate.getYear(), "年", maximalDate.getMonth(), "月", maximalDate.getDay(), "日", "】！"))
        }
        // 年干支
        val cal = DateTime(2015, 1, 1)
        val today = dateTime.getDate()
        var years = today.getYear() - 1955
        for ((key, value) in solarTermMap) {
            if (key.getYear() == today.getYear() && value === SolarTerm.立春) {
                cal[key.getYear(), key.getMonth(), key.getDay(), 0, 0] = 0
                break
            }
        }
        if (today.compareTo(cal) == -1) {
            years--
        }
        tianGanYear = tianGan[((years % 10) as Int + 2) % 10]
        diZhiYear = diZhi[((years % 12) as Int + 8) % 12]

        // 月干支
        var nextSolarTerm: Map.Entry<DateTime, SolarTerm>? = null
        for (entry in solarTermMap.entries) {
            cal[entry.key.getYear(), entry.key.getMonth(), entry.key.getDay(), 0, 0] = 0
            if (cal.compareTo(today) == 1) {
                nextSolarTerm = entry
                break
            }
        }
        var chineseMonth = 0
        chineseMonth = if (nextSolarTerm != null) {
            (nextSolarTerm.value.value - 1) / 2 + 1
        } else {
            11
        }
        tianGanMonth = GetTianGanMonth(tianGanYear, chineseMonth)
        diZhiMonth = monthDiZhi[chineseMonth]

        // 日干支  1800年1月1日00:00  庚寅日 丙子时
        val normDateTime = DateTime(1800, 0, 1, 0, 0, 0).addHours(-1)
        val totalDays = CalendarHelper.spanTotalDays(dateTime, normDateTime)
        tianGanDay = tianGan[((totalDays % 10).toInt() + 7) % 10]
        diZhiDay = diZhi[((totalDays % 12).toInt() + 3) % 12]

        // 时干支
        val totalHours = CalendarHelper.spanTotalHours(dateTime, normDateTime)
        tianGanHour = tianGan[((totalHours / 2 % 10).toInt() + 3) % 10]
        diZhiHour = diZhi[((totalHours / 2 % 12).toInt() + 1) % 12]
    }

    private fun GetTianGanMonth(tianGanYear: String?, chineseMonth: Int): String? {
        // 甲己之年丙作首
        if (tianGanYear == "甲" || tianGanYear == "己") {
            return tianGan[(2 + chineseMonth) % 10]
        } else if (tianGanYear == "乙" || tianGanYear == "庚") {
            return tianGan[(4 + chineseMonth) % 10]
        } else if (tianGanYear == "丙" || tianGanYear == "辛") {
            return tianGan[(6 + chineseMonth) % 10]
        } else if (tianGanYear == "丁" || tianGanYear == "壬") {
            return tianGan[(8 + chineseMonth) % 10]
        } else if (tianGanYear == "戊" || tianGanYear == "癸") {
            return tianGan[chineseMonth % 10]
        }
        return null
    }

    companion object {
        val minimalDate = DateTime(1999, 1, 1, 1, 0, 0)
        val maximalDate = DateTime(2050, 12, 31, 22, 59, 59)
    }

    /**
     * 根据给定的公历时间得到对应的八字。
     *
     * @param dateTime
     * @param solarTermTreeMap 必须是“按Key（也就是时间）升序排列的”TreeMap
     * @throws StackOverflowError 时间超出范围时会抛出的异常。
     */
    init {
        solarTermMap = solarTermTreeMap
        convertToGanZhi(dateTime)
    }
}