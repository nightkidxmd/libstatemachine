package com.tuyou.tsd.statemachine.log

class LogMessage(private val time: String,
                 private val tid: String,
                 private val tag: String,
                 private val message: String,
                 private val fileName: String,
                 private val lineNumber: Int) {
    override fun toString(): String = "$time $tid $tag $message($fileName:$lineNumber)"
}