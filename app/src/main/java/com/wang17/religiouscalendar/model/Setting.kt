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
        return value.toBoolean()
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

    override fun toString(): String {
        return value.toString()
    }

    enum class KEYS {
        banner, welcome, welcome_duration, zodiac1, zodiac2, szr, lzr, gyz,fj, latestVersionCode, recordIsOpened, targetAuto, birthday, is_weekend_first, targetInHour,privacy_version,checked_version_code
    }
}