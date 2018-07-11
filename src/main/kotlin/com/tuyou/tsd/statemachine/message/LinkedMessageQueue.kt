package com.tuyou.tsd.statemachine.message

import java.util.concurrent.locks.ReentrantLock

/**
 * Created by XMD on 2017/7/20.
 */
class LinkedMessageQueue : IMessageQueue<Message> {


    private var messages: Message? = null

    private val fairLock = ReentrantLock(true)

    override fun addTail(message: Message) =
            try {
                fairLock.lock()
                var p = messages
                var n = messages?.next
                while (true) {
                    if (p == null) {
                        message.next = null
                        messages = message
                        break
                    } else if (n == null) {
                        message.next = n
                        p.next = message
                        break
                    } else {
                        p = n
                        n = p.next
                    }
                }
            } finally {
                fairLock.unlock()
            }

    override fun addHead(message: Message) = try{
        fairLock.lock()
        message.next = messages
        messages = message
    }finally {
        fairLock.unlock()
    }

    override fun removeLast(): Message =
            try {
                fairLock.lock()
                var p: Message? = messages ?: throw IllegalStateException("empty")
                var pre = p
                var n = p?.next
                while (true) {
                    if (n == null) {
                        if (pre == p) {
                            messages = null
                        } else {
                            pre?.next = null
                        }
                        break
                    } else {
                        pre = p
                        p = n
                        n = p.next
                    }
                }
                p!!
            } finally {
                fairLock.unlock()
            }


    override fun next(): Message? = try{
        fairLock.lock()
        val msg = messages
        messages = msg?.next
        msg?.next = null
        msg
    }finally {
        fairLock.unlock()
    }

    override fun addAll(messageQueue: IMessageQueue<Message>)= try{
        fairLock.lock()
        var p = messages
        var n = p?.next
        while (true) {
            if (p == null) {
                messages = messageQueue.getMessages()
                break
            } else if (n == null) {
                p.next = messageQueue.getMessages()
                break
            } else {
                p = n
                n = p.next
            }
        }

    }finally {
        fairLock.unlock()
    }

    override fun clear() = try{
        fairLock.lock()
        messages = null
    }finally {
        fairLock.unlock()
    }

    override fun isEmpty(): Boolean  = try {
        fairLock.lock()
        messages == null
    }finally {
        fairLock.unlock()
    }


    @Deprecated("NOT SAFE")
    override fun getMessages(): Message? = messages

}