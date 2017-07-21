package main.java.com.tuyou.tsd.statemachine

import main.java.com.tuyou.tsd.statemachine.log.L
import main.java.com.tuyou.tsd.statemachine.message.Message

/**
 * Created by XMD on 2017/7/20.
 */
abstract class SState :IState{
    override fun enter() {
        if(DEBUG){
            L.log("SState","enter-->$name{${toString()}}")
        }
    }

    override fun exit() {
        if(DEBUG){
            L.log("SState","exit<--${toString()}")
        }
    }

    override fun processMessage(msg: Message) = false

    override fun getName(): String {
        val name = javaClass.name
        val lastDollar = name.lastIndexOf('$')
        return if(lastDollar >0) name.substring(lastDollar + 1) else name
    }

    companion object{
        private val DEBUG = true
    }
}