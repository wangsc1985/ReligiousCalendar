package com.wang17.religiouscalendar.model

import com.wang17.religiouscalendar.util.Lunar

/**
 * Created by 阿弥陀佛 on 2015/6/19.
 */
class CalendarItem{
    var yangLi:DateTime
    var nongLi: LunarDateTime
    var religious: MutableList<ReligiousInfo> = ArrayList()
    var remarks: String = ""

    constructor(yangLi: DateTime) {
        this.yangLi = yangLi
        val lunar = Lunar(yangLi)
        nongLi = LunarDateTime(lunar.year, lunar.month, lunar.day, lunar.leap)
    }
}