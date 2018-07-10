package com.tuyou.tsd.statemachine

import com.tuyou.tsd.statemachine.log.L
import com.tuyou.tsd.statemachine.message.Message

abstract class BaseState(private val stateMachine: BaseStateMachine):SState(){
    internal var message: Message? = null
    override fun enter() {
        if(stateMachine.DEBUG){
            L.log(stateMachine.name,"enter-->$this")
        }
        stateMachine.onStateEnter(this,message)
    }

    override fun exit() {
        if(stateMachine.DEBUG){
            L.log(stateMachine.name,"exit<---$this")
        }
        stateMachine.onStateExit(this,message)
    }

    override fun processMessage(msg: Message): Boolean {
        return stateMachine.onProcessMessage(this,msg) || super.processMessage(msg)
    }

    override fun toString(): String = getName()

}