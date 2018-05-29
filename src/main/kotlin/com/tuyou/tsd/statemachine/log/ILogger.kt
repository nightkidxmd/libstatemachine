package com.tuyou.tsd.statemachine.log

/**
 * Created by XMD on 2017/7/20.
 */
interface ILogger {
    fun log(logMessage: LogMessage)
    fun loge(logMessage: LogMessage)
}