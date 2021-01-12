package com.wang17.religiouscalendar.model

import com.wang17.religiouscalendar.emnu.MDrelation
import com.wang17.religiouscalendar.emnu.MDtype
import java.util.*

/**
 * Created by 阿弥陀佛 on 2015/6/30.
 */
class MemorialDay {
    var id: UUID
    var type: MDtype
    var relation: MDrelation
    var lunarDate: LunarDate

    constructor(type: MDtype, relation: MDrelation, lunarDate: LunarDate) {
        this.type = type
        this.relation = relation
        this.lunarDate = lunarDate
    }

    constructor(id: UUID, type: MDtype, relation: MDrelation, lunarDate: LunarDate) {
        this.id = id
        this.type = type
        this.relation = relation
        this.lunarDate = lunarDate
    }


    init {
        id = UUID.randomUUID()
    }
}