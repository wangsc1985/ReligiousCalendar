package com.wang17.religiouscalendar.model

import com.wang17.religiouscalendar.util._String
import java.util.*

/**
 * Created by 阿弥陀佛 on 2015/6/24.
 * 只存储农历月和农历日的类
 */
class LunarDate {
    companion object {
        val Months: MutableList<String> = ArrayList()
        private val str = arrayOf("", "一", "二", "三", "四", "五", "六", "七", "八", "九", "十")
        val Days: MutableList<String> = ArrayList()

        init {
            Months.add("正月")
            Months.add("二月")
            Months.add("三月")
            Months.add("四月")
            Months.add("五月")
            Months.add("六月")
            Months.add("七月")
            Months.add("八月")
            Months.add("九月")
            Months.add("十月")
            Months.add("十一月")
            Months.add("十二月")
        }

        init {
            for (i in 1..10) {
                Days.add("初" + str[i])
            }
            for (i in 1..9) {
                Days.add("十" + str[i])
            }
            Days.add("二十")
            for (i in 1..9) {
                Days.add("廿" + str[i])
            }
            Days.add("三十")
        }
    }

    var month: Int
    var day: Int

    constructor(lunarMonth: String, lunarDay: String) {
        month = Months!!.indexOf(lunarMonth) + 1
        day = Days!!.indexOf(lunarDay) + 1
    }

    constructor(lunarMonth: Int, lunarDay: Int) {
        month = lunarMonth
        day = lunarDay
    }

    override fun equals(obj: Any?): Boolean {
        val md = obj as LunarDate?
        return md!!.month == month && md.day == day
    }

    override fun hashCode(): Int {
        return _String.concat(_String.format(month), _String.format(day)).hashCode()
    }

    override fun toString(): String {
        return Months!![month - 1] + Days!![day - 1]
    }
}