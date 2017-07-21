package main.java.com.tuyou.tsd.statemachine.log

/**
 * Created by XMD on 2017/7/20.
 */
class SystemOutLogger: ILogger {
    override fun loge(time: String, tid: String, tag: String, message: String, fileName: String, lineNumber: Int) {
        System.err.println("$time $tid $tag $message($fileName:$lineNumber)")
    }

    override fun log(time:String,tid: String, tag: String, message: String, fileName: String, lineNumber: Int) {
        System.out.println("$time $tid $tag $message($fileName:$lineNumber)")
    }
}