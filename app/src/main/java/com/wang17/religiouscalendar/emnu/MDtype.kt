package com.wang17.religiouscalendar.emnu

/**
 * Created by 阿弥陀佛 on 2015/6/30.
 */
enum class MDtype(value: Int) {
    诞日(0), 忌日(1);

    var value = 0
    fun toInt(): Int {
        return value
    }

    companion object {
        @JvmStatic
        fun fromInt(value: Int): MDtype {    //    手写的从int到enum的转换函数
            return when (value) {
                0 -> 诞日
                else -> 忌日
            }
        }

        fun fromString(value: String?): MDtype {
            return when (value) {
                "诞日" -> 诞日
                else -> 忌日
            }
        }

        fun count(): Int {
            return 2
        }
    }

    init {    //    必须是private的，否则编译错误
        this.value = value
    }
}