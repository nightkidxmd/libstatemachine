package com.tuyou.tsd.statemachine.message

import java.util.concurrent.LinkedBlockingDeque

/**
 * Created by XMD on 2017/7/20.
 */
class LinkedBlockingDequeMessageQueue:IMessageQueue<LinkedBlockingDeque<Message>> {


    private val messages = LinkedBlockingDeque<Message>()

    override fun addTail(message: Message) = messages.addLast(message)

    override fun addHead(message: Message) = messages.addFirst(message)

    override fun removeLast() = messages.removeLast()!!

    override fun next(): Message? = messages.poll()

    override fun clear() = messages.clear()

    override fun getMessages(): LinkedBlockingDeque<Message>? = messages

    override fun addAll(messageQueue: IMessageQueue<LinkedBlockingDeque<Message>>) {
        messages.addAll(messageQueue.getMessages()!!)
    }
}