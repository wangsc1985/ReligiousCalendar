package com.wang17.religiouscalendar.emnu

/**
 * Created by 阿弥陀佛 on 2015/6/30.
 */
enum class MDrelation(value: Int) {
    本人(0), 配偶(1), 父亲(2), 母亲(3), 祖先(4);

    private var value = 0
    fun toInt(): Int {
        return value
    }

    companion object {
        @JvmStatic
        fun fromInt(value: Int): MDrelation {    //    手写的从int到enum的转换函数
            return when (value) {
                0 -> 本人
                1 -> 配偶
                2 -> 父亲
                3 -> 母亲
                else -> 祖先
            }
        }

        fun fromString(value: String): MDrelation {
            return when (value) {
                "本人" -> 本人
                "配偶" -> 配偶
                "父亲" -> 父亲
                "母亲" -> 母亲
                else -> 祖先
            }
        }

        fun count(): Int {
            return 5
        }
    }

    init {
        this.value = value
    }
}