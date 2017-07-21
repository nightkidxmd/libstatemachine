package com.tuyou.tsd.statemachine
import main.java.com.tuyou.tsd.statemachine.StateMachine
import main.java.com.tuyou.tsd.statemachine.log.L
import main.java.com.tuyou.tsd.statemachine.message.Message
import java.util.*

/**
 * Created by XMD on 2017/7/20.
 */
object Main {


       @JvmStatic fun main(args: Array<String>){
          val test = Test()
           for(i in 1..10){
               val what = Random().nextInt(10)+1
               L.log(message ="send:$what")
               test.sendMessage(what)
           }
           test.quit()
        }


    private class Test:StateMachine("test"){
        companion object{
            val MESSAGE_STATE1 = 1
            val MESSAGE_STATE2 = 2
            val MESSAGE_STATE3 = 3
            val MESSAGE_STATE4 = 4
            val MESSAGE_STATE5 = 5
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
            addState(state2,state1)
            addState(state3,state1)
            addState(state4,state3)
            addState(state5,state3)
            setInitialState(state1)
            start()
        }

        fun transitionTo(msg: Message){
            transitionTo(transitionMap[msg.what]!!)
        }

         inner class MyState(val data:Int):main.java.com.tuyou.tsd.statemachine.SState(){
             override fun toString(): String{
                 return "MyState(data=$data)"
             }

             override fun processMessage(msg: Message): Boolean {
                 if(msg.what <=5 ){
                     transitionTo(msg)
                 }else{
                     return NOT_HANDLED
                 }

                 return HANDLED
             }
         }
    }
}