package com.tuyou.tsd.statemachine.log

class LogMessage(private val time: String,
                 private val tid: String,
                 private val tag: String,
                 private val message: String,
                 private val fileName: String,
                 private val lineNumber: Int,
                 private val noStack:Boolean = false) {
    override fun toString(): String = if(noStack) "$time $tid $tag $message" else "$time $tid $tag $message($fileName:$lineNumber)"
}