package com.tuyou.tsd.statemachine


import com.tuyou.tsd.statemachine.log.L
import com.tuyou.tsd.statemachine.message.LinkedMessageQueue
import com.tuyou.tsd.statemachine.message.Message
import com.tuyou.tsd.statemachine.thread.AbsPollOnceThread
import com.tuyou.tsd.statemachine.thread.Handler
import com.tuyou.tsd.statemachine.thread.Looper

import org.junit.Test
import kotlin.reflect.KClass


/**
 * Created by XMD on 2017/7/20.
 */
class TestStateMachine {


    @Test
    fun testMessageQueue(){
        val amount = 1000
        with(Looper("test", object : Handler() {
            private var sum = 0
            override fun handleMessage(msg: Message) {
                sum += 1
                if(msg.what == amount){
                    L.log(message = "$sum  ${msg.what} $amount")
                    TestUtil.assertTrue(amount,sum)
                }

            }
        })) {
            start()
            for (i in 1..amount) {
                dispatchMessage(Message.obtain(i))
            }
            exitSafely()
            join()
        }
    }

    @Test fun testAddRemoveLastMessage(){
        val messageQueue = LinkedMessageQueue()

        (1..10).forEach {
            messageQueue.addHead(Message.obtain(it))
        }

        (10 downTo 1).forEach {
            TestUtil.assertTrue(it,messageQueue.next()?.what)
        }

        (1..10).forEach {
            messageQueue.addTail(Message.obtain(it))
        }

        (1..10).forEach {
            TestUtil.assertTrue(it,messageQueue.next()?.what)
        }
    }

    @Test
    fun testStateMachine() {
        val test = TestStateMachine()
        for (i in 1..5) {
            test.sendMessage(Message.obtain(i))
        }
        test.quit()
        test.join()
    }

    @Test
    fun testAbsPollOnceThread(){
        val test = TestThread()
        test.start()
        test.pollOnce()
        Thread.sleep(200)
        test.exit()
        test.join()
    }


    @Test
    fun testBaseStateMachine(){
        val test = TestBaseStateMachine(mapOf(
                TestBaseStateMachine.State1::class to object : BaseStateMachine.StateHandler() {
                    override fun onStateEnter(state: BaseState, msg: Message?) {
                        super.onStateEnter(state, msg)
                        L.log(message = "onStateEnter $state")
                    }

                    override fun onStateExit(state: BaseState, msg: Message?) {
                        super.onStateExit(state, msg)
                        L.log(message = "onStateExit $state")
                    }

                    override fun onProcessMessage(msg: Message?): Boolean {
                        L.log(message = "onProcessMessage $msg")
                        return super.onProcessMessage(msg)
                    }
                }
        )).initAndStart()
        arrayOf(1,6,2,3,4,5).forEach {
            test.sendMessage(Message.obtain(it))
        }
        test.quit()
        test.join()
    }

    private class TestThread : AbsPollOnceThread() {
        override fun onPollOnce() {
            L.log(message = "onPollOnce")
        }

        override fun onStart() {
            L.log(message = "onStart")
        }

        override fun onExit() {
            L.log(message = "onExit")
        }

    }



    private class TestBaseStateMachine(stateHandlersMap:Map<KClass<*>, StateHandler>):BaseStateMachine("TestBaseStateMachine",stateHandlersMap){

        /**
         * 初始化状态转移Map,使用方法{@link addTransition}
         */
        override fun onInitTransitionMap() {
            addTransition(MESSAGE_STATE1,state1)
            addTransition(MESSAGE_STATE2,state2)
            addTransition(MESSAGE_STATE3,state3)
            addTransition(MESSAGE_STATE4,state4)
            addTransition(MESSAGE_STATE5,state5)
        }

        /**
         * 初始化状态树
         */
        override fun onInitStateTree() {
            state1 = State1()
            state2 = State2()
            state3 = State3()
            state4 = State4()
            state5 = State5()
            addState(state1)
            addState(state2, state1)
            addState(state3, state1)
            addState(state4, state3)
            addState(state5, state3)
        }

        /**
         * 指定初始化状态
         */
        override fun getInitialState(): BaseState = state1

        companion object {
            const val MESSAGE_STATE1 = 1
            const val MESSAGE_STATE2 = 2
            const val MESSAGE_STATE3 = 3
            const val MESSAGE_STATE4 = 4
            const val MESSAGE_STATE5 = 5
        }


        private lateinit var state1:State1
        private lateinit var state2:State2
        private lateinit var state3:State3
        private lateinit var state4:State4
        private lateinit var state5:State5


        inner class State1 : BaseState(this@TestBaseStateMachine){
            override fun processMessage(msg: Message): Boolean = run{
                L.log(message = "processMessage $msg")
                when(msg.what){
                    MESSAGE_STATE3->{
                        sendMessageAtFrontOfQueue(msg.what)
                        super.processMessage(msg)
                    }
                    MESSAGE_STATE2->{
                        deferMessage(msg)
                        HANDLED
                    }
                    else->{
                        super.processMessage(msg)
                    }
                }

            }
        }
        inner class State2 : BaseState(this@TestBaseStateMachine){
            override fun processMessage(msg: Message): Boolean  = run {
                when (msg.what) {
                    MESSAGE_STATE2 -> {HANDLED}
                    MESSAGE_STATE3->{
                        sendMessageAtFrontOfQueue(msg.what)
                        super.processMessage(msg)
                    }
                    else -> { super.processMessage(msg) }
                }
            }
        }
        inner class State3 : BaseState(this@TestBaseStateMachine){
            override fun processMessage(msg: Message): Boolean  = when(msg.what){
                MESSAGE_STATE3->{
                    HANDLED
                }
                MESSAGE_STATE2->{
                    deferMessage(msg)
                    HANDLED
                }
                else->{
                    super.processMessage(msg)
                }
            }
        }
        inner class State4 : BaseState(this@TestBaseStateMachine){
            override fun processMessage(msg: Message): Boolean = run{
                when(msg.what){
                    MESSAGE_STATE3->{
                        sendMessageAtFrontOfQueue(msg.what)
                        super.processMessage(msg)

                    }
                    MESSAGE_STATE2->{
                        deferMessage(msg)
                        HANDLED
                    }
                    else->{
                        super.processMessage(msg)
                    }
                }
            }
        }
        inner class State5 : BaseState(this@TestBaseStateMachine){
            override fun processMessage(msg: Message): Boolean = run{
                when(msg.what){
                    MESSAGE_STATE3->{
                        sendMessageAtFrontOfQueue(msg.what)
                        super.processMessage(msg)
                    }
                    else->{
                        super.processMessage(msg)
                    }
                }
            }
        }
    }


    private class TestStateMachine : StateMachine("test") {
        companion object {
            const val MESSAGE_STATE1 = 1
            const val MESSAGE_STATE2 = 2
            const val MESSAGE_STATE3 = 3
            const val MESSAGE_STATE4 = 4
            const val MESSAGE_STATE5 = 5
        }

        private val state1 = MyState(1)
        private val state2 = MyState(2)
        private val state3 = MyState(3)
        private val state4 = MyState(4)
        private val state5 = MyState(5)

        private val transitionMap = mapOf(
                MESSAGE_STATE1 to state1,
                MESSAGE_STATE2 to state2,
                MESSAGE_STATE3 to state3,
                MESSAGE_STATE4 to state4,
                MESSAGE_STATE5 to state5
        )

        init {
            addState(state1)
            addState(state2, state1)
            addState(state3, state1)
            addState(state4, state3)
            addState(state5, state3)
            setInitialState(state1)
            start()
        }

        fun transitionTo(msg: Message) {
            transitionTo(transitionMap[msg.what]!!)
        }

        inner class MyState(val data: Int) : SState() {
            override fun toString(): String {
                return "MyState(data=$data)"
            }

            override fun processMessage(msg: Message): Boolean {
                when (msg.what) {
                    MESSAGE_STATE1, MESSAGE_STATE4, MESSAGE_STATE5 -> {
                        transitionTo(msg)
                    }
                    MESSAGE_STATE2 -> {
                        when (data) {
                            MESSAGE_STATE5 -> transitionTo(msg)
                            MESSAGE_STATE2 -> {
                                //ignore
                            }
                            else -> deferMessage(msg)
                        }
                    }
                    MESSAGE_STATE3 -> {
                        if (data != MESSAGE_STATE3) {
                            sendMessageAtFrontOfQueue(msg.what)
                            transitionTo(msg)
                        }
                    }
                    else -> {
                        return NOT_HANDLED
                    }
                }
                return HANDLED
            }
        }
    }
}