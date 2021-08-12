package main

import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

//source: https://stackoverflow.com/a/48445081/15974102
//motivation: lateinit var is a var after all, so the variable is mutable
//also, if you use lazy, you cannot define when the variable will be initialized
//it will always be the first time it is accessed
//so with this, you can have the initialization code somewhere else

class InitOnceProperty<T> : ReadWriteProperty<Any, T> {

    private object EMPTY

    private var value: Any? = EMPTY

    override fun getValue(thisRef: Any, property: KProperty<*>): T {
        if (value == EMPTY) {
            throw IllegalStateException("Value isn't initialized")
        } else {
            return value as T
        }
    }

    override fun setValue(thisRef: Any, property: KProperty<*>, value: T) {
        if (this.value != EMPTY) {
            throw IllegalStateException("Value is initialized")
        }
        this.value = value
    }
}

inline fun <reified T> lateVal(): ReadWriteProperty<Any, T> = InitOnceProperty()

//class Test {
//
//    var property: String by lateVal()
//
//    fun readValueFailure() {
//        val data = property //Value isn't initialized, exception is thrown
//    }
//
//    fun writeValueTwice() {
//        property = "Test1"
//        property = "Test2" //Exception is thrown, value already initalized
//    }
//
//    fun readWriteCorrect() {
//        property = "Test"
//        val data1 = property
//        val data2 = property //Exception isn't thrown, everything is correct
//    }
//
//}
//
//fun main() {
//    val test = Test()
////    test.readValueFailure()
////    test.property = "test"
////    test.writeValueTwice()
//
//    test.readWriteCorrect()
//    println(test.property)
//}

