package com.wang17.religiouscalendar.model

import java.util.*

/**
 * Created by 阿弥陀佛 on 2016/10/17.
 */
class SexualDay {
    var id: UUID
    var dateTime: DateTime
    var item: String
    var summary: String


    constructor(dateTime: DateTime, item: String, summary: String) {
        id = UUID.randomUUID()
        this.dateTime = dateTime
        this.item = item
        this.summary = summary
    }
    constructor(id:UUID,dateTime: DateTime, item: String, summary: String) {
        this.id = id
        this.dateTime = dateTime
        this.item = item
        this.summary = summary
    }
}