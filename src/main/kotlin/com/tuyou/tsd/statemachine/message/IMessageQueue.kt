package com.tuyou.tsd.statemachine.message

interface IMessageQueue<T>{

    fun getMessages():T?

    fun addTail(message: Message)

    fun addHead(message: Message)

    fun removeLast():Message

    fun next(): Message?

    fun addAll(messageQueue: IMessageQueue<T>)

    fun clear()

    fun isEmpty():Boolean
}