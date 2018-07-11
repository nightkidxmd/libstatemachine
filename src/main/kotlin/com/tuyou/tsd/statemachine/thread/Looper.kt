package com.tuyou.tsd.statemachine.thread


import com.tuyou.tsd.statemachine.message.IMessageQueue
import com.tuyou.tsd.statemachine.message.LinkedMessageQueue
import com.tuyou.tsd.statemachine.message.Message

/**
 * Created by XMD on 2017/7/21.
 */
open class Looper(name:String, var handler: Handler? = null): AbsPollOnceThread(name){

    companion object {
        private const val MESSAGE_EXIT = Int.MIN_VALUE
    }

    override fun onStart() = Unit

    private val dispatchWork = object :Work<Message>{
        override fun pollOnceWork(data: Message?) {
            messageQueue.addTail(data!!)
        }
    }

    private val dispatchFrontWork = object :Work<Message>{
        override fun pollOnceWork(data: Message?) {
            messageQueue.addHead(data!!)
        }
    }

    override fun onExit() {
        lock()
        messageQueue.clear()
        unlock()
    }

    private val messageQueue:IMessageQueue<*> = LinkedMessageQueue()
    override fun onPollOnce() {
        while (isRunning){
            with(messageQueue.next()){
                if(this != null){
                    when(this.what){
                        MESSAGE_EXIT->{
                            if(this.obj == exitObj){
                                exit()
                            }else{
                                handler?.handleMessage(this)
                            }
                        }
                        else->{
                            handler?.handleMessage(this)
                        }
                    }
                    this.recycle()
                }else{
                    lock()
                    if(messageQueue.isEmpty()){
                        return
                    } else {
                        unlock()
                    }
                }
            }
        }
    }

    fun dispatchMessage(msg: Message){
        pollOnce(dispatchWork,msg)
    }

    fun dispatchMessageAtFrontOfQueue(msg: Message){
        pollOnce(dispatchFrontWork,msg)
    }

    fun exitSafely(){
        dispatchMessage(Message(what = MESSAGE_EXIT,obj = exitObj))
    }

    fun exitSafelySyc(millis:Long = 0){
        dispatchMessage(Message(what = MESSAGE_EXIT,obj = exitObj))
        join(millis)
    }

    private val exitObj = ExitObj()

    private inner class ExitObj
}