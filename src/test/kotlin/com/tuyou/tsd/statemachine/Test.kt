package com.tuyou.tsd.statemachine

import com.tuyou.tsd.statemachine.log.L
import com.tuyou.tsd.statemachine.message.LinkedMessageQueue
import com.tuyou.tsd.statemachine.message.Message
import com.tuyou.tsd.statemachine.thread.AbsPollOnceThread
import com.tuyou.tsd.statemachine.thread.Handler
import com.tuyou.tsd.statemachine.thread.Looper

import org.junit.Test




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
                L.log(message = "$this process:$msg")
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