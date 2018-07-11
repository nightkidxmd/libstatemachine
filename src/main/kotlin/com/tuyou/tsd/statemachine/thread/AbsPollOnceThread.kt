package com.tuyou.tsd.statemachine.thread

import java.util.concurrent.Semaphore
import java.util.concurrent.locks.ReentrantLock


/**
 * Created by XMD on 2017/7/21.
 */
abstract class AbsPollOnceThread : Thread {
    constructor() : super()
    constructor(target: Runnable?) : super(target)
    constructor(target: (() -> Unit)?) : super(target)
    constructor(group: ThreadGroup?, target: Runnable?) : super(group, target)
    constructor(group: ThreadGroup?, target: (() -> Unit)?) : super(group, target)
    constructor(name: String?) : super(name)
    constructor(group: ThreadGroup?, name: String?) : super(group, name)
    constructor(target: Runnable?, name: String?) : super(target, name)
    constructor(target: (() -> Unit)?, name: String?) : super(target, name)
    constructor(group: ThreadGroup?, target: Runnable?, name: String?) : super(group, target, name)
    constructor(group: ThreadGroup?, target: (() -> Unit)?, name: String?) : super(group, target, name)
    constructor(group: ThreadGroup?, target: Runnable?, name: String?, stackSize: Long) : super(group, target, name, stackSize)
    constructor(group: ThreadGroup?, target: (() -> Unit)?, name: String?, stackSize: Long) : super(group, target, name, stackSize)

    private val lock = ReentrantLock()
    private val condition = lock.newCondition()
    protected var isRunning = true
    private val semaphore = Semaphore(0)
    private val exitWork = object :Work<Void>{
        override fun pollOnceWork(data: Void?) {
            isRunning = false
        }
    }

    override fun run() {
        semaphore.release()
        onStart()
        while (isRunning) {
            onPollOnce()
            lock()
            if (isRunning) {
                condition.await()
            }

            if (!isRunning) {
                break
            }
        }
        onExit()
    }

    protected fun lock() {
        lock.lock()
    }

    protected fun unlock() {
        lock.unlock()
    }

    open fun exit() {
        pollOnce(exitWork)
    }

    fun exitSyc(millis:Long = 0){
        exit()
        join(millis)
    }

    override fun start() {
        super.start()
        semaphore.acquire()
    }

    fun <T> pollOnce(work:Work<T>? = null, data:T?=null) {
        lock()
        try {
            work?.pollOnceWork(data)
            condition.signal()
        }finally {
            unlock()
        }
    }
    //-----------------------------------------------------
    abstract fun onPollOnce()
    abstract fun onStart()
    abstract fun onExit()

    interface Work<in T> {
        fun pollOnceWork(data:T?=null)
    }
}