package com.tuyou.tsd.statemachine


import com.tuyou.tsd.statemachine.message.Message
import kotlin.reflect.KClass

abstract class BaseStateMachine(name: String,private val stateHandlersMap:Map<KClass<*>, StateHandler>):StateMachine(name){

    private val transitionMap = HashMap<Int,BaseState>()

    /**
     * 初始化状态转移Map,使用方法{@link addTransition}
     */
    abstract fun onInitTransitionMap()


    /**
     * 初始化状态树
     */
    abstract fun onInitStateTree()

    /**
     * 指定初始化状态
     */
    abstract fun getInitialState():BaseState


    fun initAndStart():BaseStateMachine{
        onInitStateTree()
        onInitTransitionMap()
        setInitialState(getInitialState())
        start()
        return this
    }

    internal fun addTransition(what:Int,state: BaseState){
        if(transitionMap[what] != null){
            throw  IllegalArgumentException("<$what,$state> already added")
        }
        transitionMap[what] = state
    }

    fun transitionTo(msg: Message){
        val destState = transitionMap[msg.what] ?: throw IllegalStateException("message:${msg.what} wasn't mapped into transition map!!")
        destState.message = msg
        transitionTo(destState)
    }

    internal fun onStateEnter(state: BaseState, msg: Message?) {
        stateHandlersMap[state::class]?.onStateEnter(state,msg)
    }

    internal fun onStateExit(state: BaseState, msg: Message?) {
        stateHandlersMap[state::class]?.onStateExit(state,msg)
    }

    internal fun onProcessMessage(msg: Message): Boolean {
        if(transitionMap[msg.what] != null){
            transitionTo(msg)
            return HANDLED
        }
        return stateHandlersMap[state::class]?.onProcessMessage(msg)?: NOT_HANDLED
    }

    open inner class StateHandler {
        open fun onStateEnter(state:BaseState,msg: Message?) = Unit
        open fun onStateExit(state:BaseState,msg: Message?) = Unit
        open fun onProcessMessage(msg:Message?):Boolean = StateMachine.NOT_HANDLED
    }
}

