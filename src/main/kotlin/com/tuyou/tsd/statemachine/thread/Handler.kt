package com.tuyou.tsd.statemachine.thread

import com.tuyou.tsd.statemachine.message.Message


/**
 * Created by XMD on 2017/7/21.
 */
abstract class Handler {
    abstract fun handleMessage(msg: Message)
}