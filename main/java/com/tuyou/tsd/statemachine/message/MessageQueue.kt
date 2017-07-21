package main.java.com.tuyou.tsd.statemachine.message

import java.util.concurrent.LinkedBlockingDeque

/**
 * Created by XMD on 2017/7/20.
 */
class MessageQueue {
    private val messages = LinkedBlockingDeque<Message>()

    fun addTail(message:Message) = messages.addLast(message)

    fun addHead(message: Message) = messages.addFirst(message)

    fun removeLast() = messages.removeLast()

    fun size() = messages.size

    fun next():Message? = messages.poll()

    fun addAll(messageQueue:MessageQueue){
        messages.addAll(messageQueue.messages)
    }
    fun clear() = messages.clear()
}