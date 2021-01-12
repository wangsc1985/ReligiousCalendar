package com.wang17.religiouscalendar.util

/**
 * Created by 阿弥陀佛 on 2015/6/23.
 */
object _String {
    fun concat(vararg strings: Any?): String {
        val sb = StringBuilder()
        for (str in strings) {
            if (str != null) sb.append(str.toString())
        }
        return sb.toString()
    }

    fun IsNullOrEmpty(str: String?): Boolean {
        return if (str == null || str.length == 0) true else false
    }

    /**
     * 时间字段，月、日、时、分、秒，小于10的，设置前缀‘0’。
     *
     * @param x
     * @return
     */
    fun format(x: Int): String {
        var s = "" + x
        if (s.length == 1) s = "0$s"
        return s
    }
}