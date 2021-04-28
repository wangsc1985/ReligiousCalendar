package com.wang17.lib

class MyClass {
    companion object{
        @JvmStatic
        fun main(args: Array<String>) {
            var person = Person("wang",19)
            person.let {
                it.age=18
            }
            println("${person.name}  ${person.age}")


            person.age=19
            with(person) {
                age=18
            }
            println("${person.name}  ${person.age}")


            person.age=19
            person.run {
                age=18
            }
            println("${person.name}  ${person.age}")


            person.age=19
            person.apply {
                age=18
            }
            println("${person.name}  ${person.age}")
        }
    }
    class Person(var name:String,var age:Int)
}