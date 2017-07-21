package main.java.com.tuyou.tsd.statemachine.log

/**
 * Created by XMD on 2017/7/20.
 */
interface ILogger {
    fun log(time: String, tid: String, tag: String, message: String, fileName: String, lineNumber: Int)
    fun loge(time: String, tid: String, tag: String, message: String, fileName: String, lineNumber: Int)
}