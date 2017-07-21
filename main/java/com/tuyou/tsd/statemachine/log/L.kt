package main.java.com.tuyou.tsd.statemachine.log

import java.text.SimpleDateFormat
import java.util.*


/**
 * Created by XMD on 2017/7/20.
 */

object L {
   private val logger: SystemOutLogger by lazy { SystemOutLogger() }
   private val dateFormat: SimpleDateFormat by lazy { SimpleDateFormat("HH:mm:ss") }

   private enum class LogType{
       OUT,
       ERR
   }


   fun log(tag:String? = null,message:String){
       _log(LogType.OUT,tag,message)
   }

    fun loge(tag:String? = null,message:String){
        _log(LogType.ERR,tag,message)
    }

    private fun _log(type:LogType,tag: String?,message: String){
        val stackTrace = if (tag == null) getStackTrackElement(5) else getStackTrackElement(4)
        val currentTime = System.currentTimeMillis()
        val time = String.format("%s.%03d","${dateFormat.format(Date(currentTime))}",currentTime % 1000)
        val tid = String.format("%-3d", getThreadId())
        val _tag = "${tag?:getTag(stackTrace)}:"
        val fileName = stackTrace.fileName
        val lineNumber = stackTrace.lineNumber
        when(type){
            LogType.OUT->{
                logger.log(time,tid,_tag,message,fileName,lineNumber)
            }
            LogType.ERR->{
                logger.loge(time,tid,_tag,message,fileName,lineNumber)
            }
        }
    }

    private fun getStackTrackElement(index:Int=3) = Thread.currentThread().stackTrace[index]
    private fun getThreadId() = Thread.currentThread().id

    private fun getTag(stackTraceElement:StackTraceElement):String{
        var tag = stackTraceElement.className.substring(stackTraceElement.className.lastIndexOf(".")+1)
        val index = tag.lastIndexOf("$")
        if(index > 0){
            tag = tag.substring(index+1)
        }
        return tag
    }
}