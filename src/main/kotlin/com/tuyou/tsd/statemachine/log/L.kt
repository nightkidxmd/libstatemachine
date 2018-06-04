package com.tuyou.tsd.statemachine.log

import java.text.SimpleDateFormat
import java.util.*


/**
 * Created by XMD on 2017/7/20.
 */

object L {
    private val logger: SystemOutLogger by lazy { SystemOutLogger() }
    private val dateFormat: SimpleDateFormat by lazy { SimpleDateFormat("yyyy-MM-dd HH:mm:ss") }

    private enum class LogType {
        OUT,
        ERR
    }

    fun log(tag: String? = null, message: String, noStack:Boolean = false) {
        _log(LogType.OUT, tag, message,noStack)
    }

    fun loge(tag: String? = null, message: String,noStack:Boolean = false) {
        _log(LogType.ERR, tag, message,noStack)
    }

    private fun _log(type: LogType, tag: String?, message: String,noStack:Boolean = false) {
        val stackTrace = if (tag == null) getStackTrackElement(5) else getStackTrackElement(4)
        val currentTime = System.currentTimeMillis()
        val time = String.format("%s.%03d", dateFormat.format(Date(currentTime)), currentTime % 1000)
        val tid = String.format("%-3d", getThreadId())
        val _tag = "${tag ?: getTag(stackTrace)}:"
        val fileName = stackTrace.fileName
        val lineNumber = stackTrace.lineNumber
        with(LogMessage(time, tid, _tag, message, fileName, lineNumber,noStack)){
            when (type) {
                LogType.OUT -> {
                    logger.log(this)
                }
                LogType.ERR -> {
                    logger.loge(this)
                }
            }
        }

    }

    private fun getStackTrackElement(index: Int = 3) = Thread.currentThread().stackTrace[index]
    private fun getThreadId() = Thread.currentThread().id

    private fun getTag(stackTraceElement: StackTraceElement): String {
        var tag = stackTraceElement.className.substring(stackTraceElement.className.lastIndexOf(".") + 1)
        val index = tag.lastIndexOf("$")
        if (index > 0) {
            tag = tag.substring(index + 1)
        }
        return tag
    }
}