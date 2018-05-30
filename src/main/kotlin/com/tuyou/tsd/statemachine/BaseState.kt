package com.tuyou.tsd.statemachine

import com.tuyou.tsd.statemachine.message.Message

abstract class BaseState(private val stateMachine: BaseStateMachine):SState(){
    internal var message: Message? = null

    override fun enter() {
        super.enter()
        stateMachine.onStateEnter(this,message)
    }

    override fun exit() {
        super.exit()
        stateMachine.onStateExit(this,message)
    }

    override fun processMessage(msg: Message): Boolean {
        return stateMachine.onProcessMessage(msg) || super.processMessage(msg)
    }

    override fun toString(): String = name

}