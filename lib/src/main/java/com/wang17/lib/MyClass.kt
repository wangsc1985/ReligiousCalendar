package com.wang17.lib

class MyClass {
    companion object{
        @JvmStatic
        fun main(args: Array<String>) {
            val nameList:MutableList<String>? = null
            val size = nameList?.size ?:0
            println("size: $size")
        }
    }
}