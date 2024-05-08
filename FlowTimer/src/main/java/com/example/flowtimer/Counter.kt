package com.example.flowtimer

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.TickerMode
import kotlinx.coroutines.channels.ticker
import kotlinx.coroutines.flow.*
import kotlin.coroutines.CoroutineContext

/**
 * @property countTimeInterval 每一次間格的計算單位，預設為1秒
 */
@ObsoleteCoroutinesApi
abstract class Counter(coroutineContext: CoroutineContext = Dispatchers.Main, countTimeInterval: Long? = null, private var countDownTimeStart: Long = 0) {

    protected var countTimeInterval = countTimeInterval ?: 1000

    val flowTimer = MutableSharedFlow<Long>()

    private var countJob: Job? = null

    private var nowTime = countDownTimeStart

    private var receiveChannel: ReceiveChannel<Unit>? = null

    protected var isCancelByUser: Boolean? = null

    private val coroutineScope = CoroutineScope(coroutineContext)

    private fun resetTimer() {
        this.nowTime = countDownTimeStart
    }

    open fun startCount() {
        stopCount()
        countAction()
    }

    fun resumeCount() {
        cancelCount(false)
        countAction()
    }

    protected open fun countAction() {
        controlCount()
    }

    protected fun cancelCount(isCancelByUser: Boolean) {
        this.isCancelByUser = isCancelByUser
        countJob?.cancel()
        receiveChannel?.cancel()
    }

    fun pauseCount() {
        cancelCount(true)
    }

    fun stopCount() {
        cancelCount(true)
        resetCount()
    }

    fun getNowTime(): Long {
        return nowTime
    }

    protected fun resetCount() {
        coroutineScope.launch {
            flowTimer.emit(countDownTimeStart)
        }

        resetTimer()
    }

    private fun tickerFlow(l: Long, context: CoroutineContext): Flow<Unit> {
        receiveChannel?.cancel()

        receiveChannel = ticker(l, 0, context, TickerMode.FIXED_DELAY)

        return receiveChannel!!.receiveAsFlow()
    }

    private fun controlCount() {

        countJob = coroutineScope.launch {
            tickerFlow(countTimeInterval, this.coroutineContext).onEach {
                val needEmitTime = getNewNowTime(nowTime)
                if (needEmitTime != null) {
                    nowTime = needEmitTime
                    flowTimer.emit(needEmitTime)
                } else {
                    cancelCount(false)
                }
            }.launchIn(this)
        }

        if (countJob?.isCancelled == true) countJob?.start()
    }


    abstract fun getNewNowTime(oloNowTime: Long): Long?
}