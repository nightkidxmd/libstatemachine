package com.tuyou.tsd.statemachine.thread


import com.tuyou.tsd.statemachine.message.IMessageQueue
import com.tuyou.tsd.statemachine.message.Message
import com.tuyou.tsd.statemachine.message.LinkedMessageQueue

/**
 * Created by XMD on 2017/7/21.
 */
open class Looper(name:String, private val handler: Handler): AbsPollOnceThread(name){

    companion object {
        private const val MESSAGE_EXIT = Int.MIN_VALUE
    }

    override fun onStart() = Unit

    override fun onExit() {
        messageQueue.clear()
    }

    private val messageQueue:IMessageQueue<*> = LinkedMessageQueue()
    override fun onPollOnce() {
        while (isRunning){
            val message =  messageQueue.next()
            if(message != null){
                when(message.what){
                    MESSAGE_EXIT->{
                        if(message.obj == exitObj){
                            exit()
                        }else{
                            handler.handleMessage(message)
                        }
                    }
                    else->{
                        handler.handleMessage(message)
                    }
                }
            }else{
                break
            }
            message.recycle()
        }
    }

    fun dispatchMessage(msg: Message){
        messageQueue.addTail(msg)
        pollOnce()
    }

    fun dispatchMessageAtFrontOfQueue(msg: Message){
        messageQueue.addHead(msg)
        pollOnce()
    }

    fun exitSafely(){
        dispatchMessage(Message(what = MESSAGE_EXIT,obj = exitObj))
    }

    private val exitObj = ExitObj()

    private inner class ExitObj
}