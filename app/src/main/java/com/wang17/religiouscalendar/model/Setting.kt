package com.wang17.religiouscalendar.model

/**
 * Created by 阿弥陀佛 on 2015/6/30.
 */
class Setting {
    var key:String
    var value:String

    constructor(key: String, value: String) {
        this.key = key
        this.value = value
    }

    fun getBoolean(): Boolean {
        return java.lang.Boolean.parseBoolean(value)
    }

    fun getInt(): Int {
        return value.toInt()
    }

    fun getLong(): Long {
        return value.toLong()
    }

    fun getDateTime(): DateTime {
        return DateTime(getLong())
    }

    enum class KEYS {
        banner, welcome, welcome_duration, zodiac1, zodiac2, szr, lzr, gyz, latestVersionCode, recordIsOpened, targetAuto, birthday, is_weekend_first, targetInHour
    }
}