package com.wang17.religiouscalendar.util

import com.wang17.religiouscalendar.emnu.SolarTerm
import com.wang17.religiouscalendar.model.DateTime
import java.util.*

/**
 * Created by 阿弥陀佛 on 2015/6/21.
 */
object SolarTermReader {
    operator fun get(solarTermMap: Map<DateTime?, SolarTerm?>, start: DateTime?, end: DateTime?): Map<DateTime, SolarTerm> {
        val result: MutableMap<DateTime, SolarTerm> = HashMap()
        val set: Set<*> = solarTermMap.entries
        val i = set.iterator()
        while (i.hasNext()) {
            val solar = i.next() as Map.Entry<DateTime, SolarTerm>
            if (solar.key.compareTo(start) > 0 && solar.key.compareTo(end) < 0) {
                result[solar.key] = solar.value
            }
        }
        return result
    }

    operator fun get(solarTermMap: Map<Calendar?, SolarTerm?>?, start: Calendar?): Map<Calendar, SolarTerm>? {
        return null
    }

    fun load(): Map<DateTime, SolarTerm>? {
        return null
    }
}