package main.java.com.tuyou.tsd.statemachine.thread


import main.java.com.tuyou.tsd.statemachine.message.Message
import main.java.com.tuyou.tsd.statemachine.message.MessageQueue

/**
 * Created by XMD on 2017/7/21.
 */
open class Looper(name:String,val handler: Handler):AbsPollOnceThread(name){
    override fun onStart() {
//        throw UnsupportedOperationException("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onExit() {
//        throw UnsupportedOperationException("not implemented") //To change body of created functions use File | Settings | File Templates.
        messageQueue.clear()
    }

    private val messageQueue = MessageQueue()
    override fun onPollOnce() {
        while (isRunning){
            val message =  messageQueue.next()
            if(message != null){
                handler.handleMessage(message)
            }else{
                break
            }
        }
    }

    fun dispatchMessage(msg: Message){
        messageQueue.addTail(msg)
        pollOnce()
    }

    fun dispatchMessageAtFrontOfQueue(msg:Message){
        messageQueue.addHead(msg)
        pollOnce()
    }

    fun dispatchMessageQueue(msgqueue:MessageQueue){
        messageQueue.addAll(msgqueue)
        pollOnce()
    }
}