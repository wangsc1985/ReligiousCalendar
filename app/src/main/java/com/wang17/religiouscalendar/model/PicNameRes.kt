package com.wang17.religiouscalendar.model

/**
 * Created by 阿弥陀佛 on 2016/9/28.
 */
class PicNameRes(private val resId: Int, private val listItemString: String) {
    fun getResId(): Int {
        return resId
    }

    fun getListItemString(): String {
        return listItemString
    }
}