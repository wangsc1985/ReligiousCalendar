package com.wang17.religiouscalendar.model

/**
 * Created by 阿弥陀佛 on 2015/6/19.
 * 只用于存储农历日期，不存在阳历农历转换功能。
 */
class LunarDateTime(private val year: Int, private val month: Int, private val day: Int, private val leap: Boolean) {
    fun getYear(): Int {
        return year
    }

    fun getMonth(): Int {
        return month
    }

    fun getDay(): Int {
        return day
    }

    fun isLeap(): Boolean {
        return leap
    }

    fun getYearStr(): String {
        val res = StringBuilder()
        val str = year.toString()
        for (i in 0 until str.length) {
            res.append(toString(str[i]))
        }
        return res.toString()
    }

    fun getMonthStr(): String {
        when (month) {
            1 -> return "正月"
            2 -> return "二月"
            3 -> return "三月"
            4 -> return "四月"
            5 -> return "五月"
            6 -> return "六月"
            7 -> return "七月"
            8 -> return "八月"
            9 -> return "九月"
            10 -> return "十月"
            11 -> return "冬月"
            12 -> return "腊月"
        }
        return ""
    }

    fun getDayStr(): String {
        val res = StringBuilder()
        val str = day.toString()
        if (str.length == 1) {
            return "初" + toString(str[0])
        } else {
            when (str[0]) {
                '1' -> {
                    res.append("十")
                    if (str[1] == '0') {
                        return "初十"
                    }
                    if (str[1] != '0') {
                        res.append(toString(str[1]))
                    }
                    return res.toString()
                }
                '2' -> {
                    res.append("廿")
                    if (str[1] == '0') {
                        return "二十"
                    } else {
                        res.append(toString(str[1]))
                    }
                    return res.toString()
                }
                '3' -> return "三十"
            }
        }
        //        switch (str.charAt())
        return ""
    }

    private fun toString(number: Char): String {
        when (number) {
            '0' -> return "零"
            '1' -> return "一"
            '2' -> return "二"
            '3' -> return "三"
            '4' -> return "四"
            '5' -> return "五"
            '6' -> return "六"
            '7' -> return "七"
            '8' -> return "八"
            '9' -> return "九"
        }
        return ""
    }
}