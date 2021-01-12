package com.wang17.religiouscalendar.model

import java.util.*

/**
 * Created by 阿弥陀佛 on 2016/10/28.
 */
class RunLog {
    var id: UUID
    var runTime: DateTime
    var tag: String
    var item: String
    var message: String

    constructor(tag:String,item: String, message: String) {
        id = UUID.randomUUID()
        runTime = DateTime()
        this.tag = tag
        this.item = item
        this.message = message
    }
    constructor(id:UUID,tag:String,item: String, message: String) {
        this.id = id
        runTime = DateTime()
        this.tag = tag
        this.item = item
        this.message = message
    }
    constructor(id:UUID,runTime:DateTime,tag:String,item: String, message: String) {
        this.id = id
        this.runTime = runTime
        this.tag = tag
        this.item = item
        this.message = message
    }
}