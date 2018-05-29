package com.tuyou.tsd.statemachine.log


/**
 * Created by XMD on 2017/7/20.
 */
class SystemOutLogger: ILogger {
    override fun log(logMessage: LogMessage) {
        System.out.println("$logMessage")
    }

    override fun loge(logMessage: LogMessage) {
        System.err.println("$logMessage")
    }
}