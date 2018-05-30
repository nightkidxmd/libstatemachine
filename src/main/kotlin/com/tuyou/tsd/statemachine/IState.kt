package com.tuyou.tsd.statemachine

import com.tuyou.tsd.statemachine.message.Message

interface IState {

    companion object {
        /**
         * Returned by processMessage to indicate the the msg was processed.
         */
        const val HANDLED = true

        /**
         * Returned by processMessage to indicate the the msg was NOT processed.
         */
        const val NOT_HANDLED = false
    }


    /**
     * Called when a state is entered.
     */
    fun enter()

    /**
     * Called when a state is exited.
     */
    fun exit()

    /**
     * Called when a msg is to be processed by the
     * state machine.
     *
     * This routine is never reentered thus no synchronization
     * is needed as only one processMessage method will ever be
     * executing within a state machine at any given time. This
     * does mean that processing by this routine must be completed
     * as expeditiously as possible as no subsequent messages will
     * be processed until this routine returns.
     *
     * @param msg to process
     * @return HANDLED if processing has completed and NOT_HANDLED
     * if the msg wasn't processed.
     */
    fun processMessage(msg: Message): Boolean

    /**
     * Name of SState for debugging purposes.
     *
     * @return name of state.
     */
    fun getName(): String
}