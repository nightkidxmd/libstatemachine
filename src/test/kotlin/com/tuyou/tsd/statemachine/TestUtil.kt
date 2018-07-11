package com.tuyou.tsd.statemachine

import org.junit.Assert
import java.nio.ByteBuffer
import java.util.*

object TestUtil{
    fun <T> assertTrue(expect: T, got: T) =
    Assert.assertTrue("\nexpect:\n$expect,\nbut got:\n$got", expect == got)

    fun assertTrue(condition:Boolean, message:String) =
            Assert.assertTrue(message,condition)

    fun printBuffer(buffer: ByteBuffer) {
        print("[ ")
        buffer.array().forEach {
            print(String.format(Locale.getDefault(), "%02x ", it.toInt().and(0x0ff)))
        }
        println("]")
    }
}