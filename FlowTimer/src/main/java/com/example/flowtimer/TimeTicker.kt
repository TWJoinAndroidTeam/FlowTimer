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
abstract class TimeTicker(coroutineContext: CoroutineContext = Dispatchers.Main, countTimeInterval: Long? = null, private var countDownTimeStart: Long = 0) {

    private var countTimeInterval = 1000L

    val flowTimer = MutableSharedFlow<Long>()
    var countJob: Job? = null

    private var nowTime = 0L

    private var receiveChannel: ReceiveChannel<Unit>? = null

    protected var isCancelByUser = false

    private val coroutineScope = CoroutineScope(coroutineContext)

    init {
        initCountDownTimer(countTimeInterval, countDownTimeStart)
    }

    /**
     * @param countTimeInterval 每一次間格的計算單位，預設為1秒
     */
    private fun initCountDownTimer(countTimeInterval: Long? = null, countDownTimeStart: Long) {
        if (countTimeInterval != null) {
            this.countTimeInterval = countTimeInterval
        }

        this.nowTime = countDownTimeStart
    }

    open fun startCount() {
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
        initCountDownTimer(countTimeInterval, countDownTimeStart)
    }

    private fun tickerFlow(l: Long, context: CoroutineContext): Flow<Unit> {
        receiveChannel?.cancel()

        receiveChannel = ticker(l, 0, context, TickerMode.FIXED_DELAY)

        return receiveChannel!!.receiveAsFlow()
    }


    private fun controlCount() {
        countJob = coroutineScope.launch {
            tickerFlow(countTimeInterval, this.coroutineContext).onEach {
                val needEmitTime = getNowTime(countTimeInterval, nowTime)
                if (needEmitTime != null) {
                    nowTime = needEmitTime
                    flowTimer.emit(needEmitTime)
                } else cancelCount(false)
            }.launchIn(this)
        }
        if (countJob?.isCancelled == true) countJob?.start()
    }


    abstract fun getNowTime(countTimeInterval: Long, nowTime: Long): Long?
}