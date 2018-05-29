package com.tuyou.tsd.statemachine.thread

import java.util.concurrent.Semaphore


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

    private val lock = Object()
    protected var isRunning = true
    private val semaphore = Semaphore(0)
    override fun run() {
        semaphore.release()
        onStart()
        while (isRunning) {
            onPollOnce()
            synchronized(lock, block = {
                if (isRunning) {
                    lock.wait()
                }
            })
            if (!isRunning) {
                break
            }
        }
        onExit()
    }

    open fun exit() {
        synchronized(lock, block = {
            isRunning = false
            lock.notify()
        })
    }

    override fun start() {
        super.start()
        semaphore.acquire()
    }

    fun pollOnce() {
        synchronized(lock, block = {
            lock.notify()
        })
    }
    //-----------------------------------------------------
    abstract fun onPollOnce()
    abstract fun onStart()
    abstract fun onExit()
}