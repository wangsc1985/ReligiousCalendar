package com.wang17.religiouscalendar.emnu

/**
 * Created by 阿弥陀佛 on 2015/12/4.
 */
enum class Zodiac(value: Int) {
    无(0), 鼠(1), 牛(2), 虎(3), 兔(4), 龙(5), 蛇(6), 马(7), 羊(8), 猴(9), 鸡(10), 狗(11), 猪(12);

    var value = 0

    /**
     * Zodiac -> int
     * @return
     */
    fun toInt(): Int {
        return value
    }

    companion object {
        /**
         * int -> Zodiac
         * @param value
         * @return
         */
        fun fromInt(value: Int): Zodiac? {
            return when (value) {
                0 -> 无
                1 -> 鼠
                2 -> 牛
                3 -> 虎
                4 -> 兔
                5 -> 龙
                6 -> 蛇
                7 -> 马
                8 -> 羊
                9 -> 猴
                10 -> 鸡
                11 -> 狗
                12 -> 猪
                else -> null
            }
        }

        /**
         * String -> Zodiac
         * @param value
         * @return
         */
        fun fromString(value: String?): Zodiac? {
            return when (value) {
                "无" -> 无
                "鼠" -> 鼠
                "牛" -> 牛
                "虎" -> 虎
                "兔" -> 兔
                "龙" -> 龙
                "蛇" -> 蛇
                "马" -> 马
                "羊" -> 羊
                "猴" -> 猴
                "鸡" -> 鸡
                "狗" -> 狗
                "猪" -> 猪
                else -> null
            }
        }

        fun count(): Int {
            return 13
        }
    }

    init {
        this.value = value
    }
}