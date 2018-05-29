package com.tuyou.tsd.statemachine

import com.tuyou.tsd.statemachine.log.L
import com.tuyou.tsd.statemachine.message.Message
import com.tuyou.tsd.statemachine.message.LinkedMessageQueue
import com.tuyou.tsd.statemachine.thread.Handler
import com.tuyou.tsd.statemachine.thread.Looper
import java.util.*


/**
 * Created by XMD on 2017/7/20.
 */
abstract class StateMachine : Looper {

    constructor(name: String) : this(name, SmHandler())
    private constructor(name: String, smHandler: SmHandler) : super(name, smHandler) {
        this.smHandler = smHandler
        this.smHandler.stateMachine = this
    }

    private val smHandler: SmHandler


    override fun onStart() {
        smHandler.completeConstruction()
    }

    companion object {
        const val HANDLED = true
        const val NOT_HANDLED = false


        private const val SM_QUIT_CMD = -1
        private const val SM_INIT_CMD = -2
    }

    private class SmHandler : Handler() {
        private val smHandlerObj = Any()
        private val defferMessageQueue: LinkedMessageQueue = LinkedMessageQueue()
        val haltingState = HaltingState()
        private val quittingState = QuittingState()
        private val stateInfoMap = HashMap<IState, StateInfo>()
        init {
            addState(haltingState, null)
            addState(quittingState, null)
        }
        class StateInfo {
            var state: IState? = null
            var parent: StateInfo? = null
            var active = false
            var depth = 1
            override fun toString(): String {
                return "StateInfo(state=$state, parent=$parent, active=$active, depth=$depth)"
            }
        }

        var stateMachine: StateMachine? = null

        var initialState: IState? = null
        var destState: IState? = null

        private var isConstructionCompleted = false
        private var stateStack = emptyArray<StateInfo?>()
        private var stateStackTopIndex = -1
        private var tempStateStack = emptyArray<StateInfo?>()
        private var tempStateStackCount = 0

        inner class HaltingState: SState(){
            override fun processMessage(msg: Message): Boolean = with(stateMachine!!){
                haledProcessMessage(msg)
                return HANDLED
            }
        }

        private class QuittingState: SState(){
            override fun processMessage(msg: Message): Boolean {
                return NOT_HANDLED
            }
        }


        fun addState(state: IState, parent: IState?): StateInfo? {
            L.log(message = "add $state $parent")
            var parentStateInfo: StateInfo? = null
            if(parent != null) {
                parentStateInfo = stateInfoMap[parent]
                if(parentStateInfo == null) {
                    parentStateInfo = addState(parent, null)
                }
            }
            var stateInfo = stateInfoMap[state]
            if(stateInfo == null) {
                stateInfo = StateInfo()
                stateInfoMap.put(state, stateInfo)
            }

            if ((stateInfo.parent != null) && (stateInfo.parent != parentStateInfo)) {
                throw RuntimeException("state already added:" + state)
            }
            stateInfo.state = state
            stateInfo.parent = parentStateInfo
            stateInfo.active = false
            if (parentStateInfo != null) {
                stateInfo.depth = parentStateInfo.depth.plus(1)
            }
            return stateInfo
        }

        fun transitionTo(destState: IState) {
            this.destState = destState
        }

        fun deferMessage(message: Message) {
            defferMessageQueue.addTail(message.copy())
        }

        fun completeConstruction() = with(stateMachine!!) {
            var maxDepth = 0
            stateInfoMap.values
                    .asSequence()
                    .filter { maxDepth < it.depth }
                    .forEach { maxDepth = it.depth }
            stateStack = arrayOfNulls(maxDepth)
            tempStateStack = arrayOfNulls(maxDepth)
            setupInitialStateStack()
            sendMessageAtFrontOfQueue(what = SM_INIT_CMD, obj = smHandlerObj)
        }

        fun quit() = with(stateMachine!!){
            sendMessage(what = SM_QUIT_CMD,obj = smHandlerObj)
        }

        fun quitNow() = with(stateMachine!!){
            sendMessageAtFrontOfQueue(what = SM_QUIT_CMD,obj = smHandlerObj)
        }

        private fun setupInitialStateStack() {
            var curStateInfo = stateInfoMap[initialState]
            tempStateStackCount = 0
            while (curStateInfo != null) {
                tempStateStack[tempStateStackCount] = curStateInfo
                curStateInfo = curStateInfo.parent
                tempStateStackCount++
            }
            stateStackTopIndex = -1
            moveTempStateStackToStateStack()
        }

        private fun moveTempStateStackToStateStack(): Int {
            val startingIndex = stateStackTopIndex + 1
            var i = tempStateStackCount - 1
            var j = startingIndex
            while (i >= 0) {
                stateStack[j++] = tempStateStack[i--]
            }
            stateStackTopIndex = j - 1
            return startingIndex
        }

        override fun handleMessage(msg: Message) {
            if (isConstructionCompleted) {
                processMsg(msg)
            } else if (msg.what == SM_INIT_CMD && msg.obj == smHandlerObj) {
                isConstructionCompleted = true
                invokeEnterMethods(0)
            } else {
                throw  RuntimeException("StateMachine.handleMessage: "
                        + "The start method not called, received msg: " + msg)
            }
            performTransitions()
        }

        private fun processMsg(message: Message): IState? {
            var curStateInfo = stateStack[stateStackTopIndex]
            if (isQuit(message)) {
                transitionTo(quittingState)
            } else {
                while (!curStateInfo?.state?.processMessage(message)!!) {
                    curStateInfo = curStateInfo.parent
                    if (curStateInfo == null) {
                        stateMachine?.unhandledMessage(message)
                        break
                    }
                }
            }
            return if (curStateInfo == null) null else curStateInfo.state
        }

        private fun invokeEnterMethods(stateStackEnteringIndex: Int) {
            for (i in stateStackEnteringIndex..stateStackTopIndex) {
                stateStack[i]?.state?.enter()
                stateStack[i]?.active = true
            }
        }

        private fun invokeExitMethods(commonStateInfo: StateInfo?) {
            while (stateStackTopIndex >= 0
                    && stateStack[stateStackTopIndex] != commonStateInfo) {
                stateStack[stateStackTopIndex]?.state?.exit()
                stateStack[stateStackTopIndex]?.active = false
                stateStackTopIndex--
            }
        }

        private fun isQuit(msg: Message?) = (msg?.what == SM_QUIT_CMD) && (msg.obj == smHandlerObj)

        private fun performTransitions() {
            var destState = this.destState
            if(destState != null){
                while (true) {
                    val commonStateInfo = setupTempStateStackWithStatesToEnter(destState!!)
                    invokeExitMethods(commonStateInfo)
                    val stateStackEnteringIndex = moveTempStateStackToStateStack()
                    invokeEnterMethods(stateStackEnteringIndex)
                    moveDeferredMessageAtFrontOfQueue()
                    if (destState != this.destState) {
                        destState = this.destState
                    } else {
                        break
                    }
                }
                this.destState = null
            }

            if(destState != null) {
                if(destState == quittingState){
                    stateMachine?.onQuitting()
                    cleanupAfterQuitting()
                }else if(destState == haltingState){
                    stateMachine?.onHalting()
                }
            }
        }

        private fun setupTempStateStackWithStatesToEnter(destState: IState): StateInfo? {
            tempStateStackCount = 0
            var curStateInfo = stateInfoMap[destState]
            do {
                tempStateStack[tempStateStackCount++] = curStateInfo
                curStateInfo = curStateInfo?.parent
            } while (curStateInfo != null && !curStateInfo.active)

            return curStateInfo
        }

        private fun moveDeferredMessageAtFrontOfQueue() = with(stateMachine!!) {
            while (true) {
                try {
                    sendMessageAtFrontOfQueue(defferMessageQueue.removeLast())
                } catch (e: Exception) {
                    break
                }
            }
        }

        private fun cleanupAfterQuitting(){
            stateMachine?.exit()
            stateInfoMap.clear()
            defferMessageQueue.clear()
        }
    }

    protected fun transitionTo(state:IState){
        smHandler.transitionTo(state)
    }

    protected fun transitionToHaltingState(){
        smHandler.transitionTo(smHandler.haltingState)
    }

    protected fun setInitialState(state: IState) {
        smHandler.initialState = state
    }

    protected fun addState(state: IState, parent: IState? = null) {
        smHandler.addState(state, parent)
    }

    protected fun unhandledMessage(msg: Message){
        L.loge(message = " - unhandledMessage: msg.what=${msg.what}")
    }

    protected open fun haledProcessMessage(msg: Message){

    }

    protected open fun onHalting(){

    }

    protected open fun onQuitting(){
        L.log(message = " - onQuitting")
    }

    fun sendMessageAtFrontOfQueue(message: Message) =
            dispatchMessageAtFrontOfQueue(message)

    fun sendMessageAtFrontOfQueue(what: Int, obj: Any? = null, arg1: Int = 0, arg2: Int = 0) =
            sendMessageAtFrontOfQueue(Message.obtain(what = what, obj = obj, arg1 = arg1, arg2 = arg2))

    fun sendMessage(message: Message) = dispatchMessage(message)

    fun sendMessage(what: Int, obj: Any? = null, arg1: Int = 0, arg2: Int = 0) = sendMessage(Message.obtain(what = what, obj = obj, arg1 = arg1, arg2 = arg2))

    fun deferMessage(message: Message) = smHandler.deferMessage(message)

    fun deferMessage(what: Int, obj: Any? = null, arg1: Int = 0, arg2: Int = 0) = deferMessage(Message.obtain(what = what, obj = obj, arg1 = arg1, arg2 = arg2))

    fun quit(){
        smHandler.quit()
    }

    fun quitNow(){
        smHandler.quitNow()
    }
}