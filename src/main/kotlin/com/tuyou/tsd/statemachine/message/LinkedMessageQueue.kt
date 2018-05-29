package com.tuyou.tsd.statemachine.message

/**
 * Created by XMD on 2017/7/20.
 */
class LinkedMessageQueue :IMessageQueue<Message> {


    private var messages:Message? = null

    override fun addTail(message: Message) {
        synchronized(this,{
            var p = messages
            var n = messages?.next
            while (true){
                if(p == null){
                    message.next = p
                    messages = message
                    break
                }else if(n == null){
                    message.next = n
                    p.next = message
                    break
                }else{
                    p = n
                    n = p.next
                }
            }
        })
    }

    override fun addHead(message: Message) = synchronized(this,{
        message.next = messages
        messages = message
    })

    override fun removeLast():Message =
        synchronized(this,{
            var p: Message? = messages ?: throw IllegalStateException("empty")
            var pre = p
            var n = p?.next
            while(true){
                if(n == null){
                    if(pre == p){
                        messages = null
                    }else{
                        pre?.next = null
                    }
                    break
                }else{
                    pre = p
                    p = n
                    n = p.next
                }
            }
            p!!
        })


    override fun next(): Message? = synchronized(this,{
        val msg = messages
        messages = msg?.next
        msg?.next = null
        msg
    })

    override fun addAll(messageQueue: IMessageQueue<Message>) = synchronized(this,{
        var p = messages
        var n = p?.next
        while(true){
            if (p == null){
                messages = messageQueue.getMessages()
                break
            }else if(n == null){
                p.next = messageQueue.getMessages()
                break
            }else{
                p = n
                n = p.next
            }
        }

    })

    override fun clear() = synchronized(this,{
        messages = null
    })

    override fun getMessages(): Message? = messages

}