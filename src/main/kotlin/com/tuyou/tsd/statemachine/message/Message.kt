package com.tuyou.tsd.statemachine.message

/**
 * Created by XMD on 2017/7/20.
 */
data class Message(var what:Int = 0,var obj:Any?=null,var arg1:Int=0,var arg2:Int=0){
    var next:Message? = null

    companion object {
        private var pool:Message? = null
        private var poolSize = 0
        private val poolLock = Any()
        private const val MAX_POOL_SIZE = 100


        @JvmStatic fun obtain(what:Int = 0,obj:Any?=null,arg1:Int=0,arg2:Int=0) = synchronized(poolLock,{
            if(pool != null){
                val m = pool!!
                pool = m.next
                m.next = null
                poolSize--
                m.apply {
                    this.what = what
                    this.obj = obj
                    this.arg1 = arg1
                    this.arg2 = arg2
                }
            }else{
                Message(what,obj,arg1,arg2)
            }
        })
    }

    fun recycle()= synchronized(poolLock,{
        what = 0
        obj = null
        arg1 = 0
        arg2 = 0
        if(poolSize < MAX_POOL_SIZE){
            next = pool
            pool = this
            poolSize++
        }
    })
}