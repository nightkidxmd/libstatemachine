### Common Google Statemachine
#### Illustration for test code
```kotlin
        init {
            addState(state1)
            addState(state2, state1)
            addState(state3, state1)
            addState(state4, state3)
            addState(state5, state3)
            setInitialState(state1)
            start()
        }

```
We build tree using <code>addState</code>, and set initial state with <code>setInitialState</code> and <code>start</code>
The tree built with above code is like below
```kotlin
         state1 ---> initial state
         /   \
     state2  state3
             /   \
         state4 state5 
```
The state machine is driven by message, we could use <code>sendMessage</code> or <code>sendMessageAtFrontOfQueue</code> to send message immediately and also could use <code> defferMessage </code> to delay to handle this message only when the state changed. 
Current state will receive the message at first and the message isn't consumed it'll be sent to current state's parent state until it is consumed or the state reach to root. 
Let's see the sample below.
1.  For each state, MESSAGE_STATE1, MESSAGE_STATE4, MESSAGE_STATE5 will transition to each state with <code>transitionTo</code> ;
2.  state1,3,4 deffer MESSAGE_STATE2, state5 will handle MESSAGE_STATE2 and transition to state2 
3.   all but state3 will handle MESSAGE_STATE3 and <code>sendMessageAtFrontOfQueue</code>
```kotlin
            override fun processMessage(msg: Message): Boolean {
                L.log(message = "$this process:$msg")
                when (msg.what) {
                    MESSAGE_STATE1, MESSAGE_STATE4, MESSAGE_STATE5 -> {
                        transitionTo(msg)
                    }
                    MESSAGE_STATE2 ->{
                        if(data == MESSAGE_STATE5){
                            transitionTo(msg)
                        }else if(data == MESSAGE_STATE2){
                            //ignore
                        }else{
                            deferMessage(msg)
                        }
                    }
                    MESSAGE_STATE3->{
                        if(data != MESSAGE_STATE3){
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
```
We send message from MESSAGE_STATE1 to MESSAGE_STATE5 then <code>quit</code>
```
        val test = Test()
        for(i in 1..5){
            test.sendMessage(i)
        }
        test.quit()
```
The whole progress is:
```kotlin
enter state1 // initial
//send message start
state1 process MESSAGE_STATE1
exit state1
enter state1
state1 process MESSAGE_STATE2
state1 deffer MESSAGE_STATE2
state1 process MESSAGE_STATE3
state1 send MESSAGE_STATE3
enter state3
state3 process MESSAGE_STATE2
state3 deffer MESSAGE_STATE2
state3 process MESSAGE_STATE3
state3 process MESSAGE_STATE4
enter state4
state4 process MESSAGE_STATE2
state4 deffer MESSAGE_STATE2
state4 process MESSAGE_STATE5
exit state4
enter state5
state5 process MESSAGE_STATE2
exit state5
exit state3
enter state2
exit state2
exit state1
enter QuitingState
```